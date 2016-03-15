package de.greenrobot.performance.requery;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.Tools;
import io.requery.BlockingEntityStore;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.cache.EmptyEntityCache;
import io.requery.query.Result;
import io.requery.sql.Configuration;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/requery/requery
 */
public class PerfTestRequery extends BasePerfTestCase {

    private static final int DATABASE_VERSION = 1;

    @Override
    protected void tearDown() throws Exception {
        getApplication().deleteDatabase(Models.DEFAULT.name());

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up database
        BlockingEntityStore<Object> data = getData();
        log("Set up database.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(data, getBatchSize());
        }
    }

    private BlockingEntityStore<Object> getData() {
        DatabaseSource source = new DatabaseSource(getApplication(), Models.DEFAULT,
                DATABASE_VERSION);
        Configuration configuration = new ConfigurationBuilder(source,
                Models.DEFAULT).setEntityCache(new EmptyEntityCache()).build();
        return new EntityDataStore<>(configuration).toBlocking();
    }

    private void indexedStringEntityQueriesRun(BlockingEntityStore<Object> database, int count) {
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
        database.transaction().begin();
        try {
            for (int i = 0; i < count; i++) {
                database.insert(entities.get(i));
            }
            database.transaction().commit();
        } finally {
            database.transaction().close();
        }
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];

            Result<IndexedStringEntity> results = database.select(
                    IndexedStringEntity.class)
                    .where(IndexedStringEntity.INDEXED_STRING.eq(
                            fixedRandomStrings[nextIndex]))
                    .get();

            //noinspection unused
            IndexedStringEntity indexedStringEntity = results.first();

            results.close();
        }
        stopClock(Tools.LogMessage.QUERY_INDEXED);

        // delete all entities
        database.delete(IndexedStringEntity.class).get().value();
        log("Deleted all entities.");
    }

    @Override
    protected void doOneByOneAndBatchCrud() throws Exception {
        // set up database
        BlockingEntityStore<Object> data = getData();
        log("Set up database.");

        // set up database
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            oneByOneCrudRun(data, getBatchSize() / 10);
            batchCrudRun(data, getBatchSize());
        }
    }

    private void oneByOneCrudRun(BlockingEntityStore<Object> database, int count) {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            database.insert(list.get(i));
        }
        stopClock(Tools.LogMessage.ONE_BY_ONE_CREATE);

        // requery detects changes, so modify all entities before updating them
        modifyEntities(list);

        startClock();
        for (int i = 0; i < count; i++) {
            database.update(list.get(i));
        }
        stopClock(Tools.LogMessage.ONE_BY_ONE_UPDATE);

        deleteAll(database);
    }

    @SuppressWarnings("unused")
    private void batchCrudRun(BlockingEntityStore<Object> database, int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        database.transaction().begin();
        try {
            for (int i = 0; i < count; i++) {
                database.insert(list.get(i));
            }
            database.transaction().commit();
        } finally {
            database.transaction().close();
        }
        stopClock(Tools.LogMessage.BATCH_CREATE);

        // requery detects changes, so modify all entities before updating them
        modifyEntities(list);

        startClock();
        database.transaction().begin();
        try {
            for (int i = 0; i < count; i++) {
                database.update(list.get(i));
            }
            database.transaction().commit();
        } finally {
            database.transaction().close();
        }
        stopClock(Tools.LogMessage.BATCH_UPDATE);

        startClock();
        Result<SimpleEntityNotNull> results = database.select(
                SimpleEntityNotNull.class).get();
        List<SimpleEntityNotNull> reloaded = results.toList();
        results.close();
        stopClock(Tools.LogMessage.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            long id = entity.getId();
            boolean simpleBoolean = entity.getSimpleBoolean();
            byte simpleByte = entity.getSimpleByte();
            short simpleShort = entity.getSimpleShort();
            int simpleInt = entity.getSimpleInt();
            Long simpleLong = entity.getSimpleLong();
            float simpleFloat = entity.getSimpleFloat();
            double simpleDouble = entity.getSimpleDouble();
            String simpleString = entity.getSimpleString();
            byte[] simpleByteArray = entity.getSimpleByteArray();
        }
        stopClock(Tools.LogMessage.BATCH_ACCESS);

        startClock();
        deleteAll(database);
        stopClock(Tools.LogMessage.BATCH_DELETE);
    }

    private void deleteAll(BlockingEntityStore<Object> database) {
        database.delete(SimpleEntityNotNull.class).get().value();
    }

    protected static SimpleEntityNotNull createEntity(long id) {
        SimpleEntityNotNull entity = new SimpleEntityNotNull();
        entity.setId(id);
        entity.setSimpleBoolean(true);
        entity.setSimpleByte(Byte.MAX_VALUE);
        entity.setSimpleShort(Short.MAX_VALUE);
        entity.setSimpleInt(Integer.MAX_VALUE);
        entity.setSimpleLong(Long.MAX_VALUE);
        entity.setSimpleFloat(Float.MAX_VALUE);
        entity.setSimpleDouble(Double.MAX_VALUE);
        entity.setSimpleString("greenrobot greenDAO");
        byte[] bytes = { 42, -17, 23, 0, 127, -128 };
        entity.setSimpleByteArray(bytes);
        return entity;
    }

    protected static void modifyEntities(List<SimpleEntityNotNull> entities) {
        for (SimpleEntityNotNull entity : entities) {
            entity.setSimpleBoolean(true);
            entity.setSimpleByte(Byte.MAX_VALUE);
            entity.setSimpleShort(Short.MAX_VALUE);
            entity.setSimpleInt(Integer.MAX_VALUE);
            entity.setSimpleLong(Long.MAX_VALUE);
            entity.setSimpleFloat(Float.MAX_VALUE);
            entity.setSimpleDouble(Double.MAX_VALUE);
            entity.setSimpleString("greenrobot greenDAO");
            byte[] bytes = { 42, -17, 23, 0, 127, -128 };
            entity.setSimpleByteArray(bytes);
        }
    }
}
