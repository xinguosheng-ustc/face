package com.cloud.Controll;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {

    private String processName;
    private List<Process> process = new ArrayList<Process>();
    private int processNum;
    public ProcessManager() {

    }
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public List<Process> getProcess() {
        return process;
    }

    public void setProcess(List<Process> process) {
        this.process = process;
    }

    public int getProcessNum() {
        return processNum;
    }

    public void setProcessNum(int processNum) {
        this.processNum = processNum;
    }
}
