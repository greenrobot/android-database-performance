package de.greenrobot.performance.dbflow;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.UpdateModelListTransaction;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.Tools.LogMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/Raizlabs/DBFlow/blob/master/usage/GettingStarted.md
 */
public class PerfTestDbFlow extends BasePerfTestCase {

    @Override
    protected String getLogTag() {
        return getClass().getSimpleName();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        FlowManager.init(getApplication());
    }

    @Override
    protected void tearDown() throws Exception {
        FlowManager.destroy();
        getApplication().deleteDatabase(FlowDatabase.NAME + ".db");

        super.tearDown();
    }

    @Override
    protected void doIndexedStringEntityQueries() throws Exception {
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
        new SaveModelTransaction<>(ProcessModelInfo.withModels(entities)).onExecute();
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];

            //noinspection unused
            IndexedStringEntity indexedStringEntity = SQLite.select()
                    .from(IndexedStringEntity.class)
                    .where(IndexedStringEntity_Table.indexedString.eq(
                            fixedRandomStrings[nextIndex]))
                    .querySingle();
        }
        stopClock(LogMessage.QUERY_INDEXED);

        // delete all entities
        Delete.table(IndexedStringEntity.class);
        log("Deleted all entities.");
    }

    @Override
    protected void doOneByOneAndBatchCrud() throws Exception {
        for (int i = 0; i < RUNS; i++) {
            log("----Run " + (i + 1) + " of " + RUNS);
            oneByOneCrudRun(getBatchSize() / 10);
            batchCrudRun(getBatchSize());
        }
    }

    private void oneByOneCrudRun(int count) throws SQLException {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).insert();
        }
        stopClock(LogMessage.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).update();
        }
        stopClock(LogMessage.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @SuppressWarnings("unused")
    private void batchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        new SaveModelTransaction<>(ProcessModelInfo.withModels(list)).onExecute();
        stopClock(LogMessage.BATCH_CREATE);

        startClock();
        new UpdateModelListTransaction<>(ProcessModelInfo.withModels(list)).onExecute();
        stopClock(LogMessage.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = SQLite.select()
                .from(SimpleEntityNotNull.class)
                .queryList();
        stopClock(LogMessage.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            long id = entity._id;
            boolean simpleBoolean = entity.simpleBoolean;
            byte simpleByte = entity.simpleByte;
            short simpleShort = entity.simpleShort;
            int simpleInt = entity.simpleInt;
            long simpleLong = entity.simpleLong;
            float simpleFloat = entity.simpleFloat;
            double simpleDouble = entity.simpleDouble;
            String simpleString = entity.simpleString;
            byte[] blob = entity.simpleByteArray.getBlob();
        }
        stopClock(LogMessage.BATCH_ACCESS);

        startClock();
        deleteAll();
        stopClock(LogMessage.BATCH_DELETE);
    }

    private void deleteAll() {
        Delete.table(SimpleEntityNotNull.class);
    }

    protected static SimpleEntityNotNull createEntity(long id) {
        SimpleEntityNotNull entity = new SimpleEntityNotNull();
        entity._id = id;
        entity.simpleBoolean = true;
        entity.simpleByte = Byte.MAX_VALUE;
        entity.simpleShort = Short.MAX_VALUE;
        entity.simpleInt = Integer.MAX_VALUE;
        entity.simpleLong =Long.MAX_VALUE;
        entity.simpleFloat = Float.MAX_VALUE;
        entity.simpleDouble = Double.MAX_VALUE;
        entity.simpleString = "greenrobot greenDAO";
        byte[] bytes = { 42, -17, 23, 0, 127, -128 };
        entity.simpleByteArray = new Blob(bytes);
        return entity;
    }
}
