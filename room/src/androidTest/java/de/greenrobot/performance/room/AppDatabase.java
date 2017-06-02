package de.greenrobot.performance.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {IndexedStringEntity.class, SimpleEntityNotNull.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SimpleEntityDao simpleEntityDao();
    public abstract IndexedEntityDao indexedEntityDao();
}