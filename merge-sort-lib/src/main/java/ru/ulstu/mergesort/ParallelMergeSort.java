package ru.ulstu.mergesort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ParallelMergeSort {

    private ParallelMergeSort() {}

    public static <T extends Comparable<? super T>> SortResult<T> sort(
            List<T> input, int threads) {

        long startNanos = System.nanoTime();

        if (input == null || input.isEmpty()) {
            return new SortResult<>(new ArrayList<>(), System.nanoTime() - startNanos);
        }
        if (input.size() == 1) {
            return new SortResult<>(new ArrayList<>(input), System.nanoTime() - startNanos);
        }

        int n = input.size();
        int effectiveThreads = Math.max(1, Math.min(threads, n));

        List<List<T>> chunks = split(input, effectiveThreads);

        ExecutorService executor = Executors.newFixedThreadPool(effectiveThreads);
        try {
            List<List<T>> sortedChunks = sortChunks(chunks, executor);

            while (sortedChunks.size() > 1) {
                sortedChunks = mergeRound(sortedChunks, executor);
            }

            long duration = System.nanoTime() - startNanos;
            return new SortResult<>(sortedChunks.get(0), duration);
        } finally {
            executor.shutdownNow();
        }
    }

    private static <T> List<List<T>> split(List<T> input, int parts) {
        int n = input.size();
        int base = n / parts;
        int remainder = n % parts;

        List<List<T>> chunks = new ArrayList<>(parts);
        int from = 0;
        for (int i = 0; i < parts; i++) {
            int size = base + (i < remainder ? 1 : 0);
            chunks.add(new ArrayList<>(input.subList(from, from + size)));
            from += size;
        }
        return chunks;
    }

    private static <T extends Comparable<? super T>> List<List<T>> sortChunks(
            List<List<T>> chunks, ExecutorService executor) {

        List<Future<List<T>>> futures = new ArrayList<>(chunks.size());
        for (List<T> chunk : chunks) {
            futures.add(executor.submit(() -> {
                List<T> copy = new ArrayList<>(chunk);
                copy.sort(Comparator.naturalOrder());
                return copy;
            }));
        }

        List<List<T>> result = new ArrayList<>(chunks.size());
        for (Future<List<T>> f : futures) {
            try {
                result.add(f.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Сортировка прервана", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Ошибка сортировки", e.getCause());
            }
        }
        return result;
    }

    private static <T extends Comparable<? super T>> List<List<T>> mergeRound(
            List<List<T>> chunks, ExecutorService executor) {

        List<Future<List<T>>> futures = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i += 2) {
            if (i + 1 < chunks.size()) {
                final List<T> left  = chunks.get(i);
                final List<T> right = chunks.get(i + 1);
                futures.add(executor.submit(() -> merge(left, right)));
            } else {
                final List<T> single = chunks.get(i);
                futures.add(executor.submit(() -> single));
            }
        }

        List<List<T>> result = new ArrayList<>();
        for (Future<List<T>> f : futures) {
            try {
                result.add(f.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Слияние прервано", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Ошибка слияния", e.getCause());
            }
        }
        return result;
    }

    private static <T extends Comparable<? super T>> List<T> merge(List<T> a, List<T> b) {
        List<T> result = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).compareTo(b.get(j)) <= 0) {
                result.add(a.get(i++));
            } else {
                result.add(b.get(j++));
            }
        }
        while (i < a.size()) result.add(a.get(i++));
        while (j < b.size()) result.add(b.get(j++));
        return result;
    }

    public record SortResult<T>(List<T> sorted, long durationNanos) {
        public long durationMillis()   { return durationNanos / 1_000_000; }
        public double durationSeconds(){ return durationNanos / 1_000_000_000.0; }
    }
}