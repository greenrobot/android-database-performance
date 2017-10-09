package de.greenrobot.performance;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import de.greenrobot.performance.Benchmark.Type;
import de.greenrobot.performance.common.BuildConfig;

/**
 * Base test case including some helper methods when running a performance test.
 */
@RunWith(AndroidJUnit4.class)
public abstract class BasePerfTestCase {

    public static final int DEFAULT_BATCH_SIZE = 10000;
    public static final int ONE_BY_ONE_MODIFIER = 10;
    public static final int DEFAULT_QUERY_COUNT = 1000;
    public static final int RUNS = 8;

    private Benchmark benchmark;

    protected int getBatchSize() {
        return DEFAULT_BATCH_SIZE;
    }

    protected int getOneByOneCount() {
        return getBatchSize() / ONE_BY_ONE_MODIFIER;
    }

    protected int getQueryCount() {
        return DEFAULT_QUERY_COUNT;
    }

    /**
     * Specify a different log tag. By default uses the simple name of the class.
     */
    protected String getLogTag() {
        return getClass().getSimpleName();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    protected Context getTargetContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testIndexedStringEntityQueries() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        setUpBenchmark("indexed-query");
        onRunSetup();

        log("--------Indexed Queries: Start");
        doIndexedStringEntityQueries();
        benchmark.logResults();
        log("--------Indexed Queries: End");
    }

    @Test
    public void testOneByOneCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        setUpBenchmark("1by1");
        onRunSetup();

        log("--------One-by-one CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            doOneByOneCrudRun(getOneByOneCount());

            benchmark.commit();
        }
        benchmark.logResults();
        log("--------One-by-one CRUD: End");
    }

    @Test
    public void testBatchCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        setUpBenchmark("batch");
        onRunSetup();

        log("--------Batch CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            doBatchCrudRun(getBatchSize());

            benchmark.commit();
        }
        benchmark.logResults();
        log("--------Batch CRUD: End");
    }

    protected void onRunSetup() throws Exception {
        // no additional setup
    }

    private void setUpBenchmark(String runName) {
        // TODO ut: can not use ext. storage root directory as M+ requires runtime permission
        File outputFile = new File(getTargetContext().getExternalFilesDir(null),
                String.format("%s-%s.tsv", getLogTag(), runName));
        benchmark = new Benchmark(outputFile, getLogTag());
        benchmark.addFixedColumnDevice();
    }

    /**
     * Create entities with a string property, populate them with {@link
     * StringGenerator#createFixedRandomStrings(int)}. Then query for the fixed set of indexes given
     * by {@link StringGenerator#getFixedRandomIndices(int, int)}. See existing tests for guidance.
     */
    protected void doIndexedStringEntityQueries() throws Exception {
        log("doIndexedStringEntityQueries NOT implemented");
    }

    /**
     * Run one-by-one create, update. Delete all. See existing tests for guidance.
     */
    protected void doOneByOneCrudRun(int count) throws Exception {
        log("doOneByOneCrudRun NOT implemented");
    }

    /**
     * Batch create, update, load and access. Delete all. See existing tests for guidance.
     */
    protected void doBatchCrudRun(int count) throws Exception {
        log("doBatchCrudRun NOT implemented");
    }

    protected void startClock() {
        benchmark.start();
    }

    protected void stopClock(Type type) {
        benchmark.stop(type);
    }

    /**
     * Convenience method to create a debug log message.
     */
    protected void log(String message) {
        benchmark.log(message);
    }
}
