package org.dotspace.oofp.utils.functional.monad;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sequence<T> implements Foldable<T> {

    protected Collection<T> collection;

    public static <T> Sequence<T> of(T[] values) {
        return new Sequence<>(Arrays.stream(values).toList());
    }

    public static <T> Sequence<T> from(Collection<T> collection) {
        return new Sequence<>(collection);
    }

    public static <T> Sequence<Pair<String, T>> from(Map<String, T> keyValues) {
        List<Pair<String, T>> keyValueList = keyValues.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .toList();

        return new Sequence<>(keyValueList);
    }

    public static <T> Sequence<T> empty() {
        return new Sequence<>(Collections.emptyList());
    }

    public static <T, S> Sequence<T> unfold(S seed, Function<S, Optional<Pair<T, S>>> generator) {
        List<T> result = new ArrayList<>();
        Optional<Pair<T, S>> current = generator.apply(seed);
        while (current.isPresent()) {
            Pair<T, S> pair = current.get();
            result.add(pair.getLeft());
            current = generator.apply(pair.getRight());
        }
        return new Sequence<>(result);
    }

//    @Override
    public <R> Sequence<R> flatMap(Function<T, ? extends Sequence<R>> function) {
        return new Sequence<>(collection.stream()
                .flatMap(t -> getCollection(function, t))
                .toList());
    }

    private <R> Stream<R> getCollection(Function<T, ? extends Sequence<R>> function, T value) {
        var sequence = (Sequence<R>) function.apply(value);
        return sequence.collection.stream();
    }

//    @Override
    public <R> Sequence<R> map(Function<T, ? extends R> mapper) {
        return Sequence.from(collection.stream()
                .<R>map(mapper)
                .toList());
    }

    public Sequence<T> filter(Predicate<? super T> predicate) {
        return new Sequence<>(collection.stream()
                .filter(predicate)
                .toList());
    }

    public Sequence<T> distinct() {
        return new Sequence<>(collection.stream().distinct().toList());
    }

    public Sequence<T> sorted() {
        return new Sequence<>(collection.stream()
                .sorted()
                .toList());
    }

    public Sequence<T> sorted(Comparator<? super T> comparator) {
        return new Sequence<>(collection.stream()
                .sorted(comparator)
                .toList());
    }

    public Map<Boolean, List<T>> partitionBy(Predicate<? super T> predicate) {
        return collection.stream().collect(Collectors.partitioningBy(predicate));
    }

    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        return collection.stream().collect(Collectors.groupingBy(classifier));
    }

    public <K, M> Map<K, M> groupBy(
            Function<? super T, ? extends K> classifier, Collector<T, ?, M> downstream) {
        return collection.stream().collect(Collectors.groupingBy(classifier, downstream));
    }

    @Override
    public <R> R fold(R identity, BiFunction<R, ? super T, R> reducer) {
        R result = identity;
        for (T item: collection) {
            result = reducer.apply(result, item);
        }
        return result;
    }

    @Override
    public Stream<T> stream() {
        return unwrap();
    }

    public <U, V> Sequence<V> zip(Sequence<U> other, BiFunction<T, U, V> zipper) {
        List<T> list1 = this.collection instanceof List<T> list ?  list : new ArrayList<>(this.collection);
        List<U> list2 = other.collection instanceof List<U> list ? list : new ArrayList<>(other.collection);
        int size = Math.min(list1.size(), list2.size());
        List<V> result = IntStream.range(0, size)
                .mapToObj(i -> zipper.apply(list1.get(i), list2.get(i)))
                .toList();
        return new Sequence<>(result);
    }

    public <K, V> Map<K, V> toMap(Function<T, K> classifier, Function<T, V> mapper) {
        return unwrap().collect(Collectors.toMap(classifier, mapper));
    }

    public Stream<T> unwrap() {
        return collection.stream();
    }

    public int size() {
        return collection.size();
    }
}
