package de.greenrobot.performance.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.ColumnAdapter;
import com.squareup.sqldelight.RowMapper;

/**
 * Simple entity for performance testing.
 */
@AutoValue
public abstract class SimpleEntityNotNull implements SimpleEntityNotNullModel {

    /** Short is not supported despite the docs saying it is. Add custom adapter. */
    private static final ColumnAdapter<Short> SHORT_ADAPTER = new ColumnAdapter<Short>() {
        @Override
        public Short map(Cursor cursor, int columnIndex) {
            return cursor.getShort(columnIndex);
        }

        @Override
        public void marshal(ContentValues values, String key, Short value) {
            values.put(key, value);
        }
    };

    public static final Factory<SimpleEntityNotNull> FACTORY = new Factory<>(
            new Creator<SimpleEntityNotNull>() {
                @Override
                public SimpleEntityNotNull create(long _id, boolean simple_boolean, int simple_byte,
                        Short simple_short, int simple_int, long simple_long, float simple_float,
                        double simple_double, String simple_string, byte[] simple_byte_array) {
                    return new AutoValue_SimpleEntityNotNull(_id, simple_boolean, simple_byte,
                            simple_short, simple_int, simple_long, simple_float,
                            simple_double, simple_string, simple_byte_array);
                }
            }, SHORT_ADAPTER);

    public static final RowMapper<SimpleEntityNotNull> MAPPER = FACTORY.select_allMapper();
}
