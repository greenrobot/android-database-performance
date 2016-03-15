package de.greenrobot.performance.requery;

import io.requery.Entity;
import io.requery.Key;

/**
 * Simple entity for performance testing.
 */
@Entity
public abstract class AbstractSimpleEntityNotNull {

    @Key
    public long _id;
    public boolean simpleBoolean;
    public byte simpleByte;
    public short simpleShort;
    public int simpleInt;
    public long simpleLong;
    public float simpleFloat;
    public double simpleDouble;
    public String simpleString;
    public byte[] simpleByteArray;

}
