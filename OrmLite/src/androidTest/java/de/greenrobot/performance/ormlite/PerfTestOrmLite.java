package de.greenrobot.performance.ormlite;

import com.j256.ormlite.dao.Dao;

import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * http://ormlite.com/sqlite_java_android_orm.shtml https://github.com/j256/ormlite-examples
 */
public class PerfTestOrmLite extends BasePerfTestCase {

    private final static boolean IN_MEMORY = false;

    private DbHelper dbHelper;
    private Dao<SimpleEntityNotNull, Long> dao;
    private Dao<IndexedStringEntity, Long> daoIndexed;

    @Before
    public void setUp() throws SQLException {
        String name;
        if (IN_MEMORY) {
            name = null;
        } else {
            name = "test-db";
        }
        dbHelper = new DbHelper(getTargetContext(), name);
        dao = dbHelper.getDao(SimpleEntityNotNull.class);
        daoIndexed = dbHelper.getDao(IndexedStringEntity.class);
    }

    @After
    public void cleanUp() {
        getTargetContext().deleteDatabase("test-db");
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(int count) throws Exception {
        // create entities
        final List<IndexedStringEntity> entities = new ArrayList<>(count);
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            IndexedStringEntity entity = new IndexedStringEntity();
            entity._id = (long) i;
            entity.indexedString = fixedRandomStrings[i];
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        daoIndexed.callBatchTasks(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (IndexedStringEntity entity : entities) {
                    daoIndexed.create(entity);
                }
                return null;
            }
        });
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            //noinspection unused
            List<IndexedStringEntity> query = daoIndexed.queryBuilder()
                    .where()
                    .eq("INDEXED_STRING", fixedRandomStrings[nextIndex])
                    .query();
            // ORMLite already builds all entities when executing the query, so move on
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        dbHelper.getWritableDatabase().execSQL("DELETE FROM INDEXED_STRING_ENTITY");
        log("Deleted all entities.");
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            dao.create(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            dao.update(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        dao.callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (SimpleEntityNotNull entity : list) {
                    dao.create(entity);
                }
                return null;
            }
        });
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        dao.callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (SimpleEntityNotNull entity : list) {
                    dao.update(entity);
                }
                return null;
            }
        });
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = dao.queryForAll();
        stopClock(Benchmark.Type.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            entity.getId();
            entity.getSimpleBoolean();
            entity.getSimpleByte();
            entity.getSimpleShort();
            entity.getSimpleInt();
            entity.getSimpleLong();
            entity.getSimpleFloat();
            entity.getSimpleDouble();
            entity.getSimpleString();
            entity.getSimpleByteArray();
        }
        stopClock(Benchmark.Type.BATCH_ACCESS);

        startClock();
        deleteAll();
        stopClock(Benchmark.Type.BATCH_DELETE);
    }

    private void deleteAll() {
        dbHelper.getWritableDatabase().execSQL("DELETE FROM SIMPLE_ENTITY_NOT_NULL");
    }

    public void testSemantics() {
        try {
            Dao<MinimalEntity, Long> minimalDao = dbHelper.getDao(MinimalEntity.class);
            MinimalEntity data = new MinimalEntity();
            minimalDao.create(data);
            // ORMLite does update PK after insert if set to generatedId
            assertNotNull(data.getId());
            MinimalEntity data2 = minimalDao.queryForAll().get(0);
            MinimalEntity data3 = minimalDao.queryForId(data2.getId());
            // ORMLite does not provide object equality
            assertNotSame(data, data2);
            assertNotSame(data2, data3);
            assertEquals(data2.getId(), data3.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
