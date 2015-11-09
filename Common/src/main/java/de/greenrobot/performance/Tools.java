package de.greenrobot.performance;

import android.util.Log;

/**
 * Helper tools for performance measurements.
 */
public class Tools {

    public static final int DEFAULT_BATCH_SIZE = 10000;
    public static final int DEFAULT_QUERY_COUNT = 1000;

    private final String logTag;
    private long start;
    private int batchSize;
    private int queryCount;

    public Tools(String logTag, int batchSize, int queryCount) {
        this.logTag = logTag;
        this.batchSize = batchSize;
        this.queryCount = queryCount;
    }

    /**
     * Convenience method to create a debug log message.
     */
    public void log(String message) {
        Log.d(logTag, message);
    }

    public void startClock() {
        if (start != 0) {
            throw new IllegalStateException("Call stopClock before starting it again.");
        }
        start = System.currentTimeMillis();
    }

    public void stopClock(LogMessage type) {
        long time = System.currentTimeMillis() - start;
        start = 0;

        String message = null;
        if (type == LogMessage.QUERY_INDEXED) {
            message = "Queried for " + getQueryCount() + " of " + getBatchSize()
                    + " indexed entities in " + time + " ms.";
        } else if (type == LogMessage.BATCH_CREATE) {
            message = "Created (batch) " + getBatchSize() + " entities in " + time + " ms";
        } else if (type == LogMessage.BATCH_UPDATE) {
            message = "Updated (batch) " + getBatchSize() + " entities in " + time + " ms";
        } else if (type == LogMessage.BATCH_READ) {
            message = "Read (batch) " + getBatchSize() + " entities in " + time + " ms";
        } else if (type == LogMessage.BATCH_ACCESS) {
            message = "Accessed properties of " + getBatchSize() + " entities in " + time + " ms";
        } else if (type == LogMessage.ONE_BY_ONE_CREATE) {
            message = "Inserted (one-by-one) " + getBatchSize() / 10 + " entities in " + time
                    + " ms";
        } else if (type == LogMessage.ONE_BY_ONE_UPDATE) {
            message = "Updated (one-by-one) " + getBatchSize() / 10 + " entities in " + time
                    + " ms";
        } else if (type == LogMessage.BATCH_DELETE) {
            message = "Deleted (batch) all entities in " + time + " ms";
        }

        if (message != null) {
            log(message);
        }
    }

    private int getBatchSize() {
        return batchSize;
    }

    private int getQueryCount() {
        return queryCount;
    }

    public enum LogMessage {
        BATCH_CREATE,
        BATCH_UPDATE,
        BATCH_READ,
        BATCH_ACCESS,
        ONE_BY_ONE_CREATE,
        ONE_BY_ONE_UPDATE,
        BATCH_DELETE,
        QUERY_INDEXED
    }

}
