package com.alpha.coding.common.log;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

/**
 * ExtraMsgSupplier
 *
 * @version 1.0
 * Date: 2020/5/14
 */
public interface ExtraMsgSupplier {

    /**
     * 辅助类
     */
    class Helper {
        /**
         * 当前正在使用的ExtraMsgSupplier
         */
        public static Optional<ExtraMsgSupplier> current() {
            return Optional.ofNullable((ExtraMsgSupplier) MapThreadLocalAdaptor.get("CURR_ExtraMsgSupplier"));
        }

        /**
         * 追加消息
         */
        public static void append(String msg) {
            current().ifPresent(p -> p.appender().accept(msg));
        }
    }

    /**
     * 附加消息提供函数
     */
    default Supplier<String> supplier() {
        return () -> null;
    }

    /**
     * 附加消息扩展函数
     */
    default Consumer<String> appender() {
        return s -> {
            // 扩展消息实现
        };
    }

}
