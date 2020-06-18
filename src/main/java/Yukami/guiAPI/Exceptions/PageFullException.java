package Yukami.guiAPI.Exceptions;

public class PageFullException extends RuntimeException {

    public PageFullException() {
        super("There was an attempt to add an item to a page that is full!");
    }

}
