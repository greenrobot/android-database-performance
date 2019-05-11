package de.greenrobot.performance.sugarorm;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class SimpleEntityNotNull extends SugarRecord {

    @Unique
    long id;

    boolean simpleBoolean;
    int simpleByte;
    short simpleShort;
    int simpleInt;
    long simpleLong;
    float simpleFloat;
    double simpleDouble;
    /** Not-null value. */
    String simpleString;
    /** Storing lists and arrays not supported yet */
//    byte[] simpleByteArray;
    String replaceByteArray;

    public SimpleEntityNotNull(long id, boolean simpleBoolean, int simpleByte, short simpleShort, int simpleInt, long simpleLong, float simpleFloat, double simpleDouble, String simpleString, String replaceByteArray) {
        this.id = id;
        this.simpleBoolean = simpleBoolean;
        this.simpleByte = simpleByte;
        this.simpleShort = simpleShort;
        this.simpleInt = simpleInt;
        this.simpleLong = simpleLong;
        this.simpleFloat = simpleFloat;
        this.simpleDouble = simpleDouble;
        this.simpleString = simpleString;
        this.replaceByteArray = replaceByteArray;

    }

    public SimpleEntityNotNull() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSimpleBoolean(boolean simpleBoolean) {
        this.simpleBoolean = simpleBoolean;
    }

    public void setSimpleByte(int simpleByte) {
        this.simpleByte = simpleByte;
    }

    public void setSimpleShort(short simpleShort) {
        this.simpleShort = simpleShort;
    }

    public void setSimpleInt(int simpleInt) {
        this.simpleInt = simpleInt;
    }

    public void setSimpleLong(long simpleLong) {
        this.simpleLong = simpleLong;
    }

    public void setSimpleFloat(float simpleFloat) {
        this.simpleFloat = simpleFloat;
    }

    public void setSimpleDouble(double simpleDouble) {
        this.simpleDouble = simpleDouble;
    }

    public void setSimpleString(String simpleString) {
        this.simpleString = simpleString;
    }

    public void setReplaceByteArray(String replaceByteArray) {
        this.replaceByteArray = replaceByteArray;
    }

    @Override
    public Long getId() {
        return id;
    }

    public boolean getSimpleBoolean() {
        return simpleBoolean;
    }

    public int getSimpleByte() {
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

    public String getReplaceByteArray() {
        return replaceByteArray;
    }
}
