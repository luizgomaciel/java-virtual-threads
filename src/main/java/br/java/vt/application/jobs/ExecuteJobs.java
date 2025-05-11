package br.java.vt.application.jobs;

import br.java.vt.application.service.ProcessService;

public class ExecuteJobs {

    public static void execute(){
        System.out.println("Start job");
        new ProcessService().runVirtualThreadProcess();
        System.out.println("Finish job");
    }
}
