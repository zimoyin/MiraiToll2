package github.zimoyin.solver;

import github.zimoyin.solver.communication.CommunicationChannelOfURL;
import github.zimoyin.solver.gui.LoginSolverGuiRun;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.auth.QRCodeLoginListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;

@Slf4j
public class QRListenerToURL implements QRCodeLoginListener {
    private static final String PATH = "./cache/login.png";

    public QRListenerToURL() {
        LoginSolverGuiRun.getInstance();
    }

    /**
     * 从服务器获取二维码时调用，在下级显示二维码并扫描.
     *
     * @param bytes 二维码图像数据 (文件)
     */
    @SneakyThrows
    @Override
    public void onFetchQRCode(@NotNull Bot bot, @NotNull byte[] bytes) {
        File file = new File(PATH);
        file.getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(bytes);
            stream.flush();
        }
        log.info("创建二维码文件: " + PATH);
        CommunicationChannelOfURL.getInstance().setValue(file.toURI().toURL().toString());
    }

    /**
     * 当二维码状态变化时调用.
     *
     * @see State
     */
    @Override
    public void onStateChanged(@NotNull Bot bot, @NotNull State state) {
        //等待扫描中，请在此阶段请扫描二维码.
        if (state == State.WAITING_FOR_SCAN) {
            log.debug("请扫描二维码");
        }
        // 二维码已扫描，等待扫描端确认登录.
        else if (state == State.WAITING_FOR_CONFIRM) {
            log.debug("二维码以扫描，等待客户端确认登录");
        }
        // 扫描后取消了确认.
        else if (state == State.CANCELLED) {
            log.error("客户端取消登录授权");
        }
        //二维码超时，必须重新获取二维码.
        else if (state == State.TIMEOUT) {
            log.warn("二维码登录超时");
        }
        // 二维码已确认，将会继续登录.
        else if (state == State.CONFIRMED) {
            log.info("二维码已登录");
        }
        //默认状态，在登录前通常为此状态.
        else if (state == State.DEFAULT) {
        }
    }

    /**
     * 每隔一段时间会调用一次此函数
     * <p>
     * 在此函数抛出 [LoginFailedException] 以中断登录
     */
    @Override
    public void onIntervalLoop() {
        QRCodeLoginListener.super.onIntervalLoop();
    }
}
