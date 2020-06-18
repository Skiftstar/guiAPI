package Yukami.guiAPI.Exceptions;

public class PagesNotEnabledException extends RuntimeException {

    public PagesNotEnabledException() {
        super("There was an attempt to access a method related to Pages but Pages aren't enabled!");
    }

}
