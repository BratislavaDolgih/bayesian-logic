package pets.bayesianlogic.except;

import java.io.IOException;

/**
 * Класс, расширяющий класс исключений <body>Exception</body>.
 * Реализует объект исключений в случае неверного ввода чисел для вероятности (меньше 0, больше 1).
 * @author Stephan Kolesnikov
 */
public class ProbabilityInputException extends IOException {
    public ProbabilityInputException(String message) {
        super(message);
    }
}
