package github.zimoyin.application.command;

import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.application.command.pojo.music.qq.Music;
import github.zimoyin.application.command.pojo.music.qq.QQMusicJson;
import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@CommandClass
public class CommandMusic {
    //QQ音乐 歌名
    //QQ音乐解析
    private static final String URL_QQ_1 = "https://shengapi.cn/api/qqyy.php?song=%s&id=1";
    private static final Logger logger = LoggerFactory.getLogger(CommandMusic.class);

    //    @Command(value = "点歌",description = "查找音乐并播放（参数：【歌名】）")
    @Command(value = "点歌", description = "查找音乐并播放（参数：【歌名】）",
            help = "",
            alias = {"a", "b"},
            eventType = MessageEvent.class
    )
    public void music(CommandData commandData) throws IOException {
        if (commandData.isEmptyParams()) {
            commandData.sendMessage("命令格式错误: %s点歌 歌名", commandData.getPrefix());
            return;
        }
        //获取参数
        String[] params = commandData.getParams();
        String url = String.format(URL_QQ_1, params[0]);
        logger.info("访问URL：{}", url);
        HttpClientResult httpClientResult = HttpClientUtils.doGet(url);
        String content = httpClientResult.getContent();
        logger.info("响应正文: [{}] -> \r\n{}", url, content);
        QQMusicJson musicRoot = JSONObject.parseObject(content, QQMusicJson.class);
        Music music = musicRoot.getMeta().getMusic();
        String title = music.getTitle();//标题
        String jumpUrl = music.getJumpUrl();
        String musicUrl = music.getMusicUrl();
        String pic = music.getPreview();
        String desc = music.getDesc();
        //构造信息
        MusicShare musicShare = new MusicShare(
                MusicKind.QQMusic,
                title,
                desc,
                jumpUrl,
                pic,
                musicUrl
        );
        //发送信息
        commandData.sendMessage(musicShare);
    }


    @Command(value = "QQ", description = "查找音乐并播放（参数：【歌名】）")
    public void sb(CommandData command) throws IOException {
        music(command);
    }


}
