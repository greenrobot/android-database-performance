package de.greenrobot.performance.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.Tools;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/square/sqldelight
 */
public class PerfTestSqlDelight extends BasePerfTestCase {

    @Override
    protected void tearDown() throws Exception {
        getApplication().deleteDatabase(SqlDelightDbHelper.DATABASE_NAME);

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
        // set up database
        SqlDelightDbHelper dbHelper = new SqlDelightDbHelper(getApplication());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        log("Set up database.");

        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            indexedStringEntityQueriesRun(database, getBatchSize());
        }
    }

    private void indexedStringEntityQueriesRun(SQLiteDatabase database, int count) {
        // create entities
        List<ContentValues> entities = new ArrayList<>(count);
        String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            ContentValues entity = IndexedStringEntity.FACTORY.marshal()
                    ._id((long) i)
                    .indexed_string(fixedRandomStrings[i])
                    .asContentValues();
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                database.insert(IndexedStringEntity.TABLE_NAME, null, entities.get(i));
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

            Cursor query = database.rawQuery(IndexedStringEntity.WITH_STRING,
                    new String[] { fixedRandomStrings[nextIndex] });
            // do NO null checks and count checks, should throw to indicate something is incorrect
            query.moveToFirst();

            // reconstruct entity
            //noinspection unused
            IndexedStringEntity entity = IndexedStringEntity.MAPPER.map(query);

            query.close();
        }
        stopClock(Tools.LogMessage.QUERY_INDEXED);

        // delete all entities
        database.delete(IndexedStringEntity.TABLE_NAME, null, null);
        log("Deleted all entities.");
    }

    @Override
    protected void doOneByOneAndBatchCrud() throws Exception {
        // set up database
        SqlDelightDbHelper dbHelper = new SqlDelightDbHelper(getApplication());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        log("Set up database.");

        // set up database
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            oneByOneCrudRun(database, getOneByOneCount());
            batchCrudRun(database, getBatchSize());
        }
    }

    private void oneByOneCrudRun(SQLiteDatabase database, int count) throws SQLException {
        final List<ContentValues> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            database.insert(SimpleEntityNotNull.TABLE_NAME, null, list.get(i));
        }
        stopClock(Tools.LogMessage.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            ContentValues entity = list.get(i);
            database.update(SimpleEntityNotNull.TABLE_NAME, entity,
                    SimpleEntityNotNull._ID + "=" + entity.getAsLong(SimpleEntityNotNull._ID),
                    null);
        }
        stopClock(Tools.LogMessage.ONE_BY_ONE_UPDATE);

        deleteAll(database);
    }

    private void batchCrudRun(SQLiteDatabase database, int count) throws Exception {
        final List<ContentValues> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                database.insert(SimpleEntityNotNull.TABLE_NAME, null, list.get(i));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Tools.LogMessage.BATCH_CREATE);

        startClock();
        database.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                ContentValues entity = list.get(i);
                database.update(SimpleEntityNotNull.TABLE_NAME, entity,
                        SimpleEntityNotNull._ID + "=" + entity.getAsLong(SimpleEntityNotNull._ID),
                        null);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        stopClock(Tools.LogMessage.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = new ArrayList<>(count);

        Cursor query = database.rawQuery(SimpleEntityNotNull.SELECT_ALL, new String[0]);
        while (query.moveToNext()) {
            reloaded.add(SimpleEntityNotNull.MAPPER.map(query));
        }
        query.close();
        stopClock(Tools.LogMessage.BATCH_READ);

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
        stopClock(Tools.LogMessage.BATCH_ACCESS);

        startClock();
        deleteAll(database);
        stopClock(Tools.LogMessage.BATCH_DELETE);
    }

    private void deleteAll(SQLiteDatabase database) {
        database.delete(SimpleEntityNotNull.TABLE_NAME, null, null);
    }

    protected static ContentValues createEntity(long id) {
        byte[] bytes = { 42, -17, 23, 0, 127, -128 };
        return SimpleEntityNotNull.FACTORY.marshal()
                ._id(id)
                .simple_boolean(true)
                .simple_byte(Byte.MAX_VALUE)
                .simple_short(Short.MAX_VALUE)
                .simple_int(Integer.MAX_VALUE)
                .simple_long(Long.MAX_VALUE)
                .simple_float(Float.MAX_VALUE)
                .simple_double(Double.MAX_VALUE)
                .simple_string("greenrobot greenDAO")
                .simple_byte_array(bytes)
                .asContentValues();
    }

}
