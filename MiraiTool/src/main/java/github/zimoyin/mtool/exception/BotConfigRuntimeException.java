package github.zimoyin.mtool.exception;

public class BotConfigRuntimeException extends RuntimeException {
    public BotConfigRuntimeException() {
        super("配置bot失败");
    }

    public BotConfigRuntimeException(String message) {
        super(message);
    }

    public BotConfigRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BotConfigRuntimeException(Throwable cause) {
        super(cause);
    }

    protected BotConfigRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
