package github.zimoyin.mtool.run;

import github.zimoyin.mtool.config.MiraiToolBotConfiguration;
import github.zimoyin.mtool.config.global.LoginConfig;
import github.zimoyin.mtool.login.Login;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录Mirai
 */
@Slf4j
public final class LoginMirai {
    /**
     * 登录配置文件中定义的账号与密码
     */
    public static void login(MiraiToolBotConfiguration configuration) {
        try {
            LoginConfig.getInstance().getLoginInfo().getUser().forEach((name, user) -> {
                log.info("正在登录：{}({})", name, user.getId());
                Login.login(user.getId(), user.getPassword(), configuration);
                System.out.println("============================================================================================================");
            });
        } catch (Exception e) {
            log.error("通过配置文件中账号信息登录QQ失败");
        }
    }

    /**
     * 登录配置文件中定义的账号与密码
     */
    public static void login(boolean isQR) {
        try {
            LoginConfig.getInstance().getLoginInfo().getUser().forEach((name, user) -> {
                log.info("正在登录：{}({})", name, user.getId());
                Login.login(user.getId(), user.getPassword(), isQR);
                System.out.println("============================================================================================================");
            });
        } catch (Exception e) {
            log.error("通过配置文件中账号信息登录QQ失败");
        }
    }
}
