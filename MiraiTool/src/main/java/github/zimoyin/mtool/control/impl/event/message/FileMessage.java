package github.zimoyin.mtool.control.impl.event.message;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.event.FileMessageEvent;
import net.mamoe.mirai.event.EventKt;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileMessage {
    @EventType
    public void onFileMessageEvent(MessageEvent event) {
        MessageChain chain = event.getMessage();
        List<SingleMessage> files = chain.stream().filter(net.mamoe.mirai.message.data.FileMessage.class::isInstance).collect(Collectors.toList());
        List<SingleMessage> audios = chain.stream().filter(Audio.class::isInstance).collect(Collectors.toList());
        List<SingleMessage> voices = chain.stream().filter(Voice.class::isInstance).collect(Collectors.toList());
        List<SingleMessage> images = chain.stream().filter(Image.class::isInstance).collect(Collectors.toList());
        List<SingleMessage> all = new ArrayList<SingleMessage>();
        if (!files.isEmpty() || !audios.isEmpty() || !voices.isEmpty() || !images.isEmpty()) {
            all.addAll(files);
            all.addAll(audios);
            all.addAll(voices);
            all.addAll(images);
            FileMessageEvent messageEvent = new FileMessageEvent(event.getBot(), chain, all, event.getSender(), event.getSenderName(), event.getSource(), event.getSubject(), event.getTime());
            EventKt.broadcast(messageEvent);
        }

        if (!files.isEmpty()) {
            FileMessageEvent.FileUploadMessageEvent messageEvent = new FileMessageEvent.FileUploadMessageEvent(event.getBot(), chain, files, event.getSender(), event.getSenderName(), event.getSource(), event.getSubject(), event.getTime());
            EventKt.broadcast(messageEvent);
        }

        if (!audios.isEmpty() || !voices.isEmpty()) {
            audios.addAll(voices);
            FileMessageEvent.AudioMessageEvent messageEvent = new FileMessageEvent.AudioMessageEvent(event.getBot(), chain, audios, event.getSender(), event.getSenderName(), event.getSource(), event.getSubject(), event.getTime());
            EventKt.broadcast(messageEvent);
        }

        if (!images.isEmpty()) {
            FileMessageEvent.ImageMessageEvent messageEvent = new FileMessageEvent.ImageMessageEvent(event.getBot(), chain, images, event.getSender(), event.getSenderName(), event.getSource(), event.getSubject(), event.getTime());
            EventKt.broadcast(messageEvent);
        }
    }

}
