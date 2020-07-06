import java.io.IOException;
import java.util.logging.*;

/**
 * The class implements fuctions to log various progress of the execution of the comparator
 */
public class StatusLogger {
    private final static Logger loggerExec = Logger.getLogger("EXEC");

    private final static Logger loggerDebug = Logger.getLogger("DEBUG");

    static {
        try {
            FileHandler handlerExec = new FileHandler("ExecutionLog.txt");
            handlerExec.setFormatter(new SimpleFormatter());
            loggerExec.addHandler(handlerExec);
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

    public static void addRecordInfoExec(String msg) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        loggerExec.log(record);
    }

    public static void addRecordWarningExec(String msg) {
        LogRecord record = new LogRecord(Level.WARNING,msg);
        loggerExec.log(record);
    }

    public static void addRecordInfoDebug(String msg) {
//        LogRecord record = new LogRecord(Level.INFO, msg);
//        loggerDebug.log(record);
    }
}
