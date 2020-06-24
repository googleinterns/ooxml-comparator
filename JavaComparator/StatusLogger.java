import org.hsqldb.persist.Log;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class StatusLogger {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static FileHandler handler;

    static {
        try {
            handler = new FileHandler("ExecutionLog.txt");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void AddRecordINFO(String msg) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        logger.log(record);
    }

    public static void AddRecordWARNING(String msg){
        LogRecord record = new LogRecord(Level.WARNING,msg);
        logger.log(record);
    }

    public static void Flush(){
        handler.flush();
    }
}
