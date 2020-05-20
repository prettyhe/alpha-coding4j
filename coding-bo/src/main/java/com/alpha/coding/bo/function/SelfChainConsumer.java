package com.alpha.coding.bo.function;

import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

/**
 * SelfChainConsumer
 *
 * @version 1.0
 * Date: 2020-03-10
 */
public interface SelfChainConsumer<T extends SelfChainConsumer<T>> {

    default T inChain(@NotNull Consumer<T> consumer) {
        consumer.accept((T) this);
        return (T) this;
    }

}
