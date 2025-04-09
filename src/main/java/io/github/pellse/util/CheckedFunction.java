package io.github.pellse.util;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T1, R, E extends Throwable> extends Function<T1, R> {

    R checkedApply(T1 t1) throws E;

    @Override
    default R apply(T1 t1) {
        try {
            return checkedApply(t1);
        } catch (Throwable e) {
            return sneakyThrow(e);
        }
    }

    static <T, R, E extends Throwable> Function<T, R> unchecked(CheckedFunction<T, R, E> checkedFunction) {
        return checkedFunction;
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Throwable> T sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
