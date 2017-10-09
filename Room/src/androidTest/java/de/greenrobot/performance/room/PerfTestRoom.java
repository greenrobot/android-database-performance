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

    private static final String DB_NAME = "room-test.db";

    private AppDatabase db;
    private IndexedStringEntityDao indexedStringEntityDao;
    private SimpleEntityNotNullDao simpleEntityNotNullDao;

    @Override
    public void setUp() throws Exception {
        db = Room.databaseBuilder(getTargetContext(), AppDatabase.class, DB_NAME).build();
        indexedStringEntityDao = db.indexedStringEntityDao();
        simpleEntityNotNullDao = db.simpleEntityNotNullDao();
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        getTargetContext().deleteDatabase(DB_NAME);
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityNotNullDao.insert(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityNotNullDao.update(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        simpleEntityNotNullDao.insert(list);
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        simpleEntityNotNullDao.update(list);
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = simpleEntityNotNullDao.getAll();
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

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(getBatchSize());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void indexedStringEntityQueriesRun(int count) {
        // create entities
        List<IndexedStringEntity> entities = new ArrayList<>(count);
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            IndexedStringEntity entity = new IndexedStringEntity();
            entity.setId((long) i);
            entity.setIndexedString(fixedRandomStrings[i]);
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        indexedStringEntityDao.insert(entities);
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];

            List<IndexedStringEntity> result = indexedStringEntityDao
                    .withIndexedString(fixedRandomStrings[nextIndex]);
            for (int j = 0, resultSize = result.size(); j < resultSize; j++) {
                IndexedStringEntity entity = result.get(j);
                entity.getId();
                entity.getIndexedString();
            }
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        indexedStringEntityDao.deleteAll();
        log("Deleted all entities.");
    }

    private static SimpleEntityNotNull createSimpleEntityNotNull(Long key) {
        if (key == null) {
            return null;
        }
        SimpleEntityNotNull entity = new SimpleEntityNotNull();
        entity.setId(key);
        entity.setSimpleBoolean(true);
        entity.setSimpleByte(Byte.MAX_VALUE);
        entity.setSimpleShort(Short.MAX_VALUE);
        entity.setSimpleInt(Integer.MAX_VALUE);
        entity.setSimpleLong(Long.MAX_VALUE);
        entity.setSimpleFloat(Float.MAX_VALUE);
        entity.setSimpleDouble(Double.MAX_VALUE);
        entity.setSimpleString("greenrobot greenDAO");
        byte[] bytes = {42, -17, 23, 0, 127, -128};
        entity.setSimpleByteArray(bytes);
        return entity;
    }

    private void deleteAll() {
        simpleEntityNotNullDao.deleteAll();
    }
}
