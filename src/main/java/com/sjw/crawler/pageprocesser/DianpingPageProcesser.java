package com.sjw.crawler.pageprocesser;

import cn.hutool.core.util.ArrayUtil;
import com.sjw.crawler.tools.RegexUtil;
import com.sjw.crawler.tools.okhttpUtils;
import okhttp3.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/5.
 */
public class DianpingPageProcesser implements PageProcessor {
    //css页面具体内容
    private String css_text = null;
    //class 下标
    private HashMap<String, String> map = new HashMap<>();
    //svg 的具体内容
    private HashMap<String, String> svg_map = new HashMap<>();


    @Override
    public void process(Page page) {
        String s = page.getHtml().xpath("//a[@data-ga-page='50']/text()").get();

        String exist = page.getHtml().xpath("//div[@id='top-nav']").get();
        Integer aaa = (Integer) page.getRequest().getExtra("aaa");
        //分类进行细化
        if (exist != null) {
            //添加 二级分类
            if (s != null) {
                if (aaa == 0) {
                    List<String> all = page.getHtml().xpath("//div[@id='classfy-sub']/a/@href").all();
                    for (String url : all) {
                        Request request = new Request();
                        request.setUrl(url);
                        request.putExtra("aaa", 1);
                        page.addTargetRequest(request);
                    }
                }
                //添加地址栏分类
                if (aaa == 1) {
                    List<String> all = page.getHtml().xpath("//div[@id='region-nav']/a/@href").all();
                    for (String url : all) {
                        Request request = new Request();
                        request.setUrl(url);
                        request.putExtra("aaa", 2);
                        page.addTargetRequest(request);
                    }
                }
                //如果数据还是 达到了，最大值，则这里进行提示
                if (aaa == 2) {
                    System.out.println();
                }
            } else {
                //数据进行解析
                List<Selectable> nodes = page.getHtml().xpath("//div[@class='txt']").nodes();
                ArrayList<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
                String css_url = RegexUtil.getSubUtilSimple(page.getRawText(), "href=\"//s3plus.meituan.net(.*?)\"");
                String css_text = getCss_text(css_url);
                Map<String, String> map = getMap(css_text);
                for (Selectable selectable : nodes) {
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    String title = selectable.xpath("//h4/text()").get();
                    String addr = selectable.xpath("//span[@class='addr']").replace("<span class=\"addr\">", "").replace("</span>", "").get();
                    List<String> addrs = RegexUtil.getSubUtil(addr, "\"(.*?)\"");
                    for (String class_addr : addrs) {
                        String backageground = map.get(class_addr);
                        getSVG(class_addr, css_text);
                        String[] pxes = backageground.replace("-", "").replace("px", "").split(" ");
                        int x = Integer.parseInt(pxes[0]) / 12;
                        int y = Integer.parseInt(pxes[1]) + 24;
                        String base = class_addr.substring(0, 2);
                        Document connect = Jsoup.parse(svg_map.get(base));
                        String attr = connect.body().getElementsByAttributeValue("d", "M0 " + y + " H600").attr("id");
                        String text = connect.body().getElementsByAttributeValue("xlink:href", "#" + attr).text();
                        String s1 = text.split("")[x];
                        addr = addr.replace("<span class=\"" + class_addr + "\">", s1);
                    }
                    //addr 需要进行转换的
                    String shopId = selectable.xpath("//a[@data-shopid]/@data-shopid").get();
                    data.put("title", title);
                    data.put("addr", addr);
                    data.put("shopId", shopId);
                    System.out.println(title+"---------->"+addr);
                    maps.add(data);

                }
                page.putField("aaa", maps);
                //分页
                List<String> all = page.getHtml().xpath("//a[@data-ga-page]/@data-ga-page").all();
                Integer[] anInt = getInt(all.toArray(new String[0]));
                Integer int_lastPage = 1;
                if (anInt.length != 0) {
                    int_lastPage = ArrayUtil.max(anInt);
                }
                String s1 = page.getUrl().get();
                for (int i = 2; i <= int_lastPage; i++) {
                    page.addTargetRequest(s1 + "p" + i);
                }
            }
        } else {
            //页面被验证码了
            System.out.println("页面被验证码了:" +page.getUrl());
        }

    }


    private Integer[] getInt(String... aaa) {
        Integer[] integers = new Integer[aaa.length];
        for (int i = 0; i < aaa.length - 1; i++) {
            String s = aaa[i];
            integers[i] = Integer.parseInt(s);
        }
        return integers;
    }

    @Override
    public Site getSite() {
        return Site.me().setRetryTimes(10)
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("host", "www.dianping.com")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36")
                .addHeader("cache-control", "no-cache");
    }

    private void getStr(String css_url) {
        HashMap<String, String> map = new HashMap<>();
        String text = getCss_text(css_url);
        String[] split = text.replace(".", "").replace("background:", "").replace("{", " ").split(";}");
        for (String str : split) {
            String s = str.split(" ")[0];
            try {
                map.put(s, str.split(" ")[1] + " " + str.split(" ")[2]);
            } catch (Exception e) {
                System.out.println();
            }
        }
        System.out.println("");
    }

    /**
     * 根据css的 url 来获取 整个css 页面的内容
     *
     * @param css_url
     * @return
     */
    private String getCss_text(String css_url) {
        Connection connect = Jsoup.connect("http://s3plus.meituan.net" + css_url);
        try {
            Document document = connect.get();
            css_text = document.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return css_text;
    }

    /**
     * 根据css 页面的内容来，获取 他 的 backageground
     *
     * @param text
     * @return
     */
    private Map<String, String> getMap(String text) {
        map = new HashMap<>();
        String[] split = text.replace(".0", "").replace("background:", "").replace("{", " ").split(";}");
        for (String str : split) {
            String s = str.split(" ")[0].replace(".", "");
            try {
                map.put(s, str.split(" ")[1] + " " + str.split(" ")[2]);
            } catch (Exception e) {
            }
        }
        return map;
    }

    /**
     * 跟句他的class属性的前三个字符，来获取他的 整个 svg 的页面
     *
     * @param class_name
     * @return
     */
    private void getSVG(String class_name, String text) {
        class_name = class_name.substring(0, 2);
        if (svg_map.get(class_name) == null) {
            List<String> subUtil = RegexUtil.getSubUtil(text, "span(.*?);}");
            for (String str : subUtil) {
                if (str.contains(class_name)) {
                    String str1 = RegexUtil.getSubUtilSimple(str, "(?<=\\()(\\S+)(?=\\))");
                    svg_map.put(class_name, getSVG_text(str1));
                }
            }
        }
    }

    /**
     * 请求 svg 页面的数据
     *
     * @param url
     * @return
     */
    private String getSVG_text(String url) {
        okhttp3.Request build = new okhttp3.Request.Builder().url("https://" + url).build();
        try {
            Response execute = okhttpUtils.execute(build);
            return execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
