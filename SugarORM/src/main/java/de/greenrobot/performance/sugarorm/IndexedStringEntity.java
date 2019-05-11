package de.greenrobot.performance.sugarorm;

import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class IndexedStringEntity {
    @Unique
    long id;
    String indexedString;

    public IndexedStringEntity(long id, String indexedString) {
        this.id = id;
        this.indexedString = indexedString;
    }

    public IndexedStringEntity() {
    }
}
