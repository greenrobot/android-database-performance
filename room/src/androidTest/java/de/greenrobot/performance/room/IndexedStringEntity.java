package de.greenrobot.performance.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Simple entity with a string property that is indexed.
 */
@Entity(tableName = "INDEXED_STRING_ENTITY")
public class IndexedStringEntity {

    @PrimaryKey
    public Long _id;

    @ColumnInfo(name="INDEXED_STRING", index = true)
    public String indexedString;

}
