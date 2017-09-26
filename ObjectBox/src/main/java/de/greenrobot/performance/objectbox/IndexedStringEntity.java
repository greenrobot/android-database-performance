package de.greenrobot.performance.objectbox;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Simple entity with a string property that is indexed.
 */
@Entity
public class IndexedStringEntity {

    @Id(assignable = true)
    private long id;

    @Index
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
