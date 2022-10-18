package com.alpha.coding.bo.enums.util;

/**
 * CodeSupplyEnum
 * 简化枚举类{@code valueOf(Object code)}方法和{@code getDescByCode(Object code)}实现
 * <pre> {@code
 * @Getter
 * @AllArgsConstructor
 * public enum MyEnum implements CodeSupplyEnum<MyEnum> {
 *     A(1, "11", "a"),
 *     B(2, "22", "b"),
 *     C(3, "33", "c");
 *
 *     private final int index;
 *     private final String code;
 *     private final String desc;
 *
 *     @Override
 *     public Supplier codeSupply() {
 *         return this::getIndex;
 *     }
 *
 *     @Override
 *     public Supplier descSupply() {
 *         return this::getDesc;
 *     }
 *
 *     public static MyEnum valueOf(int code) {
 *         return CodeSupplyEnum.valueOf(code);
 *     }
 *
 *     public static String getDescByCode(int code) {
 *         return CodeSupplyEnum.getDescByCodeDefault(code, "");
 *     }
 * }}</pre>
 *
 * @version 1.0
 * Date: 2021/11/29
 */
public interface CodeSupplyEnum<E extends Enum<E>> extends EnumWithCodeSupplier {

    @SuppressWarnings({"unchecked"})
    static <E extends Enum<E> & CodeSupplyEnum<E>> E valueOf(Object code) {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(CodeSupplyEnum.class.getName())
                    && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                Class<E> clz = null;
                try {
                    clz = (Class<E>) Class.forName(ste.getClassName());
                    return EnumUtils.valueOf(clz, code);
                } catch (ClassNotFoundException e) {
                    // nothing
                }
            }
        }
        return null;
    }

    static String getDescByCodeDefault(Object code, String defaultDesc) {
        final EnumWithCodeSupplier e = CodeSupplyEnum.valueOf(code);
        return e == null ? defaultDesc : e.descSupply().get();
    }

    static String getDescByCode(Object code) {
        return CodeSupplyEnum.getDescByCodeDefault(code, null);
    }

}
