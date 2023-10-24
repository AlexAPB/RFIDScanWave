package com.fatec.rfidscanwave.util;

public class RFIDCommand {
    private final int id;
    private final int timesToCheck;
    private int checked = 0;
    private final Command command;

    public RFIDCommand(int id, Command command){
        this.id = id;
        this.command = command;
        this.timesToCheck = Command.getTimesToCheck(command);
    }

    public void appendChecked(){
        checked++;
    }

    public int getChecked() {
        return checked;
    }

    public int remainTimes(){
        return timesToCheck - checked;
    }

    public int getId() {
        return id;
    }

    public Command getCommand() {
        return command;
    }

    public int getTimesToCheck() {
        return timesToCheck;
    }

    public enum Command {
        CLOCK_OUT;

        public static int getTimesToCheck(Command command){
            return switch (command){
                case CLOCK_OUT -> 2;
                default -> 100;
            };
        }
    }
}
