package Yukami.guiAPI.Exceptions;

public class SlotOutOfBoundsException extends RuntimeException {

    public SlotOutOfBoundsException() {
        super("There was an attempt to access a slot which is out of bounds!");
    }

}
