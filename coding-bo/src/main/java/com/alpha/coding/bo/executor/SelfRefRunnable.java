package com.alpha.coding.bo.executor;

import java.util.function.Consumer;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SelfRefRunnable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
public class SelfRefRunnable implements Runnable {

    private volatile Consumer<SelfRefRunnable> command;

    public SelfRefRunnable(Consumer<SelfRefRunnable> command) {
        this.command = command;
    }

    @Override
    public void run() {
        this.command.accept(this);
    }
}
