package github.zimoyin.mtool.config.application;

public interface ApplicationType<T extends ApplicationType<T>> {
    public T parse(String value);
}
