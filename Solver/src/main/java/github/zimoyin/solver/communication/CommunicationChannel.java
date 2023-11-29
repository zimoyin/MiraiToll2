package github.zimoyin.solver.communication;

public interface CommunicationChannel<T> {
    T getValue();

    boolean setValue(T value);
}
