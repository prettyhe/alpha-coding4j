package com.alpha.coding.bo.base;

import java.util.function.Supplier;

/**
 * CodeMsgSupplier
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface CodeMsgSupplier {

    class Factory {
        public static CodeMsgSupplier build(final Supplier<Integer> codeSupplier, final Supplier<String> msgSupplier) {
            return new CodeMsgSupplier() {
                @Override
                public Supplier<Integer> codeSupplier() {
                    return codeSupplier;
                }

                @Override
                public Supplier<String> msgSupplier() {
                    return msgSupplier;
                }
            };
        }

        public static CodeMsgSupplier build(final Integer code, final String msg) {
            return build(() -> code, () -> msg);
        }
    }

    Supplier<Integer> codeSupplier();

    Supplier<String> msgSupplier();

}
