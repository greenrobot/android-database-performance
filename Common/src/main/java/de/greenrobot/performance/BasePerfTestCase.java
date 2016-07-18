package de.greenrobot.performance;

import android.app.Application;
import android.test.ApplicationTestCase;
import de.greenrobot.performance.Benchmark.Type;
import de.greenrobot.performance.common.BuildConfig;
import java.io.File;

/**
 * Base test case including some helper methods when running a performance test.
 *
 * <p/><b>Note:</b> To run a single test, create a new "Android Tests" run configuration in Android
 * Studio. Right-click to run for Android Tests currently does not work in abstract classes.
 */
public abstract class BasePerfTestCase extends ApplicationTestCase<Application> {

    public static final int DEFAULT_BATCH_SIZE = 10000;
    public static final int ONE_BY_ONE_MODIFIER = 10;
    public static final int DEFAULT_QUERY_COUNT = 1000;
    public static final int RUNS = 8;

    private Benchmark benchmark;

    public BasePerfTestCase() {
        super(Application.class);
    }

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        createApplication();
    }

    public void testIndexedStringEntityQueries() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        onRunSetup();
        setUpBenchmark("indexed-query");

        log("--------Indexed Queries: Start");
        doIndexedStringEntityQueries();
        benchmark.logResults();
        log("--------Indexed Queries: End");
    }

    public void testSingleAndBatchCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        onRunSetup();
        setUpBenchmark("1by1-and-batch");

        log("--------One-by-one/Batch CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            doOneByOneCrudRun(getOneByOneCount());
            doBatchCrudRun(getBatchSize());

            benchmark.commit();
        }
        benchmark.logResults();
        log("--------One-by-one/Batch CRUD: End");
    }

    protected void onRunSetup() throws Exception {
        // no additional setup
    }

    private void setUpBenchmark(String runName) {
        // TODO ut: can not use ext. storage root directory as M+ requires runtime permission
        File outputFile = new File(getContext().getExternalFilesDir(null),
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
