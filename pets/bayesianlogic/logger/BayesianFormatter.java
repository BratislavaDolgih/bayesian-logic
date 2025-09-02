package pets.bayesianlogic.logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class BayesianFormatter extends Formatter {
    @Override
    public String getHead(Handler h) {
        return String.format("}- DEBUGGING STARTED (%s) -{%n" +
                        ".debug level = %s%n",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                ),
                h.getLevel());
    }

    @Override
    public String format(LogRecord record) {
        return String.format("[%s; %s]: (%s) %s%n",
                LocalDateTime.now(),
                record.getLevel(),
                record.getLoggerName(),
                formatMessage(record));
    }

    @Override
    public String getTail(Handler h) {
        return String.format("-==| DEBUGGING ENDED [%s] |==-%n",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}
