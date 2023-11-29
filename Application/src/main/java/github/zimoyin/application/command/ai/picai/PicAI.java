package github.zimoyin.application.command.ai.picai;

import github.zimoyin.application.server.ai.pic.PicAIServer;
import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.annotation.ThreadSpace;
import github.zimoyin.mtool.command.CommandData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CommandClass
public class PicAI {
    @Command(value = "pic", description = "AI画图")
    @ThreadSpace
    public void pic(CommandData commandData) {
        PicAIServer.getInstance().add(commandData);
    }
}
