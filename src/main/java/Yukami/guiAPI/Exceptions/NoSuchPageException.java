package Yukami.guiAPI.Exceptions;

public class NoSuchPageException extends RuntimeException {

    public NoSuchPageException() {
        super("There was an attempt to access a page that doesn't exist!\n&cRemember that pages start at 1 (e.g. Page 1 is 1, Page 2 is 2, etc.) !");
    }

}
