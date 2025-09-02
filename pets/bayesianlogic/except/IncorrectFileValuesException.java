package pets.bayesianlogic.except;

import java.io.FileNotFoundException;

public class IncorrectFileValuesException extends FileNotFoundException {
    public IncorrectFileValuesException(String message) {
        super(message);
    }
}
