package br.java.vt.application.service;

import br.java.vt.infrastructure.DataEnum;
import br.java.vt.infrastructure.VTask;
import br.java.vt.model.Datas;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static br.java.vt.infrastructure.VTask.shutdown;
import static java.lang.Thread.sleep;

public class ProcessService {

    public void runVirtualThreadProcess() {
        VTask.of("Process started")
            .map(this::println)
            .flatMap(result -> {
                println("2nd step");
                return VTask.of("Process completed");
            })
            .sequential(result -> getTasks())
            .onErrorResume(error -> {
                println("Error: " + error.getMessage());
                return Collections.singletonList(VTask.of("Error handled"));
            })
            .map(result -> {
                println("3nd step");
                return VTask.of("Process completed");
            })
            .parallel(result -> getTasks())
            .onErrorResume(error -> {
                println("Error: " + error.getMessage());
                return Collections.singletonList(VTask.of("Error handled"));
            })
            .then();

        shutdown();
    }

    private Supplier<Object> getTask() {
        return () -> {
            try {
                int rv = randomMilli();
                System.out.println("getTask sleep: ".concat(String.valueOf(rv)));
                sleep(rv);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Datas(DataEnum.DATA_1.getValue(), random());
        };
    }

    private List<Supplier<Object>> getTasks() {
        return List.of(
            getTask(),
            () -> {
                int rv = randomMilli();
                try {
                    sleep(rv);
                    System.out.println("task DATA_2, sleep: ".concat(String.valueOf(rv)));
                    return DataEnum.DATA_2.getValue();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            },
            () -> {
                int rv = randomMilli();
                try {
                    String rt = random();
                    System.out.println("task: ".concat(rt).concat(", sleep: ".concat(String.valueOf(rv))));
                    sleep(rv);
                    return rt;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

    private String random() {
        return String.valueOf(new Random().nextInt(1000));
    }

    private String println(String txt) {
        System.out.println(txt);
        return txt;
    }

    private int randomMilli(){
        Random r = new Random();
        return r.nextInt(1000, 9000);
    }

}
