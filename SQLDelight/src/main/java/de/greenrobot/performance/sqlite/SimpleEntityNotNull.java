package de.greenrobot.performance.sqlite;

import com.google.auto.value.AutoValue;

/**
 * Simple entity for performance testing.
 */
@AutoValue
public abstract class SimpleEntityNotNull implements SimpleEntityNotNullModel {
    public static final Mapper<SimpleEntityNotNull> MAPPER = new Mapper<>(
            new Mapper.Creator<SimpleEntityNotNull>() {
                @Override
                public SimpleEntityNotNull create(long _id, boolean simple_boolean, int simple_byte,
                        short simple_short, int simple_int, long simple_long, float simple_float,
                        double simple_double, String simple_string, byte[] simple_byte_array) {
                    return new AutoValue_SimpleEntityNotNull(_id, simple_boolean, simple_byte,
                            simple_short, simple_int, simple_long, simple_float,
                            simple_double, simple_string, simple_byte_array);
                }
            });

    public static final class Marshal extends SimpleEntityNotNullMarshal<Marshal> {
    }
}
