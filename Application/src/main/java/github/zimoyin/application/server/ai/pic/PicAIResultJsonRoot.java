package github.zimoyin.application.server.ai.pic;

import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2022-10-31 21:10:15
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class PicAIResultJsonRoot {
    private List<String> images;
    private Parameters parameters;
    private Info info;
}