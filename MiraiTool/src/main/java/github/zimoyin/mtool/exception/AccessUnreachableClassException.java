package github.zimoyin.mtool.exception;

public class AccessUnreachableClassException extends RuntimeException {
    public AccessUnreachableClassException(String name, Exception e) {
        super(name, e);
    }

    public AccessUnreachableClassException(String cls) {
        super(cls);
    }
}
