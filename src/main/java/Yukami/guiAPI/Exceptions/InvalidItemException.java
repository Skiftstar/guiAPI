package Yukami.guiAPI.Exceptions;

public class InvalidItemException extends RuntimeException {

    public InvalidItemException() {
        super("The trade items of the provided player does not contain the provided item!");
    }

}
