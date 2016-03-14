package de.greenrobot.performance.squidb;

import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Simple entity for performance testing.
 */
@TableModelSpec(className = "SimpleEntityNotNull", tableName="simple_entity_not_null")
public class SimpleEntityNotNullSpec {

    @PrimaryKey(autoincrement = false)
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
