package at.fhv.productservice.messaging;

import java.util.function.Consumer;

public interface OrderEventConsumer<T> {
    Consumer<T> handle();
    void validate(T event);
}