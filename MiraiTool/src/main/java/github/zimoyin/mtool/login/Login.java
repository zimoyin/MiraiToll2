package github.zimoyin.mtool.login;

import github.zimoyin.mtool.config.MiraiToolBotConfiguration;
import github.zimoyin.mtool.event.BotExceptionEvent;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.network.WrongPasswordException;
import org.jetbrains.annotations.NotNull;


@Slf4j
public final class Login {

    /**
     * 登录机器人
     *
     * @param id            QQ ID
     * @param password      密码
     * @param configuration 登录配置文件
     */
    public static void login(long id, String password, MiraiToolBotConfiguration configuration) {
        if (configuration == null) {
            configuration = getConfiguration(false);
        }
        //初始化设备信息
        configuration.initialize(id);

        //创建机器人
        Bot bot = null;
        if (configuration.isQRLogin())
            bot = BotFactory.INSTANCE.newBot(id, new BotAuthorizationImp(password, true), configuration);
        else bot = BotFactory.INSTANCE.newBot(id, new BotAuthorizationImp(password, false), configuration);
        log.info("BOT CREATED: {}", id);
        try {
            //登录bot
            bot.login();
        } catch (WrongPasswordException e) {
            log.error("[严重]无法登录QQ", e);
            new BotExceptionEvent.BotLoginExceptionEvent(bot, e).broadcast();
        } catch (Exception e) {
            log.error("[严重]无法登录QQ", e);
            new BotExceptionEvent(bot, e).broadcast();
        }
    }

    public static void login(long id, String password) {
        login(id, password, null);
    }

    public static void login(long id, String password, boolean isQR) {
        login(id, password, getConfiguration(isQR));
    }

    @NotNull
    private static MiraiToolBotConfiguration getConfiguration(boolean isQR) {
        return new MiraiToolBotConfiguration() {
            @Override
            public void initAfter() {

            }

            @Override
            public void initBefore() {
                if (isQR) setQRLoginEnabled();
            }
        };
    }
}
