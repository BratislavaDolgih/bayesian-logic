package pets.bayesianlogic.legacy;

import pets.bayesianlogic.except.IncorrectFileValuesException;
import pets.bayesianlogic.except.NullStringPathException;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * This class contains absolutely all obsolete utilities,
 * methods that have either been reincarnated into other more powerful ones, or have become obsolete and unusable.
 * Created solely for informational purposes, it is not recommended to use it IN ANY CASE.
 * For safety, the class is declared final.
 * The class contains fields from the main class {@code BayesianBody}, but they are there only to avoid compiler swearing.
 * @author Stephan Kolesnikov
 */
public final class OldBayesianUtils {
    /**
     * A field showing the numerical equivalent of the number of facts.
     */
    private int factsCount = 0;

    /**
     * A field that describes a list of fact-strings loaded from a file or entered using the keyboard.
     */
    private final ArrayList<String> facts = new ArrayList<>();

    /**
     * A field describing a probability table for the relationship [hypothesis][fact].
     */
    private double[][] tableOfProbabilities = null;

    /**
     * A private static variable that will be needed to correctly
     * and beautifully display the position on the screen.
     */
    private static final int[] countHypos = { 1 };

    /**
     * A field showing the numerical equivalent of the number of hypotheses.
     */
    private int hyposCount = 0;

    /**
     * A field describing the number of hypotheses in the ArrayList format.
     */
    private final ArrayList<String> hypotheses = new ArrayList<>();

    /**
     * A field describing the probabilities of each hypothesis (required for multiplication).
     */
    private final ArrayList<Double> hyposChances = new ArrayList<>();

    /**
     * Field-path to the file with calculating data.<br>
     * Enter the correct <code>Path</code> (for correct format call the <code>Paths.get(...)</code>)
     */
    private Path fileDataPath = null;

    /**
     * Field-directory to the file with calculating data. May be null since there is not the only way to enter data.
     */
    @Deprecated
    private String fileDataDirectory = null;

    protected boolean directoryIsNull() {
        return this.fileDataPath == null;
    }

    // When organizing the code, the purpose of this method was forgotten.
    @Deprecated
    private void validateConsistency(int localHypos, int localFacts) throws IncorrectFileValuesException {
        if (localHypos != this.hyposCount) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException(
                            hyposCount + " hypotheses expected, but got " + localHypos);
            throw e;
        }
        if (localHypos != this.hyposChances.size()) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException(
                            "[ATTENTION] Hypotheses count != chances count");
            throw e;
        }
        if (localFacts != this.factsCount) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException(
                            factsCount + " facts expected, but got " + localFacts);
            throw e;
        }

        this.tableOfProbabilities = new double[this.hyposCount][this.factsCount];
    }

    /**
     * The "if the number of hypotheses is zero, then there is an error" testing method.
     * @deprecated because at the time of the last review, the approach to the number of hypotheses had been changed
     * (the number of hypotheses can now be determined at the time of filling in the hypotheses themselves).
     */
    @Deprecated
    private void requireHyposCountSet() throws IncorrectFileValuesException {
        if (this.hyposCount <= 0) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Count of hypotheses is missing.");
            throw e;
        }
    }

    /**
     * Prevents file transfer not in TXT format
     * @param filename name without format, for example: "filedata" or "current_file"
     */
    @Deprecated
    private void setTxt(String filename) throws NullStringPathException {
        if (directoryIsNull()) {
            throw new NullStringPathException("The root directory where the specified file is located is missing. " +
                    "Maybe use static method `BayesianBody.convertDirectory()`");
        }
        this.fileDataDirectory = new StringBuilder(this.fileDataPath.toString())
                .append(filename).append(".txt")
                .toString();
    }

    /**
     * Getter of CURRENT determinated directory of file with data.
     * @return cleaned directory without current file.
     */
    @Deprecated
    public String getStringDirectory() {
        return fileDataDirectory;
    }
}
