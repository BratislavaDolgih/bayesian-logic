package pets.bayesianlogic.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class implementing formatter for file logs. Overrides {@code format(LogRecord record)} method for JSON.
 */
public class BayesianJSONFormatter extends Formatter {
    @Override
    public String format(LogRecord rec) {
        return String.format("{\"localdate\":\"%s\",\"lvl\":\"%s\"," +
                        "\"loggername\":\"%s\",\"message\":\"%s\",\"thrown\":\"%s\"}%n",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("MM-dd-yyyy'T'HH:mm:ss")),
                rec.getLevel(),
                rec.getLoggerName(),
                rec.getMessage(),
                rec.getThrown() != null ? rec.getThrown().toString() : "");
    }
}
