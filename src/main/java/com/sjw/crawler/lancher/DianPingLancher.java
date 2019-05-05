package com.sjw.crawler.lancher;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.sjw.crawler.Config;
import com.sjw.crawler.pageprocesser.DianpingPageProcesser;
import com.sjw.crawler.pipeline.DianPingPipeline;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import java.util.List;

/**
 * Created by Administrator on 2019/3/5.
 */
public class DianPingLancher {

    public static void startCrawler() {
        DianPingPipeline pipeline = new DianPingPipeline();
        Request request = new Request();
        request.putExtra("aaa", 0);
        request.setUrl("http://www.dianping.com/shanghai/ch10/g110");
        Spider.create(new DianpingPageProcesser())
                .addPipeline(pipeline)
                .addRequest(request)
                .run();
        List collected = pipeline.getCollected();
        ExcelWriter writer = ExcelUtil.getWriter(Config.EXCEL_PATH);
        writer.write(collected);
        writer.close();
    }
}
