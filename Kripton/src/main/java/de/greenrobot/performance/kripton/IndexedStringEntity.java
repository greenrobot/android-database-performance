package de.greenrobot.performance.kripton;

import com.abubusoft.kripton.android.annotation.BindTable;

/**
 * Simple entity with a string property that is indexed.
 */
@BindTable(indexes = "indexedString")
public class IndexedStringEntity {

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
