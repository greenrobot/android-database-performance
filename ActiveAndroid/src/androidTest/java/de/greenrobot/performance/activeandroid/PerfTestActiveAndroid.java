package de.greenrobot.performance.activeandroid;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Configuration;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;

/**
 * Note: looks like development has ceased. Last commit on 2014-10-07.
 *
 * https://github.com/pardom/ActiveAndroid/wiki/Getting-started
 */
public class PerfTestActiveAndroid extends BasePerfTestCase {

    private static final String DATABASE_NAME = "active-android.db";

    @Override
    public void tearDown() throws Exception {
        if (Cache.isInitialized()) {
            ActiveAndroid.dispose();
        }
        getTargetContext().deleteDatabase(DATABASE_NAME);

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up database
        Configuration dbConfiguration = new Configuration.Builder(getTargetContext())
                .setDatabaseName(DATABASE_NAME)
                .addModelClass(IndexedStringEntity.class)
                .create();
        ActiveAndroid.initialize(dbConfiguration);
        log("Set up database.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(int count) {
        // create entities
        List<IndexedStringEntity> entities = new ArrayList<>(count);
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            IndexedStringEntity entity = new IndexedStringEntity();
            entity.indexedString = fixedRandomStrings[i];
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                entities.get(i).save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            //noinspection unused
            List<IndexedStringEntity> query = new Select()
                    .from(IndexedStringEntity.class)
                    .where("INDEXED_STRING = ?", fixedRandomStrings[nextIndex])
                    .execute();
            // ActiveAndroid already builds all entities when executing the query, so move on
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        ActiveAndroid.execSQL("DELETE FROM INDEXED_STRING_ENTITY");
        log("Deleted all entities.");
    }

    @Override
    protected void onRunSetup() throws Exception {
        super.onRunSetup();

        // set up database
        Configuration dbConfiguration = new Configuration.Builder(getTargetContext())
                .setDatabaseName(DATABASE_NAME)
                .addModelClass(SimpleEntityNotNull.class)
                .create();
        ActiveAndroid.initialize(dbConfiguration);
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity());
        }

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).save();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).save();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity());
        }

        startClock();
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                list.get(i).save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                list.get(i).save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = new Select()
                .all()
                .from(SimpleEntityNotNull.class)
                .execute();
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
        ActiveAndroid.execSQL("DELETE FROM SIMPLE_ENTITY_NOT_NULL");
    }
}
