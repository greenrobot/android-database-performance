package de.greenrobot.performance.dbflow;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;

/**
 * https://github.com/Raizlabs/DBFlow/blob/master/usage/GettingStarted.md
 */
public class PerfTestDbFlow extends BasePerfTestCase {

    private static final String DATABASE_NAME = "flowdb";
    private static final String DATABASE_EXTENSION = ".db";

    @Before
    public void setUp() {
        FlowManager.init(new FlowConfig.Builder(getTargetContext())
                .addDatabaseConfig(DatabaseConfig.builder(FlowDatabase.class)
                        .databaseName(DATABASE_NAME)
                        .extensionName(DATABASE_EXTENSION)
                        .build())
                .build());
    }

    @After
    public void cleanUp() {
        FlowManager.destroy();
        getTargetContext().deleteDatabase(DATABASE_NAME + DATABASE_EXTENSION);
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
        FlowManager.getDatabase(FlowDatabase.class).executeTransaction(insertTransaction(entities, IndexedStringEntity.class));

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
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        Delete.table(IndexedStringEntity.class);
        log("Deleted all entities.");
    }

    @NonNull
    private <TModel extends Model> ITransaction insertTransaction(Collection entities, Class<TModel> clazz) {
        ModelAdapter<? extends Model> modelAdapter = FlowManager.getModelAdapter(clazz);
        return FastStoreModelTransaction.insertBuilder(modelAdapter).addAll(entities).build();
    }

    @NonNull
    private <TModel extends Model> ITransaction updateTransaction(Collection entities, Class<TModel> clazz) {
        ModelAdapter<? extends Model> modelAdapter = FlowManager.getModelAdapter(clazz);
        return FastStoreModelTransaction.updateBuilder(modelAdapter).addAll(entities).build();
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).insert();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).update();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createEntity((long) i));
        }

        startClock();
        FlowManager.getDatabase(FlowDatabase.class).executeTransaction(insertTransaction(list, SimpleEntityNotNull.class));
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        FlowManager.getDatabase(FlowDatabase.class).executeTransaction(updateTransaction(list, SimpleEntityNotNull.class));
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = SQLite.select()
                .from(SimpleEntityNotNull.class)
                .queryList();
        stopClock(Benchmark.Type.BATCH_READ);

        startClock();
        for (int i = 0; i < reloaded.size(); i++) {
            SimpleEntityNotNull entity = reloaded.get(i);
            entity.get_id();
            entity.isSimpleBoolean();
            entity.getSimpleByte();
            entity.getSimpleShort();
            entity.getSimpleInt();
            entity.getSimpleLong();
            entity.getSimpleFloat();
            entity.getSimpleDouble();
            entity.getSimpleString();
            entity.getSimpleByteArray().getBlob();
        }
        stopClock(Benchmark.Type.BATCH_ACCESS);

        startClock();
        deleteAll();
        stopClock(Benchmark.Type.BATCH_DELETE);
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
        entity.simpleLong = Long.MAX_VALUE;
        entity.simpleFloat = Float.MAX_VALUE;
        entity.simpleDouble = Double.MAX_VALUE;
        entity.simpleString = "greenrobot greenDAO";
        byte[] bytes = {42, -17, 23, 0, 127, -128};
        entity.simpleByteArray = new Blob(bytes);
        return entity;
    }
}
