package de.greenrobot.performance.cupboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;
import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.DatabaseCompartment;

/**
 * https://bitbucket.org/littlerobots/cupboard/wiki/GettingStarted
 */
public class PerfTestCupboard extends BasePerfTestCase {

    private static final String DATABASE_NAME = "cupboard.db";
    private static final int DATABASE_VERSION = 1;

    private Cupboard cupboard;
    private DatabaseCompartment database;

    @Before
    public void setUp() {
        cupboard = new CupboardBuilder().useAnnotations().build();
        // set up database
        cupboard.register(IndexedStringEntity.class);
        cupboard.register(SimpleEntityNotNull.class);
        DbHelper dbHelper = new DbHelper(getTargetContext(), DATABASE_NAME, DATABASE_VERSION);
        database = cupboard.withDatabase(dbHelper.getWritableDatabase());
    }

    @After
    public void cleanUp() throws Exception {
        getTargetContext().deleteDatabase(DATABASE_NAME);
    }

    @Override
    protected void doIndexedStringEntityQueries() {
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
            entity._id = (long) i;
            entity.indexedString = fixedRandomStrings[i];
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        database.put(entities);
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            //noinspection unused
            List<IndexedStringEntity> query = database.query(
                    IndexedStringEntity.class)
                    .withSelection("indexedString = ?", fixedRandomStrings[nextIndex])
                    .list();
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        database.delete(IndexedStringEntity.class, "");
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
            database.put(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            database.put(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll(database);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        database.put(list);
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        database.put(list);
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = database.query(SimpleEntityNotNull.class).list();
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
        deleteAll(database);
        stopClock(Benchmark.Type.BATCH_DELETE);
    }

    private void deleteAll(DatabaseCompartment database) {
        database.delete(SimpleEntityNotNull.class, "");
    }

    private class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            cupboard.withDatabase(db).createTables();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            cupboard.withDatabase(db).upgradeTables();
        }
    }
}
