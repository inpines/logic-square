package org.dotspace.oofp.utils.functional.monad;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sequence<T> implements Foldable<T> {

    private final Supplier<Stream<T>> supplier;

    public static <T> Sequence<T> of(T[] values) {
        return new Sequence<>(() -> Arrays.stream(values));
    }

    public static <T> Sequence<T> from(Collection<T> collection) {
        return new Sequence<>(collection::stream);
    }

    public static <T> Sequence<Pair<String, T>> from(Map<String, T> keyValues) {
        return new Sequence<>(() -> keyValues.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue())));
    }

    public static <T> Sequence<T> empty() {
        return new Sequence<>(Stream::empty);
    }

    public static <T, S> Sequence<T> unfold(
            S seed, Function<S, Maybe<Pair<T, S>>> generator) {

        return new Sequence<>(() -> StreamSupport.stream(
                new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, 0) {

                    private Maybe<Pair<T, S>> current = generator.apply(seed);

                    @Override
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if (current.isEmpty()) {
                            return false;
                        }

                        Pair<T, S> pair = current.get();
                        action.accept(pair.getLeft());
                        current = generator.apply(pair.getRight());
                        return true;
                    }
                }, false));
    }

//    @Override
    public <R> Sequence<R> flatMap(Function<T, ? extends Sequence<R>> mapper) {
        return new Sequence<>(() -> supplier.get()
                .flatMap(t -> mapper.apply(t).stream()));
    }

//    @Override
    public <R> Sequence<R> map(Function<T, ? extends R> mapper) {
        return new Sequence<>(() -> supplier.get().map(mapper));
    }

    public Sequence<T> filter(Predicate<? super T> predicate) {
        return new Sequence<>(() -> supplier.get().filter(predicate));
    }

    public Sequence<T> distinct() {
        return new Sequence<>(() -> stream().distinct());
    }

    public Sequence<T> sorted() {
        return new Sequence<>(() -> stream().sorted());
    }

    public Sequence<T> sorted(Comparator<? super T> comparator) {
        return new Sequence<>(() -> stream().sorted(comparator));
    }

    public Map<Boolean, List<T>> partitionBy(Predicate<? super T> predicate) {
        try (Stream<T> s = stream()) {
            return s.collect(Collectors.partitioningBy(predicate));
        }
    }

    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        try (Stream<T> s = stream()) {
            return s.collect(Collectors.groupingBy(classifier));
        }
    }

    public <K, M> Map<K, M> groupBy(
            Function<? super T, ? extends K> classifier, Collector<T, ?, M> downstream) {
        try (Stream<T> s = stream()) {
            return s.collect(Collectors.groupingBy(classifier, downstream));
        }
    }

    @Override
    public <R> R fold(R identity, BiFunction<R, ? super T, R> reducer) {
        R result = identity;
        try (Stream<T> s = stream()) {
            Iterator<T> it = s.iterator();
            while (it.hasNext()) {
                result = reducer.apply(result, it.next());
            }
        }
        return result;
    }

    @Override
    public Stream<T> stream() {
        return supplier.get();
    }

    public <U, V> Sequence<V> zip(Sequence<U> other, BiFunction<T, U, V> zipper) {
        return new Sequence<>(() -> {
            List<T> list1 = this.stream().toList();
            List<U> list2 = other.stream().toList();
            int size = Math.min(list1.size(), list2.size());

            return IntStream.range(0, size)
                    .mapToObj(i -> zipper.apply(list1.get(i), list2.get(i)));
        });
    }

    public <K, V> Map<K, V> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
        try (Stream<T> s = stream()) {
            return s.collect(Collectors.toMap(keyMapper, valueMapper));
        }
    }

    public Stream<T> unwrap() {
        return stream();
    }

    public int size() {
        try (Stream<T> s = stream()) {
            // 這是 O(n)：lazy 模式下沒辦法 O(1) 拿 size
            return (int) s.count();
        }
    }

    public Sequence<T> peek(Consumer<T> consumer) {
        return new Sequence<>(() -> stream().peek(consumer)); // side effect
    }

}
