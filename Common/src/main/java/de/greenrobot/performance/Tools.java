package de.greenrobot.performance;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<Integer, List<Long>> measurements;

    public Tools(String logTag, int batchSize, int queryCount) {
        this.logTag = logTag;
        this.batchSize = batchSize;
        this.queryCount = queryCount;
        measurements = new HashMap<>(LogMessage.values().length);
    }

    /**
     * Convenience method to create a debug log message.
     */
    public void log(String message) {
        Log.d(logTag, message);
    }

    /**
     * Logs the collected results grouped by measurement type, displays the average and median over
     * all runs.
     */
    public void logResults() {
        StringBuilder results = new StringBuilder();
        results.append("----Results").append("\n");
        results.append("All values in [ms]").append("\n\n");

        // go through results by enum ordinal so results are sorted as expected
        for (int type = 0; type < LogMessage.values().length; type++) {
            List<Long> typeMeasurements = measurements.get(type);
            if (typeMeasurements == null) {
                continue;
            }

            results.append(getMessage(LogMessage.values()[type])).append("\n");
            for (Long measurement : typeMeasurements) {
                results.append(measurement).append("\n");
            }
            results.append(getMedian(typeMeasurements)).append(" MEDIAN").append("\n");
            results.append("\n");
        }

        results.append("----");
        log(results.toString());
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

        // store measurement
        List<Long> typeMeasurements = measurements.get(type.ordinal());
        if (typeMeasurements == null) {
            typeMeasurements = new ArrayList<>();
            measurements.put(type.ordinal(), typeMeasurements);
        }
        typeMeasurements.add(time);

        // log measured value
        log(getMessage(type) + " in " + time + " ms");
    }

    private double getMedian(List<Long> unsorted) {
        // sort ascending
        Collections.sort(unsorted);
        Long[] values = new Long[unsorted.size()];
        values = unsorted.toArray(values);
        // get median
        int middle = values.length / 2;
        if (values.length % 2 == 1) {
            return values[middle];
        } else {
            return (values[middle - 1] + values[middle]) / 2.0;
        }
    }

    private String getMessage(LogMessage type) {
        switch (type) {
            case QUERY_INDEXED:
                return "Queried for " + getQueryCount() + " of " + getBatchSize()
                        + " indexed entities";
            case ONE_BY_ONE_CREATE:
                return "Inserted (one-by-one) " + getOneByOneCount() + " entities";
            case ONE_BY_ONE_UPDATE:
                return "Updated (one-by-one) " + getOneByOneCount() + " entities";
            case ONE_BY_ONE_DELETE:
                return "Deleted (one-by-one) " + getOneByOneCount() + " entities";
            case ONE_BY_ONE_REFRESH:
                return "Refreshed (one-by-one) " + getOneByOneCount() + " entities";
            case BATCH_CREATE:
                return "Created (batch) " + getBatchSize() + " entities";
            case BATCH_UPDATE:
                return "Updated (batch) " + getBatchSize() + " entities";
            case BATCH_READ:
                return "Read (batch) " + getBatchSize() + " entities";
            case BATCH_ACCESS:
                return "Accessed properties of " + getBatchSize() + " entities";
            case BATCH_DELETE:
                return "Deleted (batch) all entities";
            default:
                throw new IllegalArgumentException("No log message defined for type " + type);
        }
    }

    public int getOneByOneCount() {
        return getBatchSize() / 10;
    }

    private int getBatchSize() {
        return batchSize;
    }

    private int getQueryCount() {
        return queryCount;
    }

    public enum LogMessage {
        QUERY_INDEXED,
        ONE_BY_ONE_CREATE,
        ONE_BY_ONE_UPDATE,
        ONE_BY_ONE_REFRESH,
        ONE_BY_ONE_DELETE,
        BATCH_CREATE,
        BATCH_UPDATE,
        BATCH_READ,
        BATCH_ACCESS,
        BATCH_DELETE
    }

}
