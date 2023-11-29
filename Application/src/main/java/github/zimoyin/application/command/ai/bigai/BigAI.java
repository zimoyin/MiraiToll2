package github.zimoyin.application.command.ai.bigai;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.annotation.ThreadSpace;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.util.IO;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import github.zimoyin.mtool.util.message.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;


@Slf4j
@CommandClass
public class BigAI {
    private final String applicationPath = "./lib/bigPictureAI/realesrgan-ncnn-vulkan.exe";
    private final String modelName = "realesrgan-x4plus-anime";
    private int taskCount = 0;

    @Command(value = "big", description = "图片放大")
    @ThreadSpace
    @Deprecated
    public void help(CommandData commandData) throws IOException, InterruptedException {
        //并发数限制
        if (taskCount >= 5) {
            commandData.sendMessage("当前图片有点多，请稍后再试");
            return;
        }
        taskCount++;
        //获取图片
        Image image = commandData.getImage();
        if (image == null) {
            commandData.sendMessage("命令未携带参数【图片】，请指定参数");
            return;
        }
        String imageId = image.getImageId();
        ImageType imageType = image.getImageType();
        InputStream imageInputStream = ImageUtils.getImageInputStream(image);
        //图片保存到本地
        File file = new File("./data/cache/pic/" + imageId + "." + imageType.name());
        file.getParentFile().mkdirs();
        if (file.exists()) log.debug("本地存在原图片缓存文件：{}", file);
        if (!file.exists()) IO.toFile(imageInputStream, file.toString());
        else imageInputStream.close();
        //放大图片
        File out = new File("./data/cache/pic/" + imageId + "x4." + imageType.name());
        if (out.exists()) log.debug("本地存在放大图片缓存文件：{}", file);
        if (!out.exists()) run(file, out, modelName, 4);
        //图片发送回去
        NewThreadPoolUtils.getInstance().execute(() -> {
            taskCount--;
            try {
                Thread.sleep(1000 * 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!out.exists()) commandData.sendMessage("放大图片失败");
            else {
                try {
                    commandData.sendMessage(ImageUtils.getImage(Files.newInputStream(out.toPath()), commandData.getContact()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * @param inputPath
     * @param outputPath
     * @param modelName
     * @param multiple   realesrgan-ncnn-vulkan.exe -i inputPath -o outputPath -n realesr-animevideov3 -s 4
     */
    private synchronized boolean run(File inputPath, File outputPath,
                                     String modelName, int multiple) throws IOException, InterruptedException {
        String buffer = new StringBuffer()
//                .append("cmd.exe ")
//                .append("/c ")
//                .append("start ")
                .append(new File(applicationPath).getCanonicalPath()).append(" ")
                .append("-i ")
                .append(inputPath.getCanonicalPath()).append(" ")
                .append("-o ")
                .append(outputPath.getCanonicalPath()).append(" ")
                .append("-n ")
                .append(modelName).append(" ")
                .append("-s ")
                .append(multiple)
                .toString();

        log.info("shall execute: " + buffer);
        Process exec = Runtime.getRuntime().exec(buffer);
        // 等待进程对象执行完成，并返回“退出值”，0 为正常，其他为异常
        boolean exitValue = exec.waitFor(12, TimeUnit.SECONDS);
        // 销毁process对象
        exec.destroy();
        return exitValue;
    }
}
