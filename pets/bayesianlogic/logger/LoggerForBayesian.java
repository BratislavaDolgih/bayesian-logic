package pets.bayesianlogic.logger;

import java.util.logging.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A special class for initializing a logger that uses JSON formatting.
 * Used in the main class {@code BayesianBody}, not intended for public access.
 */
public class LoggerForBayesian {
    /**
     * Creating the logger from constructor was blocked!
     */
    private LoggerForBayesian() {}

    /**
     * @param className className name of the class calling the logger.
     * @param willConsoleLogging a boolean value that confirms cataloging by the console.
     * @return ready-to-use logger.
     */
    public static Logger init(Class<?> className, boolean willConsoleLogging) {
        Logger logger = Logger.getLogger(className.getName());

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINE);

        try {
            // fh = file handler, with JSON-formatting.
            FileHandler fh = new FileHandler("logs/bayes.json", true);
            fh.setFormatter(new BayesianJSONFormatter());
            fh.setLevel(Level.FINE);
            logger.addHandler(fh);

            if (willConsoleLogging) {
                // ch = console handler, with classic-bayesian formatting.
                ConsoleHandler ch = new ConsoleHandler();
                ch.setFormatter(new BayesianFormatter());
                ch.setLevel(Level.FINE);
                logger.addHandler(ch);
            }
        } catch (IOException e) {
            throw new LogException("Failed to initialize logging: " + e.getMessage());
        }

        return logger;
    }

    /**
     * Unsuccessful attempt to create a specialized config file to start logging.
     * I gave up on this idea, because I wanted to understand the problem for a long time,
     * until I got to the concepts of initialization of the JVM,
     * the imposition of loggers from those inherited from the root.
     * @param className name of the class calling the logger
     *                  (in this case the decision was to call from {@link pets.bayesianlogic.stuff.BayesianBody}).
     * @return was supposed to return a ready-to-use logger (implied to be without handlers).
     * Later, when I finally abandoned this idea, it was possible to edit handlers in the flow, but it was too late...
     */
    @Deprecated
    public static Logger initWithoutFormat(Class<?> className) {
        Path PROPERTIES_CONFIG_PATH = Paths.get("src/main/resources/logging.properties");
        Logger fabricatedLogger = Logger.getLogger(className.getName());

        fabricatedLogger.setUseParentHandlers(false);

        try {
            Path absolutePath = PROPERTIES_CONFIG_PATH.toAbsolutePath();

            if (!Files.exists(absolutePath)) {
                Files.createDirectories(absolutePath.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(absolutePath)) {
                    writer.write("handlers= java.util.logging.ConsoleHandler, " +
                            "java.util.logging.FileHandler\n");
                    writer.write(".level= INFO\n");
                    writer.write("java.util.logging.ConsoleHandler.level = INFO\n");
                //  writer.write("java.util.logging.ConsoleHandler.formatter = " +
                //          "pets.bayesianlogic.logger.BayesianFormatter\n");
                    writer.write("java.util.logging.FileHandler.level = ALL\n");
                    writer.write("java.util.logging.FileHandler.pattern = logs/bayesian.json\n");
                    writer.write("java.util.logging.FileHandler.limit = 50000\n");
                    writer.write("java.util.logging.FileHandler.count = 1\n");
                //  writer.write("java.util.logging.FileHandler.formatter = " +
                //          "java.util.logging.SimpleFormatter\n");
                }
                System.out.println("A new `logging.properties` with default settings was created!");
            }
            // Initializing the LogManager -> reading config .properties.
            try (InputStream in = Files.newInputStream(absolutePath)) {
                LogManager.getLogManager().readConfiguration(in);
            }

            // creating new FileHandler with JSON-Formatter.
            FileHandler fh = new FileHandler("logs/bayesian.json");
            fh.setFormatter(new BayesianJSONFormatter());
            fh.setLevel(Level.FINE);
            fabricatedLogger.addHandler(fh);

            // creating new ConsoleHandler with classic-formatter.
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new BayesianFormatter());
            ch.setLevel(Level.FINE);
            fabricatedLogger.addHandler(ch);

        } catch (IOException e) {
            throw new LogException("Failed to initialize logging " + e);
        }

        // returning.
        fabricatedLogger.setLevel(Level.FINE);
        return fabricatedLogger;
    }
}
