package org.uzdiz.userInput;

public class CommandExecutor {
    private Command command;

    public CommandExecutor(Command command) {
        this.command = command;
    }

    public void executeCommand(String input) {
        this.command.execute(input);
    }
}
