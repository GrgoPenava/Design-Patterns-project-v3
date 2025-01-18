package org.uzdiz.userInput;

import org.uzdiz.ConfigManager;

public abstract class CommandHandlerChain {
    protected CommandHandlerChain nextHandler;

    public void setNextHandler(CommandHandlerChain nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handleCommand(String input) {
        if (canHandle(input)) {
            execute(input);
        } else if (nextHandler != null) {
            nextHandler.handleCommand(input);
        } else {
            ConfigManager.getInstance().incrementErrorCount();
            System.out.println("Greška br. " + ConfigManager.getInstance().getErrorCount() +
                    ": Upisali ste naredbu koja ne postoji -> "+ input);
        }
    }

    protected abstract boolean canHandle(String input);

    protected abstract void execute(String input);
}
