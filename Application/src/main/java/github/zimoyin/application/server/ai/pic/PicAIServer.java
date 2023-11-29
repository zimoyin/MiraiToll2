package github.zimoyin.application.server.ai.pic;


import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import github.zimoyin.mtool.util.message.ImageUtils;
import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PicAIServer {
    private volatile static PicAIServer INSTANCE;
    private final BlockingQueue<CommandData> queue = new LinkedTransferQueue<>();
    private volatile int count = 0;

    private PicAIServer() {
        NewThreadPoolUtils.getInstance().execute(() -> {
            while (true) {
                CommandData poll = null;
                try {
                    poll = queue.poll(3, TimeUnit.MINUTES);
                    run(poll);
                } catch (Exception e) {
                    log.error("生成图片失败", e);
                    if (poll == null) log.error("无法获取到聊天窗口信息");
                    else poll.sendMessage("图片生成失败，请不要再次尝试");
                }
            }
        });
    }

    public static PicAIServer getInstance() {
        if (INSTANCE == null) synchronized (PicAIServer.class) {
            if (INSTANCE == null) INSTANCE = new PicAIServer();
        }
        return INSTANCE;
    }


    public void add(CommandData data) {
        StringBuilder buffer = new StringBuilder();
        for (String param : data.getParams()) buffer.append(param.trim()).append(",");
        if (buffer.toString().length() > 100) {
            data.sendMessage("禁止生成图片：关键字大于79");
            return;
        }
        if (queue.size() >= 3) {
            data.sendMessage("禁止生成图片：任务队列以满");
            return;
        }
        data.setTextMessage(buffer.toString());
        queue.add(data);
        count++;
        data.sendMessage("图片生成中(" + queue.size() + "/" + count + ").....");
    }


    private void run(CommandData poll) throws IOException {
        if (poll == null) return;
        PicAITextToImgJsonRoot json = new PicAITextToImgJsonRoot();
        json.setPrompt(poll.getTextMessage());

        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json");

        HttpClientResult httpClientResult = HttpClientUtils.doPost("http://127.0.0.1:7860/sdapi/v1/txt2img", map, null, new StringEntity(json.toString()));
        PicAIResultJsonRoot root = JSONObject.parseObject(httpClientResult.getContent(), PicAIResultJsonRoot.class);
        String base64 = root.getImages().get(0);
        byte[] bytes = Base64.decodeBase64(base64);

        Image image = ImageUtils.getImage(bytes, poll.getContact());
        poll.sendMessage(image);
    }
}
