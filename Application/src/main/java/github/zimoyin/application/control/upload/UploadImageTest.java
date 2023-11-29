package github.zimoyin.application.control.upload;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.BeforeImageUploadEvent;
import net.mamoe.mirai.event.events.ImageUploadEvent;

@Slf4j
@Controller
public class UploadImageTest {
    /**
     * 图片上传前
     */
    @EventType
    public void onBeforeImageUploadEvent(BeforeImageUploadEvent event) {
        log.info("图片上传前");
    }

    /**
     * 图片上传完成
     */
    @EventType
    public void onImageUploadEvent(ImageUploadEvent event) {
        log.info("图片上传完成");
    }

    @EventType
    public void onImageUploadEvent(ImageUploadEvent.Failed event) {
        log.info("图片上传完成：失败");
    }

    @EventType
    public void onImageUploadEvent(ImageUploadEvent.Succeed event) {
        log.info("图片上传完成：成功");
    }
}
