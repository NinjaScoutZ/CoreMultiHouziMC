package com.houzicore.extension.execution.scheduler;

import com.houzicore.extension.exception.SchedulerTaskException;

@FunctionalInterface
public interface SchedulerRunnable {

    void run() throws SchedulerTaskException;

}
