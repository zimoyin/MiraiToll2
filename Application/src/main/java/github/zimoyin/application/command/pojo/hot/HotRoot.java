/**
 * Copyright 2022 json.cn
 */
package github.zimoyin.application.command.pojo.hot;

import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2022-08-14 20:32:3
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@lombok.Data
public class HotRoot {

    private boolean success;
    /**
     * 标题
     */
    private String title;
    /**
     * 子标题
     */
    private String subtitle;
    /**
     * 爬取时间
     */
    private Date update_time;
    /**
     * 数据
     */
    private List<Data> data;
}