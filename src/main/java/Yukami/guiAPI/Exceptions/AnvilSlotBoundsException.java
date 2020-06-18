package Yukami.guiAPI.Exceptions;

public class AnvilSlotBoundsException extends RuntimeException {

    public AnvilSlotBoundsException() {
        super("Slot cannot be less than 0 or higher than 2 in an AnvilWindow!");
    }

}
