package pets.bayesianlogic.except;

import java.io.FileNotFoundException;

public class NullStringPathException extends FileNotFoundException {
    public NullStringPathException(String message) {
        super(message);
    }
}
