package github.zimoyin.mtool.login;

import kotlin.coroutines.Continuation;
import net.mamoe.mirai.auth.BotAuthInfo;
import net.mamoe.mirai.auth.BotAuthResult;
import net.mamoe.mirai.auth.BotAuthSession;
import net.mamoe.mirai.auth.BotAuthorization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotAuthorizationImp implements BotAuthorization {
    private final String PSW;
    private final boolean isQR;

    public BotAuthorizationImp(String psw, boolean isQR) {
        PSW = psw;
        this.isQR = isQR;
    }

    @Nullable
    @Override
    public Object authorize(@NotNull BotAuthSession botAuthSession, @NotNull BotAuthInfo botAuthInfo, @NotNull Continuation<? super BotAuthResult> continuation) {
        try {
            if (isQR) return botAuthSession.authByQRCode(continuation);
            else return botAuthSession.authByPassword(PSW, continuation);
        } catch (Exception e) {
            return botAuthSession.authByPassword(PSW, continuation);
        }
    }
}
