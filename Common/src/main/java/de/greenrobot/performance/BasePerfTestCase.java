package de.greenrobot.performance;

import android.app.Application;
import android.test.ApplicationTestCase;
import de.greenrobot.performance.Tools.LogMessage;
import de.greenrobot.performance.common.BuildConfig;

/**
 * Base test case including some helper methods when running a performance test.
 *
 * <p/><b>Note:</b> To run a single test, create a new "Android Tests" run configuration in Android
 * Studio. Right-click to run for Android Tests currently does not work in abstract classes.
 */
public abstract class BasePerfTestCase extends ApplicationTestCase<Application> {

    protected static final int RUNS = 8;
    protected final Tools tools;

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

        log("--------One-by-one/Batch CRUD: Start");
        doOneByOneAndBatchCrud();
        tools.logResults();
        log("--------One-by-one/Batch CRUD: End");
    }

    /**
     * Create entities with a string property, populate them with {@link
     * StringGenerator#createFixedRandomStrings(int)}. Then query for the fixed set of indexes given
     * by {@link StringGenerator#getFixedRandomIndices(int, int)}. See existing tests for guidance.
     */
    protected abstract void doIndexedStringEntityQueries() throws Exception;

    /**
     * Run one-by-one create, update. Delete all. Then batch create, update, load and access. Delete
     * all. See existing tests for guidance.
     */
    protected abstract void doOneByOneAndBatchCrud() throws Exception;

    protected void startClock() {
        tools.startClock();
    }

    protected void stopClock(LogMessage type) {
        tools.stopClock(type);
    }

    /**
     * Convenience method to create a debug log message.
     */
    protected void log(String message) {
        tools.log(message);
    }
}
