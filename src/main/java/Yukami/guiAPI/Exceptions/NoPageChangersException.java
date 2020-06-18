package Yukami.guiAPI.Exceptions;

public class NoPageChangersException extends RuntimeException {

    public NoPageChangersException() {
        super("There was an attempt to access the Page Changers but they weren't set before!");
    }

}
