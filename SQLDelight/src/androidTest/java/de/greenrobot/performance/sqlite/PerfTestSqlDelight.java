package de.greenrobot.performance.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.squareup.sqldelight.SqlDelightStatement;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.sqlite.IndexedStringEntityModel.InsertRow;
import de.greenrobot.performance.sqlite.SimpleEntityNotNullModel.UpdateRow;

/**
 * https://github.com/square/sqldelight
 */
public class PerfTestSqlDelight extends BasePerfTestCase {

    private SQLiteDatabase database;

    @Override
    public void tearDown() throws Exception {
        getTargetContext().deleteDatabase(SqlDelightDbHelper.DATABASE_NAME);

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up database
        SqlDelightDbHelper dbHelper = new SqlDelightDbHelper(getTargetContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        log("Set up database.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(database, getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(SQLiteDatabase database, int count) {
        // create strings
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        InsertRow insert = new InsertRow(database);
        log("Built entities.");

        // insert entities
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                insert.bind((long) i, fixedRandomStrings[i]);
                insert.program.executeInsert();
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

            SqlDelightStatement query = IndexedStringEntity.FACTORY
                    .with_string(fixedRandomStrings[nextIndex]);
            Cursor cursor = database.rawQuery(query.statement, query.args);
            // do NO null checks and count checks, should throw to indicate something is incorrect
            cursor.moveToFirst();

            // reconstruct entity
            //noinspection unused
            IndexedStringEntity entity = IndexedStringEntity.MAPPER.map(cursor);

            cursor.close();
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        database.delete(IndexedStringEntity.TABLE_NAME, null, null);
        log("Deleted all entities.");
    }

    @Override
    protected void onRunSetup() throws Exception {
        super.onRunSetup();

        // set up database
        SqlDelightDbHelper dbHelper = new SqlDelightDbHelper(getTargetContext());
        database = dbHelper.getWritableDatabase();
        log("Set up database.");
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull((long) i));
        }

        SimpleEntityNotNullModel.InsertRow insert = new SimpleEntityNotNull.InsertRow(database);
        startClock();
        for (int i = 0; i < count; i++) {
            SimpleEntityNotNull entity = list.get(i);
            insert.bind(
                    entity._id(),
                    entity.simple_boolean(),
                    entity.simple_byte(),
                    entity.simple_short(),
                    entity.simple_int(),
                    entity.simple_long(),
                    entity.simple_float(),
                    entity.simple_double(),
                    entity.simple_string(),
                    entity.simple_byte_array()
            );
            insert.program.executeInsert();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        UpdateRow update = new UpdateRow(database);
        startClock();
        for (int i = 0; i < count; i++) {
            SimpleEntityNotNull entity = list.get(i);
            update.bind(
                    entity.simple_boolean(),
                    entity.simple_byte(),
                    entity.simple_short(),
                    entity.simple_int(),
                    entity.simple_long(),
                    entity.simple_float(),
                    entity.simple_double(),
                    entity.simple_string(),
                    entity.simple_byte_array(),
                    entity._id()
            );
            update.program.executeUpdateDelete();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll(database);
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull((long) i));
        }

        SimpleEntityNotNullModel.InsertRow insert = new SimpleEntityNotNull.InsertRow(database);
        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                SimpleEntityNotNull entity = list.get(i);
                insert.bind(
                        entity._id(),
                        entity.simple_boolean(),
                        entity.simple_byte(),
                        entity.simple_short(),
                        entity.simple_int(),
                        entity.simple_long(),
                        entity.simple_float(),
                        entity.simple_double(),
                        entity.simple_string(),
                        entity.simple_byte_array()
                );
                insert.program.executeInsert();
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_CREATE);

        UpdateRow update = new UpdateRow(database);
        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                SimpleEntityNotNull entity = list.get(i);
                update.bind(
                        entity.simple_boolean(),
                        entity.simple_byte(),
                        entity.simple_short(),
                        entity.simple_int(),
                        entity.simple_long(),
                        entity.simple_float(),
                        entity.simple_double(),
                        entity.simple_string(),
                        entity.simple_byte_array(),
                        entity._id()
                );
                update.program.executeUpdateDelete();
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = new ArrayList<>(count);

        SqlDelightStatement query = SimpleEntityNotNull.FACTORY.select_all();
        Cursor cursor = database.rawQuery(query.statement, query.args);
        while (cursor.moveToNext()) {
            reloaded.add(SimpleEntityNotNull.MAPPER.map(cursor));
        }
        cursor.close();
        stopClock(Benchmark.Type.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            entity._id();
            entity.simple_boolean();
            entity.simple_byte();
            entity.simple_short();
            entity.simple_int();
            entity.simple_long();
            entity.simple_float();
            entity.simple_double();
            entity.simple_string();
            entity.simple_byte_array();
        }
        stopClock(Benchmark.Type.BATCH_ACCESS);

        startClock();
        deleteAll(database);
        stopClock(Benchmark.Type.BATCH_DELETE);
    }

    private void deleteAll(SQLiteDatabase database) {
        database.delete(SimpleEntityNotNull.TABLE_NAME, null, null);
    }

    private SimpleEntityNotNull createSimpleEntityNotNull(long id) {
        byte[] bytes = {42, -17, 23, 0, 127, -128};
        return SimpleEntityNotNull.FACTORY.creator.create(
                id,
                true,
                Byte.MAX_VALUE,
                Short.MAX_VALUE,
                Integer.MAX_VALUE,
                Long.MAX_VALUE,
                Float.MAX_VALUE,
                Double.MAX_VALUE,
                "greenrobot greenDAO",
                bytes
        );
    }

}
