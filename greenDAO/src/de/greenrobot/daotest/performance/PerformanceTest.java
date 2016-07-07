/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * This file is part of greenDAO Generator.
 * 
 * greenDAO Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * greenDAO Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with greenDAO Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.greenrobot.daotest.performance;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.test.AbstractDaoTest;
import de.greenrobot.performance.Tools;
import de.greenrobot.performance.Tools.LogMessage;
import de.greenrobot.performance.common.BuildConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Base test case including some helper methods when running a performance test.
 *
 * <p/><b>Note:</b> To run a single test, create a new "Android Tests" run configuration in Android
 * Studio. Right-click to run for Android Tests currently does not work in abstract classes.
 */
public abstract class PerformanceTest<D extends AbstractDao<T, K>, T, K>
        extends AbstractDaoTest<D, T, K> {

    private static final int RUNS = 8;
    private final Tools tools;

    public PerformanceTest(Class<D> daoClass) {
        super(daoClass, false);
        tools = new Tools(getLogTag(), getBatchSize(), 0);
    }

    public static int getBatchSize() {
        return Tools.DEFAULT_BATCH_SIZE;
    }

    public static int getOneByOneCount() {
        return getBatchSize() / Tools.ONE_BY_ONE_MODIFIER;
    }

    protected abstract String getLogTag();

    public void testOneByOneCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        log("--------One-by-one CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            clearIdentityScopeIfAny();
            oneByOneCrudRun(getOneByOneCount());
        }
        tools.logResults();
        log("--------One-by-one CRUD: End");
    }

    public void testBatchCrud() throws Exception {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.RUN_PERFORMANCE_TESTS) {
            log("Performance tests are disabled.");
            return;
        }

        log("--------Batch CRUD: Start");
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            clearIdentityScopeIfAny();
            batchCrudRun(getBatchSize());
        }
        tools.logResults();
        log("--------Batch CRUD: End");
    }

    private void oneByOneCrudRun(int count) {
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(createEntity());
        }

        startClock();
        for (int i = 0; i < count; i++) {
            dao.insert(list.get(i));
        }
        stopClock(LogMessage.ONE_BY_ONE_CREATE);

        for (int i = 0; i < count; i++) {
            changeForUpdate(list.get(i));
        }
        startClock();
        for (int i = 0; i < count; i++) {
            dao.update(list.get(i));
        }
        stopClock(LogMessage.ONE_BY_ONE_UPDATE);

        startClock();
        for (int i = 0; i < count; i++) {
            dao.refresh(list.get(i));
        }
        stopClock(LogMessage.ONE_BY_ONE_REFRESH);

        startClock();
        for (int i = 0; i < count; i++) {
            dao.delete(list.get(i));
        }
        stopClock(LogMessage.ONE_BY_ONE_DELETE);
    }

    /**
     * Can be overridden, e.g. when indexed properties should change before an update.
     * Time spent in this method is not measured.
     */
    protected void changeForUpdate(T t) {
    }

    private void batchCrudRun(int count) {
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(createEntity());
        }

        startClock();
        dao.insertInTx(list);
        stopClock(LogMessage.BATCH_CREATE);

        for (int i = 0; i < count; i++) {
            changeForUpdate(list.get(i));
        }
        startClock();
        dao.updateInTx(list);
        stopClock(LogMessage.BATCH_UPDATE);

        startClock();
        List<T> reloaded = dao.loadAll();
        stopClock(LogMessage.BATCH_READ);

        accessAll(reloaded);

        startClock();
        dao.deleteAll();
        stopClock(LogMessage.BATCH_DELETE);
    }

    protected void startClock() {
        tools.startClock();
    }

    protected void stopClock(LogMessage type) {
        tools.stopClock(type);
    }

    protected abstract T createEntity();

    /**
     * Access every property of the entity under test and record execution time with {@link
     * #startClock} and {@link #stopClock}.
     */
    protected abstract void accessAll(List<T> list);

    /**
     * Convenience method to create a debug log message.
     */
    protected void log(String message) {
        tools.log(message);
    }
}
