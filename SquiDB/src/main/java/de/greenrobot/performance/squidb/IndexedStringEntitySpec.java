package de.greenrobot.performance.squidb;

import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Simple entity with a string property that is indexed.
 */
@TableModelSpec(className = "IndexedStringEntity", tableName="indexed_string_entity")
public class IndexedStringEntitySpec {

    @PrimaryKey(autoincrement = false)
    public long _id;
    public String indexedString;

}
