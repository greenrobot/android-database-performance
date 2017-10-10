package de.greenrobot.performance.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Simple entity for performance testing.
 */
@Table(database = FlowDatabase.class)
public class SimpleEntityNotNull extends BaseModel {

    @PrimaryKey
    long _id;
    @Column
    boolean simpleBoolean;
    @Column
    byte simpleByte;
    @Column
    short simpleShort;
    @Column
    int simpleInt;
    @Column
    long simpleLong;
    @Column
    float simpleFloat;
    @Column
    double simpleDouble;
    @Column
    String simpleString;
    @Column
    Blob simpleByteArray;

    public long get_id() {
        return _id;
    }

    public boolean isSimpleBoolean() {
        return simpleBoolean;
    }

    public byte getSimpleByte() {
        return simpleByte;
    }

    public short getSimpleShort() {
        return simpleShort;
    }

    public int getSimpleInt() {
        return simpleInt;
    }

    public long getSimpleLong() {
        return simpleLong;
    }

    public float getSimpleFloat() {
        return simpleFloat;
    }

    public double getSimpleDouble() {
        return simpleDouble;
    }

    public String getSimpleString() {
        return simpleString;
    }

    public Blob getSimpleByteArray() {
        return simpleByteArray;
    }
}
