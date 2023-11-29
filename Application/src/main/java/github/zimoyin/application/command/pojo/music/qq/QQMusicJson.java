/**
 * Copyright 2022 json.cn
 */
package github.zimoyin.application.command.pojo.music.qq;

import lombok.Data;

/**
 * Auto-generated: 2022-08-14 17:21:52
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class QQMusicJson {

    /**
     * APP 包名
     */
    private String app;
    /**
     * 描述信息
     */
    private String desc;
    /**
     * 描述信息
     */
    private String view;
    /**
     * ？？？
     */
    private String ver;
    /**
     * 分享信息，eg:[分享]前前前世
     */
    private String prompt;
    private String appID;
    private String sourceName;
    private String actionData;
    private String actionData_A;
    private String sourceUrl;
    /**
     * 正文内容
     */
    private Meta meta;
    private Config config;
    private String text;
    private String sourceAd;
    private String extra;
}