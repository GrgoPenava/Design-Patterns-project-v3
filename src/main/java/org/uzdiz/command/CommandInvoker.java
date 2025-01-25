package org.uzdiz.command;

import java.util.Stack;

public class CommandInvoker {
    private Stack<Command> commandHistory;

    public CommandInvoker() {
        this.commandHistory = new Stack<>();
    }

    public void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        } else {
            System.out.println("Nema komandi za poništavanje.");
        }
    }

    public void clearHistory() {
        commandHistory.clear();
    }
}
