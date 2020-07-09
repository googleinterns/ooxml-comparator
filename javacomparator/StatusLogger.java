import java.io.IOException;
import java.util.logging.*;

/**
 * The class implements fuctions to log various progress of the execution of the comparator
 */
public class StatusLogger {

    private final static Logger loggerExecution = Logger.getLogger("EXEC");
    private final static Logger loggerDebug = Logger.getLogger("DEBUG");

    static {
        try {
            FileHandler handlerExecution = new FileHandler("ExecutionLog.txt");
            handlerExecution.setFormatter(new SimpleFormatter());
            loggerExecution.addHandler(handlerExecution);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileHandler handlerDebug = new FileHandler("DebugLog.txt");
            handlerDebug.setFormatter(new SimpleFormatter());
            loggerDebug.addHandler(handlerDebug);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add Execution info to the Execution logger
     * @param msg msg string to be added to the file
     */
    public static void addRecordInfoExec(String msg) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        loggerExecution.log(record);
    }

    /**
     * Add Execution Warning to the Execution logger
     * @param msg msg string to be added to the file
     */
    public static void addRecordWarningExec(String msg) {
        LogRecord record = new LogRecord(Level.WARNING,msg);
        loggerExecution.log(record);
    }

    /**
     * Add Debug Statements to the Debug logger
     * @param msg msg string to be added to the file
     */
    public static void addRecordInfoDebug(String msg) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        loggerDebug.log(record);
    }
}
