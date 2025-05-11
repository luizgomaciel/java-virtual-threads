package br.java.vt.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;


public class VTask<T> {

    private final CompletableFuture<T> future;
    private static final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    private VTask(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> VTask<T> of(T value) {
        return new VTask<>(CompletableFuture.completedFuture(value));
    }

    public <U> VTask<U> map(Function<? super T, ? extends U> mapper) {
        return new VTask<>(future.thenApplyAsync(mapper, executor));
    }

    public <U> VTask<U> map(Function<? super T, ? extends U> mapper, Function<Throwable, ? extends U> onError) {
        CompletableFuture<U> handled = future.handleAsync((value, ex) -> {
            if (ex == null) {
                return mapper.apply(value);
            } else {
                return onError.apply(ex);
            }
        }, executor);
        return new VTask<>(handled);
    }

    public <U> VTask<U> flatMap(Function<? super T, VTask<U>> mapper) {
        return new VTask<>(future.thenComposeAsync(t -> mapper.apply(t).future, executor));
    }

    public <U> VTask<List<U>> parallel(Function<? super T, List<Supplier<U>>> tasksMapper) {
        return flatMap(t -> {
            List<Supplier<U>> suppliers = tasksMapper.apply(t);
            List<CompletableFuture<U>> futures = suppliers.stream()
                    .map(s -> CompletableFuture.supplyAsync(s, executor))
                    .toList();
            CompletableFuture<List<U>> all = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .toList());
            return new VTask<>(all);
        });
    }

    public <U> VTask<List<U>> sequential(Function<? super T, List<Supplier<U>>> tasksMapper) {
        return flatMap(t -> {
            Supplier<List<U>> seqSupplier = () -> {
                List<U> results = new ArrayList<>();
                for (Supplier<U> supp : tasksMapper.apply(t)) {
                    results.add(supp.get());
                }
                return results;
            };
            return new VTask<>(CompletableFuture.supplyAsync(seqSupplier, executor));
        });
    }

    public VTask<T> onErrorResume(Function<Throwable, ? extends T> fn) {
        return new VTask<>(future.exceptionally(fn));
    }

    public void then() {
        future.join();
    }

    public T result() {
        return future.join();
    }

    public static void shutdown() {
        executor.shutdown();
    }

}
