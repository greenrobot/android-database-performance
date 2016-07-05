package de.greenrobot.performance;

import android.app.Application;
import android.test.ApplicationTestCase;
import de.greenrobot.performance.Tools.LogMessage;
import de.greenrobot.performance.common.BuildConfig;
import java.io.File;

/**
 * Base test case including some helper methods when running a performance test.
 *
 * <p/><b>Note:</b> To run a single test, create a new "Android Tests" run configuration in Android
 * Studio. Right-click to run for Android Tests currently does not work in abstract classes.
 */
public abstract class BasePerfTestCase extends ApplicationTestCase<Application> {

    protected static final int RUNS = 8;
    protected final Tools tools;
    private Benchmark benchmark;

    public BasePerfTestCase() {
        super(Application.class);
        this.tools = new Tools(getLogTag(), getBatchSize(), getQueryCount());
    }

    protected int getBatchSize() {
        return Tools.DEFAULT_BATCH_SIZE;
    }

    protected int getOneByOneCount() {
        return tools.getOneByOneCount();
    }

    protected int getQueryCount() {
        return Tools.DEFAULT_QUERY_COUNT;
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

        log("--------Indexed Queries: Start");
        doIndexedStringEntityQueries();
        tools.logResults();
        log("--------Indexed Queries: End");
    }

    public void testSingleAndBatchCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        onRunSetup("1by1-and-batch");

        log("--------One-by-one/Batch CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            doOneByOneCrudRun(getOneByOneCount());
            doBatchCrudRun(getBatchSize());

            benchmark.commit();
        }
        tools.logResults();
        log("--------One-by-one/Batch CRUD: End");
    }

    protected void onRunSetup(String runName) throws Exception {
        // TODO ut: can not use ext. storage root directory as M+ requires runtime permission
        File outputFile = new File(getContext().getExternalFilesDir(null),
                String.format("%s-%s.tsv", getLogTag(), runName));
        benchmark = new Benchmark(outputFile);
        benchmark.addFixedColumnDevice();
    }

    /**
     * Create entities with a string property, populate them with {@link
     * StringGenerator#createFixedRandomStrings(int)}. Then query for the fixed set of indexes given
     * by {@link StringGenerator#getFixedRandomIndices(int, int)}. See existing tests for guidance.
     */
    protected abstract void doIndexedStringEntityQueries() throws Exception;

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
        tools.startClock();
        benchmark.start();
    }

    protected void stopClock(LogMessage type) {
        benchmark.stop(type.name());
        tools.stopClock(type);
    }

    /**
     * Convenience method to create a debug log message.
     */
    protected void log(String message) {
        tools.log(message);
    }
}
