package de.greenrobot.performance.realm;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.StringGenerator;
import de.greenrobot.performance.Tools.LogMessage;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://realm.io/docs/java/latest/ https://github.com/realm/realm-java/
 */
public class PerfTestRealm extends BasePerfTestCase {

    private boolean inMemory = false;

    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        createRealm();
    }

    protected void createRealm() {
        RealmConfiguration.Builder configBuilder = new RealmConfiguration.Builder(getContext());
        if (inMemory) {
            configBuilder.name("inmemory.realm").inMemory();
        } else {
            configBuilder.name("ondisk.realm");
        }
        realm = Realm.getInstance(configBuilder.build());
    }

    @Override
    protected void tearDown() throws Exception {
        if (realm != null) {
            String path = realm.getPath();

            realm.close();

            if (!inMemory) {
                //noinspection ResultOfMethodCallIgnored
                new File(path).delete();
            }
        }

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
            entity.setId((long) i);
            entity.setIndexedString(fixedRandomStrings[i]);
            entities.add(entity);
        }
        log("Built entities.");

        // insert entities
        realm.beginTransaction();
        realm.copyToRealm(entities);
        realm.commitTransaction();
        log("Inserted entities.");

        // query for entities by indexed string at random
        int[] randomIndices = StringGenerator.getFixedRandomIndices(getQueryCount(), count - 1);

        startClock();
        for (int i = 0; i < getQueryCount(); i++) {
            int nextIndex = randomIndices[i];
            RealmQuery<IndexedStringEntity> query = realm.where(IndexedStringEntity.class);
            query.equalTo("indexedString", fixedRandomStrings[nextIndex]);
            RealmResults<IndexedStringEntity> result = query.findAll();
            for (int j = 0, resultSize = result.size(); j < resultSize; j++) {
                // actually get each entity so its object is reconstructed, same with properties
                IndexedStringEntity entity = result.get(j);
                entity.getId();
                entity.getIndexedString();
            }
        }
        stopClock(LogMessage.QUERY_INDEXED);

        // delete all entities
        realm.beginTransaction();
        realm.delete(IndexedStringEntity.class);
        realm.commitTransaction();
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
            realm.beginTransaction();
            realm.copyToRealm(list.get(i));
            realm.commitTransaction();
        }
        stopClock(LogMessage.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(list.get(i));
            realm.commitTransaction();
        }
        stopClock(LogMessage.ONE_BY_ONE_UPDATE);

        deleteAll();
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(SimpleEntityNotNullHelper.createEntity((long) i));
        }

        startClock();
        realm.beginTransaction();
        realm.insert(list);
        realm.commitTransaction();
        stopClock(LogMessage.BATCH_CREATE);

        startClock();
        realm.beginTransaction();
        realm.insertOrUpdate(list);
        realm.commitTransaction();
        stopClock(LogMessage.BATCH_UPDATE);

        startClock();
        RealmResults<SimpleEntityNotNull> reloaded = realm.where(SimpleEntityNotNull.class).findAll();
        stopClock(LogMessage.BATCH_READ);

        // as Realm is not actually loading data, just referencing it,
        // at least make sure we access every property to force it being loaded
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
        stopClock(LogMessage.BATCH_ACCESS);

        startClock();
        deleteAll();
        stopClock(LogMessage.BATCH_DELETE);
    }

    private void deleteAll() {
        realm.beginTransaction();
        realm.delete(SimpleEntityNotNull.class);
        realm.commitTransaction();
    }
}
