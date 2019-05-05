package com.sjw.crawler.pipeline;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.sjw.crawler.Config;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.CollectorPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/5.
 */
public class DianPingPipeline implements CollectorPipeline {
    private List<Map<String, Object>> collected = new ArrayList<Map<String, Object>>();

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> all = resultItems.getAll();
        ArrayList<Map<String, Object>> maps = (ArrayList<Map<String, Object>>) all.get("aaa");
        if (all.size() != 0) {
            collected.addAll(maps);
        }
        if (collected.size() % 100 == 0) {
            ExcelWriter writer = ExcelUtil.getWriter(Config.EXCEL_PATH);writer.write(collected);
            writer.write(collected);
            writer.close();
        }
    }

    @Override
    public List getCollected() {
        return collected;
    }
}
