package Yukami.guiAPI.Exceptions;

public class InvalidPlayerException extends RuntimeException {

    public InvalidPlayerException() {
        super("The provided Player is not a valid one! It has to be one of the players involved in the trade!");
    }

}
