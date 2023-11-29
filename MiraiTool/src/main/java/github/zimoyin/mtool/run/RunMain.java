package github.zimoyin.mtool.run;

import github.zimoyin.mtool.command.CommandLoadInit;
import github.zimoyin.mtool.command.filter.FilterTable;
import github.zimoyin.mtool.command.filter.GlobalFilterInitOrExecute;
import github.zimoyin.mtool.config.MiraiToolBotConfiguration;
import github.zimoyin.mtool.config.global.LevelConfig;
import github.zimoyin.mtool.config.global.LoginConfig;
import github.zimoyin.mtool.control.ControllerAbs;
import github.zimoyin.mtool.login.Login;
import github.zimoyin.mtool.plug.loader.PlugLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 启动类
 */
@Slf4j
public final class RunMain {
    public static final String VERSION = "1.2.0-ClassLoader";
    public static boolean isRunning = false;

    /**
     * 启动框架
     */
    public static void initAll() {
        initLogin();
        initController();
        initCommand();
        GlobalFilterInit();
        initLevel();
        isRunning = true;
        //加载插件
        try {
            PlugLoader loader = PlugLoader.getInstance();
            loader.setSingleLoader(true);
            loader.start();
        } catch (Exception e) {
            log.error("[测试功能] 无法加载插件", e);
        }
    }

    /**
     * 登录信息初始化
     */
    public static void initLogin() {
        //登录配置信息单例创建，并初始化
        LoginConfig.getInstance();
    }

    /**
     * 控制器初始化
     */
    public static void initController() {
        //控制器初始化
        ControllerAbs.init();
    }

    /**
     * 命令初始化
     */
    public static void initCommand() {
        //命令集合单例创建，并初始化
//        CommandSet.getInstance();
        CommandLoadInit.init();
    }

    /**
     * 权限初始化
     */
    public static void initLevel() {
        LevelConfig.LevelConfigInfo info = LevelConfig.getInstance().getCommandConfigInfo();
        FilterTable filterTable = FilterTable.getInstance();
        if (info.getSystems() != null) filterTable.getSystem().addAll(info.getSystems());
        if (info.getSystems() != null) for (LevelConfig.LevelConfigItem item : info.getLevels()) {
            Long groupid = item.getGroupid();
            if (item.getRoots() != null) for (Long id : item.getRoots()) {
                filterTable.setRoot(groupid, id);
            }
            if (item.getFirsts() != null) for (Long id : item.getFirsts()) {
                filterTable.setFirst(groupid, id);
            }
            if (item.getSeconds() != null) for (Long id : item.getSeconds()) {
                filterTable.setSecond(groupid, id);
            }
        }
    }

    /**
     * 全局过滤器初始化
     */
    public static void GlobalFilterInit() {
        GlobalFilterInitOrExecute.getInstance();
    }

    public static void runAndLogin() {
        runAndLogin(null);
    }


    /**
     * 运行框架并登录
     * @param configuration 登录用的配置文件
     */
    public static void runAndLogin(MiraiToolBotConfiguration configuration) {
        //初始化框架
        initAll();
        log.info("框架初始化完成");
        //登录
        LoginMirai.login(configuration);
    }

    /**
     * 运行框架并登录（通过二维码登录）
     * @param isQR 是否使用二维码登录
     */
    public static void runAndLogin(boolean isQR) {
        //初始化框架
        initAll();
        log.info("框架初始化完成");
        //登录
        LoginMirai.login(isQR);
    }

    /**
     * 运行框架并登录
     * @param id QQ号
     * @param password 密码
     */
    public static void runAndLogin(long id, String password) {
        //初始化框架
        initAll();
        log.info("框架初始化完成");
        //登录
        Login.login(id, password);
    }

    /**
     * 运行框架并登录
     * @param id QQ号
     * @param password 密码
     * @param botConfiguration 登录配置
     */
    public static void runAndLogin(long id, String password, MiraiToolBotConfiguration botConfiguration) {
        //初始化框架
        initAll();
        log.info("框架初始化完成");
        //登录
        Login.login(id, password, botConfiguration);
    }

}
