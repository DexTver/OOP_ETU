package Exceptions;

public class EmptySearchException extends Exception {
    public EmptySearchException() {
        super("Поле поиска не должно быть пустым");
    }
}
