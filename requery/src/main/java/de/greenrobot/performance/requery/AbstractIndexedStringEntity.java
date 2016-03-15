package de.greenrobot.performance.requery;

import io.requery.Entity;
import io.requery.Index;
import io.requery.Key;

/**
 * Simple entity with a string property that is indexed.
 */
@Entity
public abstract class AbstractIndexedStringEntity {

    @Key
    public long _id;

    @Index(name = "string_index")
    public String indexedString;

}
