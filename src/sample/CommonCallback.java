package sample;

public interface CommonCallback<T, D> {
    T call(D event);
}
