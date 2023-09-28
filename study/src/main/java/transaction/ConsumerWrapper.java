package transaction;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsumerWrapper {

    private static final Logger log = LoggerFactory.getLogger(ConsumerWrapper.class);

    public static <T> Consumer<T> accept(ThrowingConsumer<T, Exception> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                log.error(e.getMessage(), e.getCause());
                throw new RuntimeException(e);
            }
        };
    }

    private ConsumerWrapper() {}
}
