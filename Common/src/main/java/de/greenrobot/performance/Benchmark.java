package de.greenrobot.performance;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.greenrobot.essentials.StringUtils;
import org.greenrobot.essentials.io.FileUtils;

public class Benchmark {

    private final List<Pair<String, String>> fixedColumns = new ArrayList<>();
    private final List<Pair<String, String>> values = new ArrayList<>();
    private final Map<Integer, List<Long>> measurements = new HashMap<>();
    private final File file;
    private final SimpleDateFormat dateFormat;
    private final char separator = '\t';
    private final String logTag;

    private String[] headers;
    private boolean storeThreadTime;

    private boolean started;
    private long threadTimeMillis;
    private long timeMillis;
    private int runs;
    private int warmUpRuns;

    public Benchmark(File file, String logTag) {
        this.file = file;
        this.logTag = logTag;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        checkForLastHeader(file);
    }

    private void checkForLastHeader(File file) {
        String contents = null;
        try {
            contents = FileUtils.readUtf8(file);
        } catch (FileNotFoundException e) {
            // OK
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (contents == null) {
            return;
        }

        String[] lines = StringUtils.split(contents, '\n');
        for (int i = lines.length - 1; i >= 0; i--) {
            String[] columnValues = StringUtils.split(lines[i], separator);
            if (columnValues.length > 1) {
                boolean longValueFound = false;
                for (String value : columnValues) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Long.parseLong(value);
                        longValueFound = true;
                        break;
                    } catch (NumberFormatException e) {
                        // OK, header candidate
                    }
                }
                if (!longValueFound) {
                    headers = columnValues;
                    break;
                }
            }
        }
    }

    public Benchmark warmUpRuns(int warmUpRuns) {
        this.warmUpRuns = warmUpRuns;
        return this;
    }

    public Benchmark enableThreadTime() {
        this.storeThreadTime = true;
        return this;
    }

    public Benchmark disableThreadTime() {
        this.storeThreadTime = false;
        return this;
    }

    public Benchmark addFixedColumn(String key, String value) {
        fixedColumns.add(new Pair<>(key, value));
        return this;
    }

    public Benchmark addFixedColumnDevice() {
        addFixedColumn("device", Build.MODEL);
        return this;
    }

    public void start() {
        if (started) {
            throw new RuntimeException("Already started");
        }
        started = true;
        prepareForNextRun();
        if (values.isEmpty()) {
            values.addAll(fixedColumns);
            String startTime = dateFormat.format(new Date());
            values.add(new Pair<>("time", startTime));
        }
        threadTimeMillis = SystemClock.currentThreadTimeMillis();
        timeMillis = SystemClock.elapsedRealtime();
    }

    /**
     * Try to give GC some time to settle down.
     */
    public void prepareForNextRun() {
        for (int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(Type type) {
        long time = SystemClock.elapsedRealtime() - timeMillis;
        long timeThread = SystemClock.currentThreadTimeMillis() - threadTimeMillis;
        if (!started) {
            throw new RuntimeException("Not started");
        }
        started = false;

        String name = type.name();
        log(String.format(Locale.US, "%s: %d ms (thread: %d ms)", name, time, timeThread));
        values.add(new Pair<>(name, Long.toString(time)));
        if (storeThreadTime) {
            values.add(new Pair<>(name + "-thread", Long.toString(timeThread)));
        }

        List<Long> typeMeasurements = measurements.get(type.ordinal());
        if (typeMeasurements == null) {
            typeMeasurements = new ArrayList<>();
            measurements.put(type.ordinal(), typeMeasurements);
        }
        typeMeasurements.add(time);
    }

    public void commit() {
        runs++;
        if (runs > warmUpRuns) {
            log(String.format("Writing results for run %s", runs));
            String[] collectedHeaders = getAllFirsts(values);
            if (!Arrays.equals(collectedHeaders, headers)) {
                headers = collectedHeaders;
                String line = StringUtils.join(headers, "" + separator) + '\n';
                try {
                    FileUtils.appendUtf8(file, line);
                } catch (IOException e) {
                    throw new RuntimeException("Could not write header in benchmark file", e);
                }
            }

            StringBuilder line = new StringBuilder();
            for (Pair<String, String> pair : values) {
                line.append(pair.second).append(separator);
            }
            line.append('\n');
            try {
                FileUtils.appendUtf8(file, line);
            } catch (IOException e) {
                throw new RuntimeException("Could not write header in benchmark file", e);
            }
        } else {
            log(String.format("Ignoring results for run %s (warm up)", runs));
        }
        values.clear();
    }

    private String[] getAllFirsts(List<Pair<String, String>> columns) {
        String[] firsts = new String[columns.size()];
        for (int i = 0; i < firsts.length; i++) {
            firsts[i] = columns.get(i).first;
        }
        return firsts;
    }

    /**
     * Convenience method to create a debug log message.
     */
    public void log(String message) {
        Log.d(logTag, message);
    }

    /**
     * Logs the collected results grouped by measurement type, displays the median over all runs.
     */
    public void logResults() {
        StringBuilder results = new StringBuilder();
        results.append("----Results").append("\n");
        results.append("All values in [ms]").append("\n\n");

        // go through results by enum ordinal so results are sorted as expected
        for (int type = 0; type < Type.values().length; type++) {
            List<Long> typeMeasurements = measurements.get(type);
            if (typeMeasurements == null) {
                continue;
            }

            results.append(Type.values()[type].name()).append("\n");
            for (Long measurement : typeMeasurements) {
                results.append(measurement).append("\n");
            }
            results.append(getMedian(typeMeasurements)).append(" MEDIAN").append("\n");
            results.append("\n");
        }

        results.append("----");
        log(results.toString());
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

    // Note: order determines the order in logResults()
    public enum Type {
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
