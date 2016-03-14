package de.greenrobot.performance.sqlite;

import com.google.auto.value.AutoValue;

/**
 * Simple entity with a string property that is indexed.
 */
@AutoValue
public abstract class IndexedStringEntity implements IndexedStringEntityModel {

    public static final Mapper<IndexedStringEntity> MAPPER = new Mapper<>(
            new Mapper.Creator<IndexedStringEntity>() {
                @Override
                public IndexedStringEntity create(long _id, String indexed_string) {
                    return new AutoValue_IndexedStringEntity(_id, indexed_string);
                }
            });

    public static final class Marshal extends IndexedStringEntityMarshal<Marshal> {
    }
}
