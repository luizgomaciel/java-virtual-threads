package br.java.vt.application.service;

import br.java.vt.infrastructure.DataEnum;
import br.java.vt.infrastructure.VTask;
import br.java.vt.model.Datas;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class ProcessService {

    public void runVirtualThreadProcess() {
        VTask.of("Process started")
            .map(this::println)
            .flatMap(result -> {
                println(result.concat(" 2nd step"));
                return VTask.of("Process completed");
            })
            .sequential(result -> getTasks())
            .onErrorResume(error -> {
                println("Error: " + error.getMessage());
                return Collections.singletonList(VTask.of("Error handled"));
            })
            .parallel(result -> getTasks())
            .onErrorResume(error -> {
                println("Error: " + error.getMessage());
                return Collections.singletonList(VTask.of("Error handled"));
            })
            .then();
    }

    private Supplier<Object> getTask() {
        return () -> {
            try {
                Random r = new Random();
                int rv = r.nextInt(10);

                System.out.println("task sleep: ".concat(String.valueOf(rv)));
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
                System.out.println("task DATA_2");
                return DataEnum.DATA_2.getValue();
            },
            () -> {
                String rt = random();
                System.out.println("task: ".concat(rt));
                return rt;
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

}
