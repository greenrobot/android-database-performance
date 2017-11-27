package de.greenrobot.performance.room;

import android.database.sqlite.SQLiteDatabase;

import com.abubusoft.kripton.android.KriptonLibrary;
import com.abubusoft.kripton.android.sqlite.DataSourceOptions;
import com.abubusoft.kripton.android.sqlite.DatabaseLifecycleHandler;
import com.abubusoft.kripton.android.sqlite.TransactionResult;
import com.abubusoft.kripton.common.One;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.kripton.BindAppDaoFactory;
import de.greenrobot.performance.kripton.BindAppDataSource;
import de.greenrobot.performance.kripton.IndexedStringEntity;
import de.greenrobot.performance.kripton.IndexedStringEntityDaoImpl;
import de.greenrobot.performance.kripton.SimpleEntityNotNull;
import de.greenrobot.performance.kripton.SimpleEntityNotNullDaoImpl;

/**
 * https://developer.android.com/topic/libraries/architecture/room.html
 */
public class PerfTestKripton extends BasePerfTestCase {

    private static final String DB_NAME = "kripton-test.db";

    private BindAppDataSource db;
    private IndexedStringEntityDaoImpl indexedStringEntityDao;
    private SimpleEntityNotNullDaoImpl simpleEntityNotNullDao;

    @Override
    public void setUp() throws Exception {
        KriptonLibrary.init(getTargetContext());

        db = BindAppDataSource.build(DataSourceOptions.builder().log(false).build());

        indexedStringEntityDao = db.getIndexedStringEntityDao();
        simpleEntityNotNullDao = db.getSimpleEntityNotNullDao();

        db.openWritableDatabase();
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        getTargetContext().deleteDatabase(DB_NAME);
    }

    @Override
    protected void doOneByOneCrudRun(final int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        // final reference to entity to save
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
    protected void doBatchCrudRun(final int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        db.execute( new BindAppDataSource.Transaction() {
            @Override
            public TransactionResult onExecute(BindAppDaoFactory daoFactory) {
                SimpleEntityNotNullDaoImpl dao = daoFactory.getSimpleEntityNotNullDao();

                for (int i=0; i<count;i++) {
                    dao.insert(list.get(i));
                }
                return TransactionResult.COMMIT;
            }
        });
        stopClock(Benchmark.Type.BATCH_CREATE);


        startClock();
        db.execute(new BindAppDataSource.Transaction() {
            @Override
            public TransactionResult onExecute(BindAppDaoFactory daoFactory) {
                SimpleEntityNotNullDaoImpl dao = daoFactory.getSimpleEntityNotNullDao();

                for (int i=0; i<count;i++) {
                    dao.update(list.get(i));
                }
                return TransactionResult.COMMIT;
            }
        });
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
    private void indexedStringEntityQueriesRun(final int count) {
        // create entities
        final List<IndexedStringEntity> entities = new ArrayList<>(count);
        final String[] fixedRandomStrings = StringGenerator.createFixedRandomStrings(count);
        for (int i = 0; i < count; i++) {
            IndexedStringEntity entity = new IndexedStringEntity();
            entity.setId((long) i);
            entity.setIndexedString(fixedRandomStrings[i]);
            entities.add(entity);
        }
        log("Built entities.");

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
