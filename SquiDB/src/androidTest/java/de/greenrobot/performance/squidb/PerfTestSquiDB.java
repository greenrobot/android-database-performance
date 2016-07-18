package de.greenrobot.performance.squidb;

import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;
import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/yahoo/squidb/wiki
 */
public class PerfTestSquiDB extends BasePerfTestCase {

    private MySquidDatabase database;

    @Override
    protected void tearDown() throws Exception {
        getApplication().deleteDatabase(MySquidDatabase.DATABASE_NAME);

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up database
        MySquidDatabase database = new MySquidDatabase(getApplication());
        log("Set up database.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(database, getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(MySquidDatabase database, int count) {
        // create entities
        List<IndexedStringEntity> entities = new ArrayList<>(count);
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            IndexedStringEntity entity = new IndexedStringEntity();
            // start with id 1 as 0 is treated as NO_ID
            entity.setId((long) i + 1);
            entity.setIndexedString(fixedRandomStrings[i]);
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                database.persistWithId(entities.get(i));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            //noinspection unused
            IndexedStringEntity indexedStringEntity = database.fetchByCriterion(
                    IndexedStringEntity.class,
                    IndexedStringEntity.INDEXED_STRING.eq(fixedRandomStrings[nextIndex]));
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        database.deleteAll(IndexedStringEntity.class);
        log("Deleted all entities.");
    }

    @Override
    protected void onRunSetup() throws Exception {
        super.onRunSetup();

        // set up database
        database = new MySquidDatabase(getApplication());
        log("Set up database.");
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            database.persistWithId(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        // re-set values to set entity as modified
        for (int i = 0; i < list.size(); i++) {
            updateEntity(list.get(i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            database.persist(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll(database);
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                database.persistWithId(list.get(i));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_CREATE);

        // re-set values to set entity as modified
        for (int i = 0; i < list.size(); i++) {
            updateEntity(list.get(i));
        }

        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                database.persist(list.get(i));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = new ArrayList<>(count);

        SquidCursor<SimpleEntityNotNull> query = database.query(SimpleEntityNotNull.class,
                Query.select());
        while (query.moveToNext()) {
            SimpleEntityNotNull entity = new SimpleEntityNotNull();
            entity.readPropertiesFromCursor(query);
            reloaded.add(entity);
        }
        query.close();
        stopClock(Benchmark.Type.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            long id = entity.getId();
            Boolean simpleBoolean = entity.isSimpleBoolean();
            byte simpleByte = entity.getSimpleByte()[0];
            Integer simpleShort = entity.getSimpleShort();
            Integer simpleInt = entity.getSimpleInt();
            Long simpleLong = entity.getSimpleLong();
            Double simpleFloat = entity.getSimpleFloat();
            Double simpleDouble = entity.getSimpleDouble();
            String simpleString = entity.getSimpleString();
            byte[] simpleByteArray = entity.getSimpleByteArray();
        }
        stopClock(Benchmark.Type.BATCH_ACCESS);

        startClock();
        deleteAll(database);
        stopClock(Benchmark.Type.BATCH_DELETE);
    }

    private void deleteAll(MySquidDatabase database) {
        database.deleteAll(SimpleEntityNotNull.class);
    }

    protected static SimpleEntityNotNull createEntity(long id) {
        SimpleEntityNotNull entity = new SimpleEntityNotNull();
        // start with id 1 as 0 is treated as NO_ID
        entity.setId(id + 1);
        entity.setIsSimpleBoolean(true);
        entity.setSimpleByte(new byte[] { Byte.MAX_VALUE });
        entity.setSimpleShort((int) Short.MAX_VALUE);
        entity.setSimpleInt(Integer.MAX_VALUE);
        entity.setSimpleLong(Long.MAX_VALUE);
        entity.setSimpleFloat((double) Float.MAX_VALUE);
        entity.setSimpleDouble(Double.MAX_VALUE);
        entity.setSimpleString("greenrobot greenDAO");
        byte[] bytes = { 42, -17, 23, 0, 127, -128 };
        entity.setSimpleByteArray(bytes);
        return entity;
    }

    protected static void updateEntity(SimpleEntityNotNull entity) {
        entity.setIsSimpleBoolean(true);
        entity.setSimpleByte(new byte[] { Byte.MAX_VALUE });
        entity.setSimpleShort((int) Short.MAX_VALUE);
        entity.setSimpleInt(Integer.MAX_VALUE);
        entity.setSimpleLong(Long.MAX_VALUE);
        entity.setSimpleFloat((double) Float.MAX_VALUE);
        entity.setSimpleDouble(Double.MAX_VALUE);
        entity.setSimpleString("greenrobot greenDAO");
        byte[] bytes = { 42, -17, 23, 0, 127, -128 };
        entity.setSimpleByteArray(bytes);
    }
}
