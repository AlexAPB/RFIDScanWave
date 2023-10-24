package com.fatec.rfidscanwave.util;

public class InterfaceCommand {
    private final int id;
    private final Command command;

    public InterfaceCommand(int id, Command command){
        this.id = id;
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public int getId() {
        return id;
    }

    public enum Command {
        DISPLAY_USER,
        ALREADY_CLOCKED,
        CANT_WORK,
        FORCE_CLOCK_OUT,
        WRONG_USER;
    }
}
