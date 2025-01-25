package org.uzdiz.command;

public interface Command {
    void execute();

    void undo();
}
