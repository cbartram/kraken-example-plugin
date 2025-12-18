package com.krakenplugins.example.script;

public interface Task {
    /**
     * Checks if this task should currently be executed.
     * @return true if the task is valid, false otherwise.
     */
    boolean validate();

    /**
     * Executes the task logic.
     * @return The number of milliseconds to sleep after execution.
     */
    int execute();

    /**
     * Returns the name of the status for display.
     * @return Status string.
     */
    String status();
}
