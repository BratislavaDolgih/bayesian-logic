package pets.bayesianlogic.stuff;

import pets.bayesianlogic.except.*;
import pets.bayesianlogic.logger.LoggerForBayesian;

import java.awt.geom.IllegalPathStateException;
import java.io.FileNotFoundException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

/**
 * The class implements the body of probability calculation according to Bayesian logic. <br>
 * The organization of the API implementation is not obvious, customized (please, read the README file). <br>
 * Contains two constructors: the first one loads data from a file according to a certain principle,
 * the second one reads from the keyboard, throwing exceptions if there is a user error. <br>
 * The supplied exceptions can be partially intercepted within the implementation, but be careful.
 * @author Kolesnikov Stephan
 */
public class BayesianBody {

    /**
     * Private field with absolute probability value
     */
    private final double ABSOLUTE_PROBABILITY = 1.00000;

    /**
     * Private field with absolute impossible probability.
     */
    private final double ABSOLUTE_IMPOSSIBLE = 1e-9;


    /**
     * Static-final field with class logger. All "motions" in the class will action ALWAYS with logging.<br>
     * Logging to file is done ALWAYS in JSON format (the formatter is implemented manually).
     * @link pets.bayesianlogic.logger.BayesianJSONFormatter
     */
    private static Logger toLog = null;

    /**
     * An industry record to be used EXCLUSIVELY for the method {@code getLoggingProperties()}.
     * @see #getLoggingProperties()
     * @param attribute specific logger characteristic.
     */
    public record LogAttributes(Object attribute) {}

    /**
     * A static method for obtaining a list of logger characteristics by record {@link LogAttributes}, including:
     * <code>Level</code> instance, logger name, array of <code>Handler</code>,
     * logger parent, <code>ResourceBundle</code> instance, logging filter.
     * @return list of characteristics (for informatively purpose only).
     */
    public static ArrayList<LogAttributes> getLoggingProperties() {
        if (toLog == null) {
            throw new CrashException("Missing of start-logging object.");
        }

        ArrayList<LogAttributes> atrs = new ArrayList<>();
        atrs.add(new LogAttributes(toLog.getLevel()));
        atrs.add(new LogAttributes(toLog.getName()));
        atrs.add(new LogAttributes(toLog.getHandlers()));
        atrs.add(new LogAttributes(toLog.getParent()));
        atrs.add(new LogAttributes(toLog.getResourceBundle()));
        atrs.add(new LogAttributes(toLog.getFilter()));

        return atrs;
    }

//  ============================================================================================================
//  🔸— «Constructor and fields for loading file»
//  ============================================================================================================

    /**
     * Field-path to the file with calculating data.<br>
     * Enter the correct <code>Path</code> (for correct format call the <code>Paths.get(...)</code>)
     */
    private Path fileDataPath = null;

    /**
     * Public static-method for setting the directory of a file.<br>
     * Used in tests, but as such it makes no sense when there is <code>Paths.get(...)</code>
     * @param pathWithoutFileName input path in format: "C:\\...\\...", but WITHOUT the current file being viewed.
     */
    public static String convertDirectory(String pathWithoutFileName)
            throws NullStringPathException {

        if (pathWithoutFileName == null || pathWithoutFileName.isBlank()) {
            throw new NullStringPathException("Директория null!");
        }

        // Заменяем все одиночные слэши на двойные обратные слэши
        return pathWithoutFileName
                .replace("\\", "\\\\")  // обратные слэши -> двойные
                .replace("/", "\\\\") + "\\\\";
    }

    /**
     * The constructor allows you to insert a path (directory to a file) without any problems.
     * @param dirPath <code>Path</code> instance of FULL directory.
     */
    public BayesianBody(Path dirPath) {
        toLog = LoggerForBayesian.init(BayesianBody.class, true);
        toLog.setLevel(Level.FINE);

        this.fileDataPath = dirPath;
        toLog.fine("New directory (" + this.fileDataPath + ") installed.");
    }

    /**
     * Setter of directory path to the file. Specify a clear and complete path to the file.
     */
    public void setDirectory(Path yourNewPath) {
        this.fileDataPath = yourNewPath;
    }

    /**
     * Method of obtaining the path.
     * @return result directory or null.
     */
    public Path getDirectory() {
        return this.fileDataPath == null ? null : Path.of(this.fileDataPath.toString());
    }

//  ============================================================================================================
//  🔸— «Constructor for keyboard-input»
//  ============================================================================================================

    /**
     * A field-line with the main thesis around which the probability calculation system will be built.
     */
    private String mainExpression = null;

    /**
     * Global scanner to avoid erroneous closing of System.in.
     * Since we need the scanner throughout the entire program, for manual deletion
     * it is worth closing it only in the last method, when all probabilities are calculated.
     * DO NOT CLOSE IT YOURSELF UNDER ANY CIRCUMSTANCES!
     * IT IS ALLOWED TO BE CLOSED by the method {@code this.fabricate()}.
     */
    private final java.util.Scanner bayesianGlobalScanner = new java.util.Scanner(System.in);

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
     * A constructor that fills in the object's fields (hypotheses stuff) "live".
     * @see BayesianBody#fillFacts()
     * @throws ProbabilityInputException is thrown out in case of
     *                               incorrect determination of probabilities and values.
     */
    public BayesianBody()
            throws ProbabilityInputException, InputMismatchException {
        // Epsilon for comparing floating point numbers.
        final double COMPARE_EPSILON = 1e-5;
        toLog = LoggerForBayesian.init(BayesianBody.class, false);

        System.out.println("-== Write your main thesis (main_thesis): ==-");
        // Filling in the field with the main thesis.
        this.mainExpression = bayesianGlobalScanner.nextLine();
        toLog.fine("Filling in the main thesis.");

        System.out.println("> Do you want to write a new hypothesis? | [q]uit");

        // Set symbol to continue filling.
        char triggerButton = bayesianGlobalScanner.nextLine().charAt(0);
        toLog.info("Is being recorded the `" + triggerButton + "` symbol.");

        // (any character = fill; q = exit)
        if (triggerButton != 'q') {
            // The loop will run until we press "exit" ("q").
            do {
                System.out.println("> Hypothesis H" + countHypos[0]++ + " (hypo): ");
                this.hypotheses.ensureCapacity(countHypos[0]);
                this.hypotheses.add(bayesianGlobalScanner.nextLine());
                this.hyposCount++;

                toLog.info("The hypothesis №" + (countHypos[0] - 1)
                        + "is filled.");

                System.out.println("> Do you want to write a new hypothesis? | [q]uit");
                triggerButton = bayesianGlobalScanner.nextLine().charAt(0);
                toLog.info("Is being recorded the `" + triggerButton + "` symbol.");
            } while (triggerButton != 'q');

            if (hyposCountIsZero()) {
                CrashException e = new CrashException("No hypothesis was introduced!");
                toLog.log(Level.SEVERE, "Field `hyposCount` equals zero.", e);
                throw e;
            }

            // We check for complete occupancy of probability.
            double checker100Percent = 0.0; // A variable that will show whether it will definitely be "1".
            for (int i = 0; i < hyposCount; ++i) {
                boolean isValidDouble = false;
                while (!isValidDouble) {
                    try {
                        System.out.println("> Enter probability H" +
                                (i+1) + "(" + hypotheses.get(i) + "): ");
                        System.out.println("[Warning] Probability must be in (0;1].");
                        toLog.fine("Output warning message.");

                        // Dangerous moment with exception throw
                        double cleanDouble = bayesianGlobalScanner.nextDouble();
                        bayesianGlobalScanner.nextLine(); // Swallow the "\n".

                        if (cleanDouble <= ABSOLUTE_IMPOSSIBLE) {
                            ProbabilityInputException e =
                                    new ProbabilityInputException("You entered a negative OR zero-valued probability.");
                            toLog.throwing(BayesianBody.class.getName(),
                                    "BayesianBody()", e);
                            throw e;
                        }

                        if ((cleanDouble - ABSOLUTE_PROBABILITY) > COMPARE_EPSILON) {
                            ProbabilityInputException e =
                                    new ProbabilityInputException("Invalid probability input: " +
                                    "does not fit the range (0; 1].");
                            toLog.throwing(BayesianBody.class.getName(),
                                    "BayesianBody()", e);
                            throw e;
                        }

                        // temporal summary checking (last value).
                        double tentativeSum = checker100Percent + cleanDouble;
                        if (i == hyposCount - 1 && Math.abs(tentativeSum - ABSOLUTE_PROBABILITY) > COMPARE_EPSILON) {
                            System.out.println("[ProbabilityError] The sum of all probabilities must be 1.0." +
                                    " Reenter this last one, please.");
                            continue;
                        }

                        // Add cleaned double.
                        hyposChances.add(cleanDouble);
                        checker100Percent += cleanDouble;
                        isValidDouble = true;
                        toLog.info("The probability has been established.");

                    } catch (InputMismatchException e) {
                        System.out.println("[InputError] NOT DOUBLE, reenter please.");
                        bayesianGlobalScanner.nextLine(); // swallow incorrect.
                        toLog.info("An attempt was made to write down a number.");

                    } catch (ProbabilityInputException ex) {
                        System.out.println("[ProbabilityError] " + ex.getMessage() +
                                " Reenter please (after skipping next line).");
                        bayesianGlobalScanner.nextLine();
                        toLog.info("An attempt was made to write down a CORRECT number.");
                    }
                }
            }
        }
    }

    /**
     * Getter of main thesis of logic.
     */
    public String getThesis() {
        return this.mainExpression;
    }

    /**
     * Getter of list of hypotheses.
     */
    public ArrayList<String> getHypotheses() {
        return new ArrayList<String>(this.hypotheses);
    }

    /**
     * Getter of hypotheses count value.
     */
    public int getHyposCount() {
        return this.hyposCount;
    }

    /**
     * Getter of hypotheses chances count value.
     */
    public ArrayList<Double> getHyposChances() {
        return new ArrayList<>(this.hyposChances);
    }

//  ============================================================================================================
//  🔸— «DESCRIPTION OF OPERATION WITH MANUAL INPUT OF SYMBOLS AND PROBABILITIES FROM THE KEYBOARD»
//  ============================================================================================================

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
     * A public method for defining and recording facts in a system.
     * Should be used when data is entered from a keyboard.
     */
    public void fillFacts() {
        System.out.println("> Determine the number of facts that will be taken into account!");

        boolean integerPressed = false;

        // Using continue-label for fun.
        toLog.finer("Using label for fun.");

        notZero_target:
        while (!integerPressed) {
            try {
                int factsCount = bayesianGlobalScanner.nextInt();

                if (factsCount <= 0) {
                    System.err.println("[ATTENTION] Count of facts is equal zero OR negative-valued. Fix it, please.");
                    toLog.warning("Failed input, code give an new attempt of input.");
                    continue notZero_target;
                }

                bayesianGlobalScanner.nextLine(); // Additionally swallow.

                this.factsCount = factsCount;
                toLog.info("Field `factsCount` was filled.");
                integerPressed = true;
            } catch (InputMismatchException e) {
                System.out.println("[InputError] Please, press an Integer value.");
                toLog.warning("Non-Integer input!");

                bayesianGlobalScanner.nextLine(); // Swallowing.
                continue;
            }
        }

        for (int i = 0; i < this.factsCount; ++i) {
            System.out.println("[Fact №"+ (i+1) +"]: ");
            toLog.fine("Filling a new fact #" + (i+1));

            String currentInputFact = bayesianGlobalScanner.nextLine();
            facts.add(currentInputFact);
            toLog.info("New fact: \"" + currentInputFact + "\" was filled");
        }
    }

    /**
     * Метод, который вызывается для заполнения вероятностей.
     * @throws ProbabilityInputException исключение с неверным значением вероятности.
     * Вероятность должна находиться в диапазоне (0; 1].
     */
    public void fillProbabilities()
            throws ProbabilityInputException, BayesianLogicalException {
        toLog.info("The conditions for creating a table are checking now...");
        if (hyposCountIsZero() || factsCountIsZero()) {
            BayesianLogicalException e =
                    new BayesianLogicalException("[MissingDataError] Hypotheses count is zero OR " +
                            "facts count is zero (please, link to `fillFacts()` " +
                            "OR `readFacts()`, `readHypotheses`).");
            toLog.log(Level.SEVERE, e.getMessage());
            throw e;
        }

        this.tableOfProbabilities = new double[this.hypotheses.size()][this.facts.size()];
        toLog.info("Cheking was successful, probability table was created.");

        for (int i = 0; i < this.hypotheses.size(); ++i) {  // Passing through hypotheses (lines).
            for (int j = 0; j < this.facts.size(); ++j) {   // Passing through hypotheses (columns).
                System.out.println("«" + this.hypotheses.get(i)
                        + "» is true, how likely is it «" + this.facts.get(j)
                        + "» (range of (0...1])?");

                boolean isCorrectDouble = false;
                double localProbability = 0.0;      // Have a zero.

                while (!isCorrectDouble) {
                    try {
                        localProbability = bayesianGlobalScanner.nextDouble();
                        isCorrectDouble = true;
                    } catch (InputMismatchException e) {
                        System.err.println("[InputMismatchError] Non-Double value. Reenter please.");
                        toLog.warning("Non-double value error. Replay this episode of programme.");
                        bayesianGlobalScanner.nextLine(); // <-- consume invalid token.
                        continue;
                    }
                }

                toLog.info("Successful input.");
                bayesianGlobalScanner.nextLine(); // Swallow.

                if (localProbability > ABSOLUTE_PROBABILITY) {
                    ProbabilityInputException e =
                            new ProbabilityInputException("The probability must be in the range [0; 1].");
                    toLog.log(Level.SEVERE, e.getMessage());
                    throw e;
                } else if (localProbability < ABSOLUTE_IMPOSSIBLE) {
                    ProbabilityInputException e =
                            new ProbabilityInputException("Error: sorry, but the probability cannot be negative!");
                    toLog.log(Level.SEVERE, e.getMessage());
                    throw e;
                }

                // Only after all the checks we put it into the probability table.
                this.tableOfProbabilities[i][j] = localProbability;
                toLog.info("Position [" + i + "][" + j + "] was filled with " + localProbability);
            }
        }
    }

    /**
     * Getter of count of facts.
     */
    public int getFactsCount() {
        return this.factsCount;
    }

    /**
     * Getter of ArrayList-typed list with facts.
     */
    public ArrayList<String> getFacts() {
        return new ArrayList<String>(this.facts);
    }

    /**
     * Getter (with {@code clone()} in) the probability table.
     * @return probability table like <code>double[][]</code>.
     */
    public double[][] getDeepClonedProbabilityTable() {
        if (this.tableOfProbabilities == null) { return null; }

        double[][] copiedTable = new double[this.tableOfProbabilities.length][];
        for (int i = 0; i < this.tableOfProbabilities.length; ++i) {
            if (this.tableOfProbabilities[i] != null) {
                copiedTable[i] = this.tableOfProbabilities[i].clone();
            }
        }

        return copiedTable;
    }

//  ============================================================================================================
//  🔸— «METHODS FOR INTERACTING WITH A FILE»
//  ============================================================================================================

    /**
     * An open-API method that reads the main thesis from a file and NOTHING BUT IT.<br>
     * Only the LAST ENTERED LINE is written to the body
     * (if there are many correct {@code main_thesis} in the file, the method will write the last correct line). <br>
     * Be careful, because if incorrect data is entered or there is no data,
     * the program will just leave the field with the main thesis equal to <code>null</code>
     * + a warning from the {@code System.err} stream.
     * @throws IncorrectFileValuesException open file failure.
     * @link setDirectory() — check the directory, because without changing it you may not be able to read the data!
     */
    public void readThesis() throws IncorrectFileValuesException {
        processingKeyword(this.fileDataPath.toString(), "main_thesis", false);
        if (this.mainExpression == null) {
            System.err.println("[ThesisIsMissing] File was read, but it's hasn't main thesis!");
        }
    }


    /**
     * An open-API method for reading all the necessary information about hypotheses.
     * This includes checking the file for the following keywords (what is meant by the hood?):<br>
     *
     * {@code hypos_count} — a word that specifies the exact number of hypotheses entered from the file.
     * The keyword is recommended, but not optional for use. You can do without it,
     * entering only the following two words into the file.<br>
     *
     * {@code hypo} — a word that defines a specific hypothesis and contains a field,
     * that will be interpreted by the code as a <code>String</code>.
     * Without this word, it is impossible to work properly with a publicly accessible class.<br>
     *
     * {@code hypo_chance} — A word that helps determine the probability of
     * a hypothesis declared under the keyword <code>hypo</code> (see Java-doc above).
     * The input awaits a <code>double</code>-number written with a DOT, not a COMMA.<br></br>
     *
     * It makes no sense or sense to read the number of hypotheses separately without hypotheses,
     * so there will be an exception!<br>
     * The author of the class recommends using all these keywords.
     * @link setDirectory() — check the directory, because without changing it you may not be able to read the data!
     */
    public void readHypotheses()
            throws IncorrectFileValuesException, BayesianLogicalException {
        if (this.fileDataPath == null) {
            IllegalPathStateException e
                    = new IllegalPathStateException("File-data path is missing.");
            toLog.severe(e.getMessage());
            throw e;
        }

        processingKeyword(this.fileDataPath.toString(), "hypos_count", false);

//       if (this.hyposCount <= 0) {
//           IncorrectFileValuesException e
//                   = new IncorrectFileValuesException("The hypotheses amount is still 0 or less.\n" +
//                   "Further filling of hypotheses is impossible, check the file.");
//           toLog.severe(e.getMessage());
//           throw e;
//       }

        if (this.hyposCount <= 0) {
            toLog.warning("The keyword `hypos_count` was not found in the file, " +
                    "but the file is still being investigated. In case of successful passing through the files, " +
                    "the identified number of hypotheses will be written to the variable " +
                    "of the number of hypotheses BY FACT");
            this.hyposCount = 0; // you never know!
        }

        processingKeyword(this.fileDataPath.toString(), "hypo", false);

        if (this.hypotheses.isEmpty()) {
            IncorrectFileValuesException e
                    = new IncorrectFileValuesException("Hypotheses in fact is missing. " +
                    "Recheck file for this or other problems!");
            toLog.severe(e.getMessage());
            throw e;
        }

        this.hyposCount = this.hypotheses.size();
        processingKeyword(this.fileDataPath.toString(), "hypo_chance", false);

        if (this.hyposChances.isEmpty()) {
            IncorrectFileValuesException e
                    = new IncorrectFileValuesException("Hypos chances in fact is missing. " +
                    "Recheck file for this or other problems!");
            toLog.severe(e.getMessage());
            throw e;
        }

        if (this.hypotheses.size() != this.hyposChances.size()) {
            IncorrectFileValuesException e
                    = new IncorrectFileValuesException("File has problems " +
                    "(hypotheses amount {" + this.hypotheses.size() +
                    "} does not match hypotheses chances amount {" + this.hyposChances.size() + "}), recheck it.");
            toLog.severe(e.getMessage());
            throw e;
        }

        toLog.info("From a technical point of view, the files were read correctly.");
        toLog.fine("A check is made for correctness from a computational point of view of probabilities.");

        double summaryOfChances = 0.0;
        for (double chance : this.hyposChances) {
            toLog.finer("summaryOfChances = " + summaryOfChances);
            summaryOfChances += chance;
        }

        toLog.fine("The chance-check shows that the amount is now = " + summaryOfChances);
        if (Math.abs(summaryOfChances - 1.0) > ABSOLUTE_IMPOSSIBLE) {
            BayesianLogicalException e
                    = new BayesianLogicalException("Probabilities do not add up to 1.0 (absolute probability). " +
                    "Use `readHypotheses()` again with refactored data in, please.");
            toLog.warning(e.getMessage());
            throw e;
        }
    }


    /**
     * An open-API method for reading all the necessary information about facts.
     * This includes checking the file for the following keywords (what is meant by the hood?):<br>
     *
     * {@code facts_count} — a word that specifies the exact number of facts entered from the file.
     * The keyword is recommended, but not optional for use. You can do without it,
     * entering only the following one word into the file.<br>
     *
     * {@code fact} — a word that defines a specific facts and contains a field,
     * that will be interpreted by the code as a <code>String</code>.
     * Without this word, it is impossible to work properly with a publicly accessible class.<br></br>
     *
     * It makes no sense or sense to read the number of facts separately without facts,
     * so there will be an exception!<br>
     * @throws BayesianLogicalException lack of facts as a logical fallacy.
     * @link setDirectory() — check the directory, because without changing it you may not be able to read the data!
     */
    public void readFacts() throws BayesianLogicalException {
        if (this.fileDataPath == null) {
            IllegalPathStateException e
                    = new IllegalPathStateException("Filedata path is missing.");
            toLog.severe(e.getMessage());
            throw e;
        }
        processingKeyword(this.fileDataPath.toString(), "facts_count", false);

        if (this.factsCount <= 0) {
            toLog.warning("The keyword `hypos_count` was not found in the file, " +
                    "but the file is still being investigated. In case of successful passing through the files, " +
                    "the identified number of hypotheses will be written to the variable " +
                    "of the number of hypotheses BY FACT.");
            this.factsCount = 0; // you never know!
        }

        processingKeyword(this.fileDataPath.toString(), "fact", false);
        if (this.facts.isEmpty()) {
            BayesianLogicalException e
                    = new BayesianLogicalException("Facts list is missing. Recheck your file.");
            toLog.severe(e.getMessage());
            throw e;
        } else {
            this.factsCount = this.facts.size();
        }
    }


    /**
     * API-open method that reads a file with probabilities by the keyword «prob».
     * {@code prob} — the line with the keyword must contain the WORD ITSELF
     * and 3 arguments separated by the ";" sign:
     * first argument is keyword, second - row number,
     * third - column number, fourth - probability in double-value type (example: prob;1;1;0.78).<br>
     *
     * <br>Important warning about organizing lines in the file:
     * if you write a keyword in any case, it is considered;
     * BUT IF any "argument" of the line is missing,
     * the program will NOT GIVE an exception, but will process the table strictly BEFORE THE PROBLEM LINE.
     * In fact, the table can be used, however, according to Bayesian logic (read README.md),
     * probabilities cannot be equal to zero. Check the data!</br>
     *
     * @throws BayesianLogicalException missing of hypotheses or facts.
     * @link setDirectory() — check the directory, because without changing it you may not be able to read the data!
     */
    public void readProbabilities() throws BayesianLogicalException {
        if (this.facts.isEmpty()) {
            BayesianLogicalException e
                    = new BayesianLogicalException("No listed facts found, " +
                    "please double check data (maybe follow the `readFacts()` or `fillFacts`).");
            toLog.severe(e.getMessage());
            throw e;
        } else if (this.hypotheses.isEmpty()) {
            BayesianLogicalException e
                    = new BayesianLogicalException("No listed hypotheses found, " +
                    "please double check data (maybe follow the `readHypotheses()` " +
                    "or constructor `BayesianBody()` with the action of filling of hypotheses).");
            toLog.severe(e.getMessage());
            throw e;
        }

        // hyposCount & factsCount MUST BE NON-NULL!
        this.tableOfProbabilities = new double[this.hyposCount][this.factsCount];

        processingKeyword(this.fileDataPath.toString(), "prob", true);
    }


    /**
     * An open-API method for reading ABSOLUTELY ALL data from a file.<br>
     * All information on keywords is described in discrete methods
     * ({@code readThesis()}, {@code readHypotheses()}, {@code readFacts()}, {@code readProbabilities()})
     * and the README file.
     * @param pathToTheFile the full path of the file where the downloaded data is stored.
     *                      Also, you can use <code>null</code> to try to read a file
     *                      from the current directory specified earlier.
     * @throws FileNotFoundException if the file was not found.
     * @throws IncorrectFileValuesException failure input.
     */
    public void readAll(Path pathToTheFile)
            throws FileNotFoundException, IncorrectFileValuesException {
        if (pathToTheFile == null) {
            if (this.fileDataPath == null) {
                IncorrectFileValuesException e
                        = new IncorrectFileValuesException("Object body's path is null.");
                toLog.warning(e.getMessage());
                throw e;
            }
            toLog.info("The directory remains the same.");
        } else {
            this.fileDataPath = pathToTheFile;
            toLog.info("Path to the data was changed to " + pathToTheFile.toString());
        }

        // Using try-with-resources the file will be opened.
        try (java.util.Scanner inputText =
                     new java.util.Scanner(new java.io.File(this.fileDataPath.toString()))) {
            boolean separate2Parts = true;

            String bufferedLine = null;

            int localHypos = 0, localFacts = 0;

            while (inputText.hasNextLine() && separate2Parts) {
                String line = inputText.nextLine();

                if (line.isBlank() || (!line.startsWith("main_thesis") && !line.startsWith("prob")
                        && !line.startsWith("hypos_count") && !line.startsWith("hypo")
                        && !line.startsWith("facts_count") && !line.startsWith("fact"))) {
                    toLog.fine("Empty line was skipped.");
                    continue;
                }

                // Remember the line with the keyword «prob».
                if (line.trim().startsWith("prob")) {
                    toLog.info("First line with keyword caught.");
                    separate2Parts = false;

                    bufferedLine = line;
                    toLog.info("`" + bufferedLine + "` was written to the buffer.");
                } else {
                    line = line.trim();
                    String[] parts = line.split(";", 2);

                    assert parts.length == 2 : "Separated line has length NOT EQUAL 2.";

                    if (parts[0].isBlank()) {
                        IncorrectFileValuesException e =
                                new IncorrectFileValuesException("Incorrect keyword separation " +
                                        "and(or) parameters and(or) symbol «;»");
                        toLog.log(Level.SEVERE, "Reading file symbols failure.", e);
                        throw e;
                    }

                    String parameter = parts[0].toLowerCase();
                    if (parts[1].isBlank()) {
                        IncorrectFileValuesException e =
                                new IncorrectFileValuesException("No value for parameter {" + parameter + "}");
                        toLog.log(Level.SEVERE, "Parameter value is missing.", e);
                        throw e;
                    }

                    // parameter — is the first part of separated line -> parts[i].
                    switch (parameter) {
                        // Search by File keyword «main_thesis»
                        case "main_thesis"  -> {
                            toLog.info("A line containing `main_thesis` was read.");
                            this.mainExpression = parts[1].trim();
                        }

                        // Search by File keyword «hypos_count»
                        case "hypos_count"  -> {
                            toLog.info("A line containing `hypos_count` was read.");
                            if (!parts[1].matches("\\d+")){
                                IncorrectFileValuesException e =
                                        new IncorrectFileValuesException("Non-Integer entered.");
                                toLog.log(Level.SEVERE, "Non-Integer.", e);
                                throw e;
                            }

                            this.hyposCount = Integer.parseInt(parts[1].trim());
                        }

                        // Search by File keyword «hypo»
                        case "hypo"         -> {
                            if (this.hyposCount == 0) {
                                IncorrectFileValuesException e =
                                        new IncorrectFileValuesException("Count of hypotheses is missing.");
                                toLog.log(Level.SEVERE, "Missing number of hypotheses.", e);
                                throw e;
                            }
                            toLog.info("A line containing `hypo` was read.");
                            this.hypotheses.add(parts[1].trim());
                            toLog.info("A new hypothesis was recorded in the list.");
                            localHypos++;
                        }

                        // Search by File keyword «hypo_chance»
                        case "hypo_chance"  -> {
                            toLog.info("A line containing `hypo_chance` was read.");
                            if (!parts[1].matches("-?\\d+(\\.\\d+)?")) {
                                IncorrectFileValuesException e =
                                        new IncorrectFileValuesException("Non-Double entered.");
                                toLog.log(Level.SEVERE, "Non-Double", e);
                                throw e;
                            }
                            this.hyposChances.add(Double.parseDouble(parts[1].trim()));
                            toLog.info("The chance of a new hypothesis was recorded in the list.");
                        }

                        // Search by File keyword «facts_count»
                        case "facts_count"  -> {
                            toLog.info("A line containing `facts_count` was read.");
                            if (!parts[1].matches("\\d+")){
                                IncorrectFileValuesException e =
                                        new IncorrectFileValuesException("Non-Integer entered.");
                                toLog.log(Level.SEVERE, "Non-Integer", e);
                                throw e;
                            }
                            this.factsCount = Integer.parseInt(parts[1].trim());
                        }

                        // Search by File keyword «fact»
                        case "fact"         -> {
                            toLog.info("A line containing `fact` was read.");
                            this.facts.add(parts[1].trim());
                            toLog.info("A new fact was recorded in the list.");
                            localFacts++;
                        }
                    }
                }
            }

            if (localHypos != this.hyposCount) {
                IncorrectFileValuesException e
                        = new IncorrectFileValuesException(
                        this.hyposCount + " hypotheses were stated, but in fact - " + localHypos);
                toLog.log(Level.SEVERE, e.getMessage());
                throw e;
            } else if (localHypos != this.hyposChances.size()) {
                System.err.println("[ATTENTION] Count of chances of probabilities not equal to count hypotheses");
            }

            if (localFacts != this.factsCount) {
                IncorrectFileValuesException e
                        = new IncorrectFileValuesException(
                        this.factsCount + " facts were stated, but in real - " + localFacts);
                toLog.log(Level.SEVERE, e.getMessage());
                throw e;
            }

            // Initializing the table of probabilities.
            this.tableOfProbabilities = new double[this.hyposCount][this.factsCount];
            toLog.finer("The table was initialized.");

            if (bufferedLine != null) {
                // Throw buffered data (one line) into the parser word by «prob».
                parseProbabilityLine(bufferedLine);

                toLog.finer("Buffered line was processed.");

                while (inputText.hasNextLine()) { parseProbabilityLine(inputText.nextLine().trim()); }
            }
            else {
                System.err.println("— Sudden silence.");
                IncorrectFileValuesException e =
                        new IncorrectFileValuesException("Part of the data starting with " +
                                "the keyword «prob» is missing.");
                toLog.log(Level.SEVERE, "File data failure.", e);
                throw e;
            }
        }
    }


    /**
     * A universal method for building a public API for reading from a file.
     * It can read keywords in a file and fill in fields based on them.<br>
     * Moreover, a keywords:<br>
     * {@code main_thesis} — helper for filling {@code this.mainExpression}<br>
     * {@code hypos_count} — helper for filling {@code this.hyposCount}<br>
     * {@code hypo} — helper for filling {@code this.hypotheses}<br>
     * {@code hypo_chance} — helper for filling {@code this.hyposChances}<br>
     * {@code facts_count} — helper for filling {@code this.factsCount}<br>
     * {@code facts} — helper for filling {@code this.facts}<br>
     * {@code hypos_count} — helper for filling {@code this.hyposCount}<br>
     * {@code prob} — helper for filling {@code this.tableOfProbabilities}<br>
     *
     * @param fullFileName string representation of the path
     *                     (in realization transmitted as <code>this.fileDataPath.toString()</code>).
     * @param key keyword, by which the file will be read.
     * @param fourParts flag for keyword «prob» (probability).
     */
    private void processingKeyword(String fullFileName, final String key, boolean fourParts) {
        this.fileDataPath = Paths.get(fullFileName);
        toLog.finer("Checking for \"fourParts\" flag...");

        try (java.util.Scanner in
                     = new java.util.Scanner(new java.io.File(this.fileDataPath.toString()))) {
            toLog.fine("File was opened.");

            if (!in.hasNextLine()) {
                toLog.fine("It's very sad, but the current file is empty.");
            }

            while (in.hasNextLine()) {
                String line = in.nextLine().trim();

                String scanKeyword = line.split(";", -1)[0].toLowerCase().trim();
                if (line.isBlank() || !scanKeyword.equals(key)) {
                    toLog.fine("Skipped irrelevant line.");
                    continue;
                }

                toLog.info("Line (" + line + ") was checked for «noise».");

                if (!fourParts) {
                    parsingTwoSeparatedParts(line);
                } else {
                    parseProbabilityLine(line);
                }
            }

            if (fourParts) {
                validateTable();
            }
        } catch (InvalidPathException e) {
            toLog.warning("[InvalidPathError] " + e.getMessage());
            System.err.println("[InvalidPathError] " + e.getMessage());
        } catch (FileNotFoundException e) {
            toLog.warning("[FileNotFoundError] " + e.getMessage());
            System.err.println("[FileNotFoundError] " + e.getMessage());
        }
    }


    /**
     * Helper method that performs the string processing option
     * where the keyword split is in two parts (all keywords, except {@code prob})
     * @param line the current line being processed.
     * @throws IncorrectFileValuesException all sorts of problems with splitting and reading lines.
     */
    private void parsingTwoSeparatedParts(String line)
            throws IncorrectFileValuesException{
        String[] lineParts = line.split(";", 2);

        if (lineParts.length != 2) {
            IncorrectFileValuesException e
                    = new IncorrectFileValuesException("Line must contain exactly two parts separated by `;`");
            toLog.severe(e.getMessage());
            throw e;
        }

        String param = lineParts[0].toLowerCase().trim();
        String value = lineParts[1].trim();

        if (value.isBlank()) {
            IncorrectFileValuesException e
                    = new IncorrectFileValuesException("Missing value for {" + param + "}");
            toLog.severe(e.getMessage());
            throw e;
        }

        switch (param) {
            case "main_thesis" -> {
                toLog.fine("A keyword `main_thesis` was detected.");
                this.mainExpression = value;
            }
            case "hypos_count" -> {
                toLog.fine("A keyword `hypos_count` was detected.");
                this.hyposCount = parseIntSafe(value, "hypos_count");
            }
            case "hypo" -> {
                toLog.fine("A keyword `hypo` was detected.");
                //requireHyposCountSet();
                this.hypotheses.add(value);
                toLog.fine("Now hypotheses size = " + this.hypotheses.size());
            }
            case "hypo_chance" -> {
                toLog.fine("A keyword `hypo_chance` was detected.");
                this.hyposChances.add(parseDoubleSafe(value, "hypo_chance"));
            }
            case "facts_count" -> {
                toLog.fine("A keyword `facts_count` was detected.");
                this.factsCount = parseIntSafe(value, "facts_count");
            }
            case "fact" -> {
                toLog.fine("A keyword `fact` was detected.");
                this.facts.add(value);
            }
            default -> toLog.warning("An unknown key was detected.");
        }
    }


    /**
     * A private intermediate method that is taken out specifically for unloading code.
     * It processes strings with four parameters and the keyword «prob».
     * @param trimmedLine readable string.
     * @throws IncorrectFileValuesException error values when entering indexing and probability errors in file.
     */
    private void parseProbabilityLine(String trimmedLine) throws IncorrectFileValuesException {
        if (trimmedLine.isBlank()) { return; }

        // Non-limited separating :))
        String[] parts = trimmedLine.split(";", -1);

        if (parts.length != 4) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("The line with the keyword \"prob\" must contain" +
                            " 4 values separated by the symbol «;»");
            toLog.log(Level.SEVERE, "Incorrect separating by symbol \";\"", e);
            throw e;
        }

        assert parts[0].toLowerCase().trim().equals("prob") : "The line with the «tabular» probability " +
                "must begin with the keyword \"prob\".";

        // After split, we have:
        /*
            parts[0] - keyword "prob",
            parts[1] - hypothesis number,
            parts[2] - fact number,
            parts[3] - probability.
        */

        int hypoNum, factNum;
        double probability;

        try {
            hypoNum = Integer.parseInt(parts[1].trim()) - 1;
            factNum = Integer.parseInt(parts[2].trim()) - 1;
            probability = Double.parseDouble(parts[3].trim());
        } catch (NumberFormatException e) {
            toLog.log(Level.WARNING, "`parts` has a non-number value.");
            throw new IncorrectFileValuesException("Non-value argument in parsing by `prob`.");
        }

        // Validating the hypotheses amount.
        if (hypoNum < 0 || hypoNum >= this.hyposCount) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Hypothesis index " + (hypoNum+1) +
                            "is out of range [1.." + this.hyposCount + "]!");
            toLog.severe(e.getMessage());
            throw e;
        }

        // Validating the facts amount.
        if (factNum >= this.factsCount || factNum < 0) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Fact index " + (factNum+1) +
                            "is out of range [1.." + this.factsCount + "]!");
            toLog.severe(e.getMessage());
            throw e;
        }

        // Validating the probability (0; 1].
        if (probability > ABSOLUTE_PROBABILITY || probability < ABSOLUTE_IMPOSSIBLE) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Invalid Double entered: " +
                            " the probability must be in the range of values [0; 1].");
            toLog.log(Level.SEVERE, e.getMessage());
            throw e;
        }

        this.tableOfProbabilities[hypoNum][factNum] = probability;
        toLog.finer("Table position [" + hypoNum + "][" + factNum + "] was filled.");
    }


    /**
     * Method to check if a number is a string for natural.
     * @param val variable in {@code lineParts[1]}.
     * @param param current keyword.
     * @return integer-valued number of count hypotheses or facts.
     * @throws IncorrectFileValuesException
     */
    private int parseIntSafe(String val, String param) throws IncorrectFileValuesException {
        if (val == null || val.trim().isEmpty()) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Empty value in " + param);
            toLog.log(Level.SEVERE, e.getMessage());
            throw e;
        }

        String trimmed = val.trim();
        if (!val.matches("([1-9]\\d*)")) {
            IncorrectFileValuesException e =
                    new IncorrectFileValuesException("Invalid natural number in " + param);
            toLog.severe(e.getMessage());
            throw e;
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            IncorrectFileValuesException ex =
                    new IncorrectFileValuesException("Number too large in " + param);
            toLog.severe(ex.getMessage());
            throw ex;
        }
    }


    /**
     * Check and return a double number.
     * @param val variable in {@code lineParts[1]}
     * @param param current keyword.
     */
    private double parseDoubleSafe(String val, String param) throws IncorrectFileValuesException {
        if (!val.matches("\\d+(\\.\\d+)?")) {
            throw new IncorrectFileValuesException("Non-double in " + param);
        }
        return Double.parseDouble(val);
    }


//  ============================================================================================================
//  🔸— «PART OF THE INTERNAL CALCULATIONS OF THE PROGRAM»
//  ============================================================================================================

    /**
     * An optional field that updates the last calculated result.
     */
    private final String[] cachedResult = new String[]{ null };

    /**
     * Method where probabilities are multiplied (intermediate method).
     * @return array with probabilities (type: double[])
     */
    private double[] result()
            throws BayesianLogicalException, CrashException {
        double[] result = new double[this.hypotheses.size()];

        // validating by the industrial method.
        validateTable();

        if (this.hypotheses.isEmpty() || this.hyposChances.isEmpty()) {
            throw new BayesianLogicalException("LOGICAL ERROR: " +
                    "There are no hypotheses or their probabilities.");
        }

        for (int i = 0; i < hyposChances.size(); ++i) {
            double coupleCalcs = this.hyposChances.get(i);
            for (int j = 0; j < this.facts.size(); ++j) {
                coupleCalcs *= this.tableOfProbabilities[i][j];
            }
            result[i] = coupleCalcs;
        }

        return result;
    }

    /**
     * An open method that allows you to get the final chance array (converted to 100% ratio).
     * @return array with int[] type.
     */
    private int[] getProbabilityArray() throws BayesianLogicalException {
        final double[] separated = result();
        final double LOCAL_EPSILON = 1e-10;

        double total = 0.0;
        int[] percentage = new int[separated.length];

        for (double position : separated) {
            total += position;
        }

        if (Math.abs(total) < LOCAL_EPSILON) {
            throw new CrashException("Division by zero.");
        }

        for (int i = 0; i < percentage.length; ++i) {
            percentage[i] = (int) Math.round((separated[i] / total) * 100);
        }

        return percentage;
    }

    /**
     * An industrial open-API method that allows you to get a beautifully edited line with effective data.
     * @param needToCash a boolean variable that takes into account whether caching is needed.
     * @return String view of the calculation result (formatted).
     * @throws BayesianLogicalException semantic exceptions (lack of facts and thesis).
     * @throws CrashException error getting row by non-existent key (not taken into account in main(String[] args)).
     */
    public String getStringResult(boolean needToCash)
            throws BayesianLogicalException, CrashException {
        if (mainExpressionIsNull()) {
            throw new BayesianLogicalException("Semantic error: the main thesis of the class is missing.");
        } else if (this.facts.isEmpty()) {
            throw new BayesianLogicalException("Semantic error: class facts are missing.");
        }

        StringBuilder outputString = new StringBuilder();

        // «wallID»: 1.
        // Upper ceiling of symbols («▓») + main thesis
        outputString.append(getSymbolPart(1));

        // «wallID»: 2.
        // The second ceiling («▓»), separating the main thesis from the list of all the facts.
        outputString.append(getSymbolPart(2));

        // «wallID»: 3.
        // Central ceiling printing another symbol («░») + hypotheses + calculations.
        outputString.append(getSymbolPart(3));

        // «wallID»: 4.
        // Floor symbols («▓»)
        outputString.append(getSymbolPart(4));

        if (needToCash) { this.cachedResult[0] = outputString.toString(); }

        return needToCash ? this.cachedResult[0] : outputString.toString();
    }

    /**
     * An open-API method for outputting effective information based on calculated probabilities.
     * Caching of the result inside the class DOES NOT OCCUR with this method.
     * @throws BayesianLogicalException logical exceptions, for example: lack of main points or facts
     * @throws CrashException error getting row by non-existent key.
     */
    public void outputPrettyResult() throws BayesianLogicalException, CrashException {
        System.out.println(getStringResult(false));
    }

    /**
     * A closed method that goes through all available hypotheses and identifies a long.
     * @return longest string hypothesis.
     */
    private String getLongestHypothesis() {
        if (this.hypotheses.isEmpty()) {
            if (this.mainExpression == null) { return null; }
            else { return this.mainExpression; }
        }
        String longest = this.hypotheses.getFirst();
        for (int i = 0; i < this.hypotheses.size(); ++i) {
            if (this.hypotheses.get(i).length() > longest.length()) { longest = this.hypotheses.get(i); }
        }

        return longest;
    }

    /**
     *
     * @param wallNumber variable showing, which part we will print.
     * @return string part (there are 4 of them in total).
     * @throws BayesianLogicalException taken into account in (@code getProbabilityArray()).
     * @throws CrashException critical exceptions.
     */
    private String getSymbolPart(final int wallNumber)
            throws BayesianLogicalException, CrashException {
        if (!(wallNumber > 0 && wallNumber < 5)) {
            throw new CrashException("Invalid «ID» for symbol wall number plate.");
        }

        StringBuilder sb = new StringBuilder();
        // Setting up the key string
        String keyString = getLongestHypothesis();
        if (keyString == null) {
            keyString = "";
        }

        int wallLength = 30 + keyString.length();
        if (wallNumber == 1) {
            repeatWallChar(sb, '▓', wallLength);

            // Top «wall» of hypotheses.
            return sb.append(">> \"").append(this.mainExpression).append("\"\n").toString();

        } else if (wallNumber == 2) {
            repeatWallChar(sb, '▓', wallLength);

            sb.append("Considering the facts:\n");
            for (var fact : this.facts) {
                sb.append("— \"").append(fact).append("\"\n");
            }

            return sb.toString();

        } else if (wallNumber == 3) {
            repeatWallChar(sb, '░', wallLength);

            final int[] probabilityArray = getProbabilityArray();

            // Floor «wall» of hypotheses.
            for (int i = 0; i < probabilityArray.length; ++i) {
                if (this.hypotheses.get(i).length() < keyString.length()) {
                    int differenceBlanks = keyString.length() - this.hypotheses.get(i).length();
                    sb.append("• «")
                            .append(this.hypotheses.get(i))
                            .append("» ");

                    for (int blank = 0; blank < differenceBlanks; ++blank) { sb.append(" "); }

                    sb.append("will have a chance: ")
                            .append(probabilityArray[i]).append("%\n");
                }
                else {
                    sb.append("• «").append(this.hypotheses.get(i))
                            .append("» will have a chance: ")
                            .append(probabilityArray[i]).append("%\n");
                }
            }
            return sb.toString();
        }

        // else if (wallNumber == 4)
        repeatWallChar(sb, '▓', wallLength);
        return sb.toString();
    }

    /**
     * Helper method that replaces 2-3 lines of code that are spent on drawing a wall
     * @param builder StringBuilder instance
     * @param c symbol, that will be repeated.
     * @param count number of times.
     */
    private void repeatWallChar(StringBuilder builder, char c, int count) {
        for (int cur = 0; cur < count; cur++) { builder.append(c); }
        builder.append("\n");
    }

    /**
     * Getter of last cached result of calculations.
     */
    public String getCachedResultString() {
        return this.cachedResult[0] == null ? null : this.cachedResult[0];
    }

//  ============================================================================================================
//  🔸— «INDUSTRIAL-PROTECTED METHODS FOR FASTER CHECKING (all boolean)»
//  ============================================================================================================

    /**
     * An industrial method for testing the main thesis for null.<br>
     * May be overridden.
     * @return boolean value.
     */
    protected boolean mainExpressionIsNull() {
        return this.mainExpression == null;
    }

    /**
     * Industrial method for checking if a directory is missing.<br>
     * May be overridden.
     * @return boolean value.
     */
    protected boolean directoryIsNull() {
        return this.fileDataPath == null;
    }

    /**
     * An industrial method for testing a number of hypotheses for nullity.<br>
     * May be overridden.
     * @return boolean value.
     */
    protected boolean hyposCountIsZero() {
        return this.hyposCount == 0;
    }

    /**
     * An industrial method for testing a number of facts for nullity.<br>
     * May be overridden.
     * @return boolean value.
     */
    protected boolean factsCountIsZero() {
        return this.factsCount == 0;
    }

    /**
     * A method that could be of a logical type.
     * Validates the data in the table, and throws an exception in case of a mismatch.<br>
     * When inheriting a class and modifying a table, it is recommended to override the method.
     * @throws CrashException null-pointer-exceptions.
     */
    protected void validateTable() throws CrashException {
        if (this.tableOfProbabilities == null) {
            throw new CrashException("The table is empty (null).");
        }
        for (var row : this.tableOfProbabilities) {
            if (row == null) { throw new CrashException("The line is empty (null)."); }
            for (var col : row) {
                if (col == 0) { throw new CrashException("Probability cannot take the value 0!"); }
            }
        }
    }


//  ============================================================================================================
//  🔸— «Object OVERRIDDEN METHODS»
//  ============================================================================================================

    /**
     * @param otherObject the reference object with which to compare.
     * @return boolean value: true, if they equal; false in opposite way.
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) { return true; }
        if (!(otherObject instanceof BayesianBody)) {
            return false;
        }

        BayesianBody other = (BayesianBody) otherObject;

        boolean equalFlag = other.mainExpression.equals(this.mainExpression)
                && other.hyposCount == this.hyposCount
                && other.factsCount == this.factsCount;

        if (!equalFlag) { return false; }

        // Testing for the sameness of hypotheses.
        if (!this.hypotheses.equals(other.hypotheses)) {
            return false;
        }

        // Testing for the sameness of hypotheses chances.
       if (!this.hyposChances.equals(other.hyposChances)) {
           return false;
       }

        // Testing for the sameness of facts.
        if (!this.facts.equals(other.facts)) {
            return false;
        }

        // Check for table sameness.
        double[][] thisTable = this.tableOfProbabilities,
                otherTable = other.tableOfProbabilities;
        if (thisTable == null && otherTable == null) { return true; }
        else if (thisTable == null || otherTable == null) { return false; }
        else { return java.util.Arrays.deepEquals(this.tableOfProbabilities, other.tableOfProbabilities); }
    }

    /**
     * Overridden method that finds a hash code.
     * @return hash code.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                this.mainExpression.length(),
                this.factsCount,
                this.hyposCount);
    }

    /**
     * Overridden method that give you a String information about this class.
     * @return line with powerful information.
     */
    @Override
    public String toString() {
        // 20 blanks for long tabulation.
        String longBlank = "                    ";

        return new StringBuilder("class: BayesianBody[")
                .append("mainThesis: `").append(this.mainExpression).append("`,\n")
                .append(longBlank).append("countOfHypotheses: ").append(this.hyposCount).append(",\n")
                .append(longBlank).append("countOfFacts: ").append(this.factsCount).append(",\n")
                .append(longBlank).append("fileDir: ").append(
                        this.fileDataDirectory == null
                        ? this.fileDataPath == null
                                ? "N/D"
                                : this.fileDataPath.toString()
                        : this.fileDataDirectory)
                .append("]").toString();
    }


//  ============================================================================================================
//  🔸— «DEPRECATED STUFF» (for informational purposes only!)
//  ============================================================================================================

    /**
     * Field-directory to the file with calculating data. May be null since there is not the only way to enter data.
     */
    @Deprecated
    private String fileDataDirectory = null;

    /**
     *
     * @param dir String full path.
     * @return <code>Path</code> instance, used in deprecated-constructor {@link #BayesianBody(String, String)}
     * @throws InvalidPathException
     */
    @Deprecated
    private Path initPath(String dir) throws InvalidPathException {
        try {
            return Paths.get(dir);
        } catch (InvalidPathException e) {
            System.out.println("[IncorrectPathError] -> " + e.getMessage());
            return null;
        }
    }

    /**
     * A constructor that allows you to load data from a file according to a certain principle (read <em>README.md</em>)
     * @param fileDirectory full path to the directory (without file name).
     * @param fileName the name of the file located in the specified directory.
     * @throws FileNotFoundException standard: file not found.
     */
    @Deprecated
    public BayesianBody(String fileDirectory, String fileName)
            throws FileNotFoundException {
        toLog = LoggerForBayesian.init(BayesianBody.class, true);
        toLog.setLevel(Level.FINE);

        this.fileDataPath = initPath(fileDirectory);
        toLog.fine("New directory (" + this.fileDataPath + ") installed.");
    }


    /**
     * An industrial method that clearly displays all the necessary data.
     * It is recommended to override for use.
     * @deprecated outputs unformatted data, in an ugly form.
     */
    @Deprecated
    protected void industrialPrint() {
        System.out.println("=========");
        System.out.println(">> " + mainExpression);
        countHypos[0] = 1;
        for (int i = 0; i < this.hypotheses.size(); ++i) {
            System.out.println(countHypos[0]++ + ": " + hypotheses.get(i) + "\t— "
                    + hyposChances.get(i));
        }
        System.out.println(factsCount);
        for (int i = 0; i < this.tableOfProbabilities.length; ++i) {
            for (int j = 0; j < this.tableOfProbabilities[i].length; ++j) {
                System.out.print(this.tableOfProbabilities[i][j] + " ");
            }
            System.out.println();
        }
    }


    /**
     * Created and revised many times by the author in 2025.
     * The class is open for copying, restructuring, revision,
     * addition, criticism and discussion or just poking around.
     *
     * No rights reserved—everything here is GitHub-friendly.
     *
     * May your debugging be painless and your coffee strong!
     * Have a nice time, reviewers!
     *
     * (Crafted in sep, 2025)
     */
}