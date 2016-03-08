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

    @PrimaryKey(autoincrement = false)
    public long _id;
    @Column
    public boolean simpleBoolean;
    @Column
    public byte simpleByte;
    @Column
    public short simpleShort;
    @Column
    public int simpleInt;
    @Column
    public long simpleLong;
    @Column
    public float simpleFloat;
    @Column
    public double simpleDouble;
    @Column
    public String simpleString;
    @Column
    public Blob simpleByteArray;
}
