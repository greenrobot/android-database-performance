package de.greenrobot.performance.sugarorm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orm.SugarRecord;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.performance.BasePerfTestCase;
import de.greenrobot.performance.Benchmark;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PerfTestSugarorm extends BasePerfTestCase {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("de.greenrobot.performance.sugarorm", appContext.getPackageName());
    }

    @Override
    protected void doOneByOneCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();

        for (long i = 0; i < count; i++)
            list.add(createSimpleEntityNotNull(i));

        startClock();
        for (int i = 0; i < count; i++) {
            list.get(i).save();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_CREATE);

        startClock();
        for (int i = 0; i < count; i++) {
            SimpleEntityNotNull.findById(SimpleEntityNotNull.class, i).save();
        }
        stopClock(Benchmark.Type.ONE_BY_ONE_UPDATE);

        deleteAll(SimpleEntityNotNull.class);
    }

    @Override
    protected void doBatchCrudRun(int count) throws Exception {
        final List<SimpleEntityNotNull> list = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            list.add(createSimpleEntityNotNull(i));
        }

        startClock();
        SugarRecord.saveInTx(list);
        stopClock(Benchmark.Type.BATCH_CREATE);

        startClock();
        SugarRecord.updateInTx(list);
        stopClock(Benchmark.Type.BATCH_UPDATE);

        startClock();
        List<SimpleEntityNotNull> reloaded = SugarRecord.listAll(SimpleEntityNotNull.class);
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
            entity.getReplaceByteArray();
        }
        stopClock(Benchmark.Type.BATCH_ACCESS);

        startClock();
        deleteAll(SimpleEntityNotNull.class);
        stopClock(Benchmark.Type.BATCH_DELETE);
    }

    private static SimpleEntityNotNull createSimpleEntityNotNull(Long key) {
        if (key == null) {
            return null;
        }
        SimpleEntityNotNull entity = new SimpleEntityNotNull();
        entity.setId(key);
        entity.setSimpleBoolean(true);
        entity.setSimpleByte(Integer.MAX_VALUE);
        entity.setSimpleShort(Short.MAX_VALUE);
        entity.setSimpleInt(Integer.MAX_VALUE);
        entity.setSimpleLong(Long.MAX_VALUE);
        entity.setSimpleFloat(Float.MAX_VALUE);
        entity.setSimpleDouble(Double.MAX_VALUE);
        entity.setSimpleString("greenrobot greenDAO");
        entity.replaceByteArray = "42, -17, 23, 0, 127, -128";
        return entity;
    }

    private static <T extends SugarRecord> void deleteAll(Class<T> type) {
        SugarRecord.deleteAll(type);
    }
}
