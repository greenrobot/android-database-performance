package de.greenrobot.performance.objectbox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;
import de.greenrobot.performance.StringGenerator;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;


import static org.junit.Assert.assertTrue;

/**
 * http://objectbox.io/documentation/introduction/
 */
public class PerfTestObjectBox extends BasePerfTestCase {

    private BoxStore store;
    private Box<SimpleEntityNotNull> simpleEntityNotNullBox;
    private Box<IndexedStringEntity> indexedStringEntityBox;

    private File getObjectBoxTestDir() {
        File targetFilesDir = getTargetContext().getFilesDir();
        return new File(targetFilesDir, "objectbox-test");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        File objectBoxTestDir = getObjectBoxTestDir();
        boolean mkdirs = objectBoxTestDir.mkdirs();
        assertTrue("Failed to create boxstore dir.", mkdirs);

        store = MyObjectBox.builder()
                .directory(objectBoxTestDir)
                .build();
        simpleEntityNotNullBox = store.boxFor(SimpleEntityNotNull.class);
        simpleEntityNotNullBox.removeAll();
        indexedStringEntityBox = store.boxFor(IndexedStringEntity.class);
        indexedStringEntityBox.removeAll();
    }

    @Override
    public void tearDown() throws Exception {
        if (store != null) {
            store.close();
            store = null;
        }
        BoxStore.deleteAllFiles(getObjectBoxTestDir());
        super.tearDown();
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityNotNullBox.put(list.get(i));
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            simpleEntityNotNullBox.put(list.get(i));
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
        simpleEntityNotNullBox.put(list);
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        simpleEntityNotNullBox.put(list);
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = simpleEntityNotNullBox.getAll();
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
        indexedStringEntityBox.put(entities);
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        Query<IndexedStringEntity> query = indexedStringEntityBox.query()
                .equal(IndexedStringEntity_.indexedString, "")
                .build();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];

            query.setParameter(IndexedStringEntity_.indexedString, fixedRandomStrings[nextIndex]);
            List<IndexedStringEntity> result = query.find();
            for (int j = 0, resultSize = result.size(); j < resultSize; j++) {
                IndexedStringEntity entity = result.get(j);
                entity.getId();
                entity.getIndexedString();
            }
        }
        stopClock(Benchmark.Type.QUERY_INDEXED);

        // delete all entities
        // FIXME ut: ObjectBox fails to delete if strings are longer than 480 chars (see StringGenerator)
        indexedStringEntityBox.removeAll();
        log("Deleted all entities.");
    }

    private void deleteAll() {
        simpleEntityNotNullBox.removeAll();
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

}
