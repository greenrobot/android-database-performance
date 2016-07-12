package de.greenrobot.performance.sqlite;

import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;

/**
 * Simple entity with a string property that is indexed.
 */
@AutoValue
public abstract class IndexedStringEntity implements IndexedStringEntityModel {

    public static final Factory<IndexedStringEntity> FACTORY = new Factory<>(
            new Creator<IndexedStringEntity>() {
                @Override
                public IndexedStringEntity create(long _id, @NonNull String indexed_string) {
                    return new AutoValue_IndexedStringEntity(_id, indexed_string);
                }
            });

    public static final RowMapper<IndexedStringEntity> MAPPER = FACTORY.with_stringMapper();

}
