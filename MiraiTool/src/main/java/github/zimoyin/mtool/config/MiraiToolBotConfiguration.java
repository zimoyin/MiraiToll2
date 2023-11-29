package github.zimoyin.mtool.config;


import github.zimoyin.mtool.dao.MiraiLog4j;
import github.zimoyin.mtool.exception.BotConfigRuntimeException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 配置机器人的登录已经状态信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class MiraiToolBotConfiguration extends BotConfiguration {
    //缓存保存地方
    private String cache = "./cache/%s/";
    //日志
    private Logger logger = LoggerFactory.getLogger(MiraiToolBotConfiguration.class);
    //心跳策略
    private HeartbeatStrategy register = HeartbeatStrategy.REGISTER;
    //登录协议
    private MiraiProtocol version = MiraiProtocol.ANDROID_PAD;
    //是否关闭log日志
    private boolean isLog = false;
    //是否关闭net日志
    private boolean isNet = true;
    //设备文件路径
    private String devicePath;
    //是否二维码登录
    private boolean isQRLogin = false;
    //是否共享设备信息
    private final boolean isShareDevice;

    public MiraiToolBotConfiguration() {
        this.isShareDevice = true;
    }

    /**
     * @param isShareDevice 设备文件路径是否共享
     */
    public MiraiToolBotConfiguration(final boolean isShareDevice) {
        this.isShareDevice = isShareDevice;
    }

    public MiraiToolBotConfiguration initialize(final long id){
        this.cache = String.format(cache, id);
        logger.info("[配置信息]缓存信息目录:{} {}", cache, new File(cache).mkdirs());
        if (isShareDevice) this.devicePath = "cache/device.json";
        else this.devicePath = cache + "device.json";
        try {
            init();
        } catch (IOException e) {
            throw new BotConfigRuntimeException(e);
        }
        return this;
    }

    //初始化
    private void init() throws IOException {
        initBefore();
        redirectBotLogToFile(new File("./log/mirai/log.log"));
        redirectNetworkLogToFile(new File("./log/mirai/net.log"));
        //重定向日志
        MiraiLoggerPlatformBase log = new MiraiLog4j();
        setBotLoggerSupplier(bot -> log);
        setNetworkLoggerSupplier(bot -> log);
        logger.info("[配置信息]Mirai日志重定向为：{}", log);
        //设备信息
        fileBasedDeviceInfo(devicePath);
        logger.info("[配置信息]设备信息：{}", devicePath);
        // 心跳策略
        setHeartbeatStrategy(register);
        logger.info("[配置信息]心跳策略为：{}", register);
        // 登录协议
        setProtocol(version);
        logger.info("[配置信息]登录协议为：{}", version);
        // 运行目录
        //setWorkingDir(new File("C:/mirai"));
        logger.info("[配置信息]运行目录为： {}", getWorkingDir().getCanonicalPath());
        // 修改 Bot 缓存目录 (以运行目录为相对路径的坐标系)
        setCacheDir(new File(cache)); // 最终为 workingDir 目录中的 cache 目录
        logger.info("[配置信息]缓存目录为:{}", getCacheDir());
        //关闭日志
        if (isLog) noBotLog();
        logger.info("[配置信息]关闭log日志: {}", isLog);
        if (isNet) noNetworkLog();
        logger.info("[配置信息]关闭net日志: {}", isNet);
        // 开启所有列表缓存
        enableContactCache();
        logger.info("[配置信息]列表缓存已开启");

        //覆盖登录器
//        ImageLoginSolverKt solverKt = new ImageLoginSolverKt();//有短信验证
//        setLoginSolver(solverKt);
        initAfter();
    }

    public abstract void initBefore();
    public abstract void initAfter();


    /**
     * 能否使用二维码登录
     */
    public final boolean isCanUseQR() {
        if (version == MiraiProtocol.ANDROID_WATCH || version == MiraiProtocol.MACOS) return true;
        return false;
    }

    /**
     * 启用二维码登录
     */
    public final void setQRLoginEnabled() {
        if (!isCanUseQR()) version = MiraiProtocol.ANDROID_WATCH;
        isQRLogin = true;
    }
}
