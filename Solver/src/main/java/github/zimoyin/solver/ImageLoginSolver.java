package github.zimoyin.solver;

import github.zimoyin.solver.communication.CommunicationChannelOfTicket;
import github.zimoyin.solver.communication.CommunicationChannelOfURL;
import kotlin.coroutines.Continuation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.DeviceVerificationRequests;
import net.mamoe.mirai.utils.DeviceVerificationResult;
import net.mamoe.mirai.utils.LoginSolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 无短信验证
 */
@Slf4j
@Data
@Deprecated
public class ImageLoginSolver extends LoginSolver {

    /**
     * 处理图片验证码, 返回图片验证码内容.
     * <p>
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * <p>
     * ## 异常类型
     * <p>
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     * <p>
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     */
    @Nullable
    @Override
    public String onSolvePicCaptcha(@NotNull Bot bot, @NotNull byte[] bytes, @NotNull Continuation<? super String> continuation) {
        log.error("onSolvePicCaptcha is not implemented");
        return null;
    }

    /**
     * 处理滑动验证码.
     * <p>
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * <p>
     * ## 异常类型
     * <p>
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     * <p>
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @return 验证码解决成功后获得的 ticket.
     */
    @Nullable
    @Override
    public String onSolveSliderCaptcha(@NotNull Bot bot,
                                       @NotNull String url,
                                       @NotNull Continuation<? super String> continuation) {
        log.info("[系统日志] 登录器实现类正在处理验证码滑块验证");
        log.info("[系统日志] 滑块验证地址: {}", url);
        TypeSelect.open();
        CommunicationChannelOfURL.getInstance().setValue(url);
        return CommunicationChannelOfTicket.getInstance().getValue();
    }

    /**
     * 处理不安全设备验证.
     * <p>
     * 返回值保留给将来使用. 目前在处理完成后返回任意内容 (包含 `null`) 均视为处理成功.
     * <p>
     * ## 异常类型
     * <p>
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     * <p>
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @return 任意内容. 返回值保留以供未来更新.
     */
    @Nullable
    @Override
    public String onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String url, @NotNull Continuation<? super String> continuation) {
        TypeSelect.open();
        CommunicationChannelOfURL.getInstance().setValue(url);
        return CommunicationChannelOfTicket.getInstance().getValue();
    }

    /**
     * 防止服务器发送 {当前QQ版本过低，请升级至最新版本后再登录}
     *
     * @return
     */
    @Override
    public boolean isSliderCaptchaSupported() {
        return true;
    }


    /**
     * 处理设备验证.
     * <p>
     * ## 异常类型
     * <p>
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     * <p>
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @since 验证结果, 可通过解决 [DeviceVerificationRequests] 获得.
     * @since 2.13
     */
    @Nullable
    @Override
    public Object onSolveDeviceVerification(@NotNull Bot bot,
                                            @NotNull DeviceVerificationRequests requests,
                                            @NotNull Continuation<? super DeviceVerificationResult> $completion) {
        log.error("onSolveDeviceVerification is not override");
        log.error("不支持短信登录,使用其他方式登录，请验证");
        TypeSelect.open();
        /**
         * 其他验证方式. 在不为 `null` 时表示支持该验证方式.
         */
        DeviceVerificationRequests.FallbackRequest fallback = requests.getFallback();
        String url = fallback.getUrl();//HTTP URL. 可能需要在 QQ 浏览器中打开并人工操作.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        DeviceVerificationResult solved1 = fallback.solved();//通知此请求已被解决. 获取 [DeviceVerificationResult] 用于返回 [LoginSolver.onSolveDeviceVerification].
        log.info("fallback url: {}", url);
        log.info("fallback solved1: {}", solved1);

        return super.onSolveDeviceVerification(bot, requests, $completion);
    }


}
