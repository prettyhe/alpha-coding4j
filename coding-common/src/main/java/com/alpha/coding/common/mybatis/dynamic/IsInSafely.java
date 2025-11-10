package com.alpha.coding.common.mybatis.dynamic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.condition.IsIn;

/**
 * IsInSafely 当集合为空时，根据模式选择忽略条件还是阻断条件，默认阻断
 *
 * @version 1.0
 * @date 2025年04月16日
 */
public class IsInSafely<T> extends IsIn<T> {

    private static final IsInSafely<?> EMPTY_IGNORE = new IsInSafely<>(EmptyStrategy.IGNORE, Collections.emptyList());
    private static final IsInSafely<?> EMPTY_BLOCK = new IsInSafely<>(EmptyStrategy.BLOCK, Collections.emptyList());

    /**
     * 空值处理策略
     */
    public enum EmptyStrategy {
        IGNORE, BLOCK
    }

    /**
     * 集合为空时处理策略，默认阻断
     */
    private final EmptyStrategy emptyStrategy;

    /**
     * 空条件
     *
     * @param emptyStrategy 集合为空时选择忽略条件(IGNORE)还是阻断条件(BLOCK)，默认阻断
     */
    @SuppressWarnings("unchecked")
    public static <T> IsInSafely<T> empty(EmptyStrategy emptyStrategy) {
        return (IsInSafely<T>) (Objects.requireNonNull(emptyStrategy) == EmptyStrategy.IGNORE ? EMPTY_IGNORE
                                        : EMPTY_BLOCK);
    }

    /**
     * 空条件：集合为空时阻断条件
     */
    public static <T> IsInSafely<T> empty() {
        return empty(EmptyStrategy.BLOCK);
    }

    protected IsInSafely(EmptyStrategy emptyStrategy, Collection<T> values) {
        super(Optional.ofNullable(values).orElse(Collections.emptyList()));
        this.emptyStrategy = Objects.requireNonNull(emptyStrategy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R accept(ConditionVisitor<T, R> visitor) {
        final R visit = visitor.visit(this);
        if (values.isEmpty() && visit instanceof FragmentAndParameters) {
            // 值为空时，忽略右值
            FragmentAndParameters origin = (FragmentAndParameters) visit;
            return (R) FragmentAndParameters.withFragment("")
                    .withParameters(origin.parameters())
                    .build();
        }
        return visit;
    }

    @Override
    public IsInSafely<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, this.emptyStrategy);
    }

    /**
     * 过滤集合元素，并产生新的条件
     *
     * @param predicate     条件
     * @param emptyStrategy 集合为空时选择忽略条件(IGNORE)还是阻断条件(BLOCK)，默认阻断
     */
    public IsInSafely<T> filter(Predicate<? super T> predicate, EmptyStrategy emptyStrategy) {
        return filterSupport(predicate, collection -> of(emptyStrategy, collection), this,
                () -> empty(emptyStrategy));
    }

    /**
     * If not empty, apply the mapping to each value in the list return a new condition with the mapped values.
     * Else return an empty condition (this).
     *
     * @param mapper a mapping function to apply to the values, if not empty
     * @param <R>    type of the new condition
     * @return a new condition with mapped values if renderable, otherwise an empty condition
     */
    @Override
    public <R> IsInSafely<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, this.emptyStrategy);
    }

    /**
     * If not empty, apply the mapping to each value in the list return a new condition with the mapped values.
     * Else return an empty condition (this).
     *
     * @param mapper        a mapping function to apply to the values, if not empty
     * @param <R>           type of the new condition
     * @param emptyStrategy 集合为空时选择忽略条件(IGNORE)还是阻断条件(BLOCK)，默认阻断
     * @return a new condition with mapped values if renderable, otherwise an empty condition
     */
    public <R> IsInSafely<R> map(Function<? super T, ? extends R> mapper, EmptyStrategy emptyStrategy) {
        Function<Collection<R>, IsInSafely<R>> constructor = collection -> of(emptyStrategy, collection);
        return mapSupport(mapper, constructor, () -> empty(emptyStrategy));
    }

    /**
     * 静态构造
     *
     * @param emptyStrategy 集合为空时选择忽略条件(IGNORE)还是阻断条件(BLOCK)，默认阻断
     * @param values        集合值
     */
    @SafeVarargs
    public static <T> IsInSafely<T> of(EmptyStrategy emptyStrategy, T... values) {
        return of(emptyStrategy, Arrays.asList(values));
    }

    /**
     * 静态构造
     *
     * @param emptyStrategy 集合为空时选择忽略条件(IGNORE)还是阻断条件(BLOCK)，默认阻断
     * @param values        集合
     */
    public static <T> IsInSafely<T> of(EmptyStrategy emptyStrategy, Collection<T> values) {
        return new IsInSafely<>(emptyStrategy, values);
    }

    /**
     * 静态构造，集合为空时阻断条件
     *
     * @param values 集合值
     */
    @SafeVarargs
    public static <T> IsInSafely<T> of(T... values) {
        return of(EmptyStrategy.BLOCK, Arrays.asList(values));
    }

    /**
     * 静态构造，集合为空时阻断条件
     *
     * @param values 集合
     */
    public static <T> IsInSafely<T> of(Collection<T> values) {
        return new IsInSafely<>(EmptyStrategy.BLOCK, values);
    }

    @Override
    public String overrideRenderedLeftColumn(String renderedLeftColumn) {
        if (values.isEmpty()) {
            // 忽略空值时，左值处理为恒定通过; 阻断空值时，左值处理为恒定不通过
            return emptyStrategy == EmptyStrategy.IGNORE ? "1 = 1" : "1 <> 1";
        }
        return super.overrideRenderedLeftColumn(renderedLeftColumn);
    }

}
