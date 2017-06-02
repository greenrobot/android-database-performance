package de.greenrobot.performance.room;

import android.arch.persistence.room.Room;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;

/**
 * https://developer.android.com/topic/libraries/architecture/room.html
 */
public class PerfTestRoom extends BasePerfTestCase {

    public static final String ROOM_DB = "room-db";
    private AppDatabase db;
    private SimpleEntityDao simpleEntityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        db = Room.databaseBuilder(getApplication(),
                AppDatabase.class, ROOM_DB).build();
    }

    @Override
    protected void tearDown() throws Exception {
        getApplication().deleteDatabase(ROOM_DB);

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up data access
        final IndexedEntityDao dao = db.indexedEntityDao();
        log("Set up data access.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(dao, getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(final IndexedEntityDao dao, int count)
            throws Exception {
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
        dao.insertAll(entities);
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            List<IndexedStringEntity> query = dao.getAllByString(fixedRandomStrings[nextIndex]);
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        dao.deleteAll();
        log("Deleted all entities.");
    }

    @Override
    protected void onRunSetup() throws Exception {
        super.onRunSetup();

        simpleEntityDao = db.simpleEntityDao();
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityDao.insert(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityDao.update(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        simpleEntityDao.insertAll(list);
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        simpleEntityDao.updateAll(list);
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = simpleEntityDao.getAll();
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
        simpleEntityDao.deleteAll();
    }

}
