package de.greenrobot.performance.room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Simple entity with a string property that is indexed.
 */
@Entity(indices = {@Index("indexedString")})
public class IndexedStringEntity {

    @PrimaryKey
    private long id;

    private String indexedString;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndexedString() {
        return indexedString;
    }

    public void setIndexedString(String indexedString) {
        this.indexedString = indexedString;
    }
}
