package github.zimoyin.application.command.chatgpt.api2.api;

import com.alibaba.fastjson2.JSON;
import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;

import java.io.IOException;

/**
 * 桑帛云提供强大的ChatGPT API接口
 */
@CommandClass
public class SangJinYun {
    private static final String URL = "https://api.caonm.net/api/ai/o.php?img=%s";

    @Command("sj")
    public String command(CommandData data) throws IOException {
        HttpClientResult result = HttpClientUtils.doGet(String.format(URL, data.getParam().trim()));
        return JSON.parseObject(result.getContent()).getString("html").trim();
    }
}
