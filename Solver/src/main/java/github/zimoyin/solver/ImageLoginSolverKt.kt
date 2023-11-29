package github.zimoyin.solver

import github.zimoyin.solver.communication.CommunicationChannelOfTest
import github.zimoyin.solver.communication.CommunicationChannelOfTicket
import github.zimoyin.solver.communication.CommunicationChannelOfURL
import net.mamoe.mirai.Bot
import net.mamoe.mirai.auth.QRCodeLoginListener
import net.mamoe.mirai.utils.DeviceVerificationRequests
import net.mamoe.mirai.utils.DeviceVerificationResult
import net.mamoe.mirai.utils.LoginSolver
import org.slf4j.LoggerFactory

/**
 * 支持短信验证码登录（注意仅支持一次）
 */
class ImageLoginSolverKt : LoginSolver() {
    private val logger = LoggerFactory.getLogger(ImageLoginSolverKt::class.java)


    /**
     * 处理图片验证码, 返回图片验证码内容.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     *
     * ## 异常类型
     *
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     *
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @throws LoginFailedException
     */
    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        logger.warn("onSolvePicCaptcha(处理图片验证码)： 未能实现")
        return null
    }


    /**
     * 处理滑动验证码.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     *
     * ## 异常类型
     *
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     *
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @throws LoginFailedException
     * @return 验证码解决成功后获得的 ticket.
     */
    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        logger.info("onSolveSliderCaptcha(处理滑动验证码) url: $url")
        TypeSelect.open()
        CommunicationChannelOfURL.getInstance().value = url
        return CommunicationChannelOfTicket.getInstance().value
    }


    /**
     * 为 `true` 表示支持滑动验证码, 遇到滑动验证码时 mirai 会请求 [onSolveSliderCaptcha].
     * 否则会跳过滑动验证码并告诉服务器此客户端不支持, 有可能导致登录失败
     */
    override val isSliderCaptchaSupported: Boolean get() = true


    /**
     * 处理设备验证. 通常需要覆盖此函数. 此函数为 `open` 是为了兼容旧代码 (2.13 以前).
     *
     * 设备验证的类型可在 [DeviceVerificationRequests] 查看.
     *
     * ## 异常类型
     *
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     *
     * 抛出任意其他 [Throwable] 将视为验证码解决器的自身错误.
     *
     * @return 验证结果, 可通过解决 [DeviceVerificationRequests] 获得.
     * @throws LoginFailedException
     * @since 2.13
     */
    override suspend fun onSolveDeviceVerification(
        bot: Bot,
        requests: DeviceVerificationRequests,
    ): DeviceVerificationResult {
        logger.info("短信登录逻辑被调用")
        TypeSelect.open()
        var solved: DeviceVerificationResult?

        //服务器要求使用短信验证码
        val preferSms = requests.preferSms
        logger.info("服务器是否要求短信验证码 {}", preferSms)

        //其他验证方式：当短信验证码报错时启用
        val fallback = requests.fallback

        //正常流程： 优先使用短信验证码，如果在规定时间内或服务器拒绝验证码则使用其他验证方式
        logger.info("将发送一个验证码短信至 +${requests.sms?.countryCode} ${requests.sms?.phoneNumber}")
        try {
            requests.sms?.requestSms()
            //阻塞，等待验证码被获取，或者超时
            val value = CommunicationChannelOfTest.getInstance().value
            if (value == null || value.length != 6) {
                logger.error("验证码格式错误")
            }
            solved = requests.sms?.solved(value)
        } catch (e: Exception) {
            //其他验证方式：当短信验证码报错时启用
            CommunicationChannelOfURL.getInstance().setValue(fallback?.url)

            logger.error("短信发送失败", e)
            logger.warn("请使用其他方式登录,登录完成后请点击\"我已经完成了验证\"")

            //阻塞
            logger.info("CommunicationChannelOfTicket.valur: {}", CommunicationChannelOfTicket.getInstance().value)
            //通知此请求已被解决
            solved = fallback?.solved()
        }

        logger.info("solved: {}", solved)
        return solved ?: super.onSolveDeviceVerification(bot, requests)
    }

    /**
     * 二维码登录
     */
    override fun createQRCodeLoginListener(bot: Bot): QRCodeLoginListener {
        return QRListenerToURL()
    }
}