package Exceptions;

/**
 * Исключение, вызываемое, когда поле поиска пустое.
 */
public class EmptySearchException extends Exception {

    /**
     * Конструктор без параметров, создающий исключение с сообщением об ошибке пустого поля поиска.
     */
    public EmptySearchException() {
        super("Поле поиска не должно быть пустым");
    }
}
