package de.greenrobot.performance.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ColumnInfo.BLOB;


/** 
 * Entity mapped to table SIMPLE_ENTITY_NOT_NULL (schema version 1).
*/
@Entity(tableName = "SIMPLE_ENTITY_NOT_NULL")
public class SimpleEntityNotNull {

    @PrimaryKey
    private long id;
    
    @ColumnInfo(name="SIMPLE_BOOLEAN")
    private boolean simpleBoolean;
    @ColumnInfo(name="SIMPLE_BYTE")
    private byte simpleByte;
    @ColumnInfo(name="SIMPLE_SHORT")
    private short simpleShort;
    @ColumnInfo(name="SIMPLE_INT")
    private int simpleInt;
    @ColumnInfo(name="SIMPLE_LONG")
    private long simpleLong;
    @ColumnInfo(name="SIMPLE_FLOAT")
    private float simpleFloat;
    @ColumnInfo(name="SIMPLE_DOUBLE")
    private double simpleDouble;
    @ColumnInfo(name="SIMPLE_STRING")
    private String simpleString;
    @ColumnInfo(typeAffinity=BLOB, name="SIMPLE_BYTE_ARRAY")
    private byte[] simpleByteArray; 
    
    public long getId() {
        return id;
    } 

    public void setId(long id) {
        this.id = id;
    } 

    public boolean getSimpleBoolean() {
        return simpleBoolean;
    } 

    public void setSimpleBoolean(boolean simpleBoolean) {
        this.simpleBoolean = simpleBoolean;
    } 

    public byte getSimpleByte() {
        return simpleByte;
    } 

    public void setSimpleByte(byte simpleByte) {
        this.simpleByte = simpleByte;
    } 

    public short getSimpleShort() {
        return simpleShort;
    } 

    public void setSimpleShort(short simpleShort) {
        this.simpleShort = simpleShort;
    } 

    public int getSimpleInt() {
        return simpleInt;
    } 

    public void setSimpleInt(int simpleInt) {
        this.simpleInt = simpleInt;
    } 

    public long getSimpleLong() {
        return simpleLong;
    } 

    public void setSimpleLong(long simpleLong) {
        this.simpleLong = simpleLong;
    } 

    public float getSimpleFloat() {
        return simpleFloat;
    } 

    public void setSimpleFloat(float simpleFloat) {
        this.simpleFloat = simpleFloat;
    } 

    public double getSimpleDouble() {
        return simpleDouble;
    } 

    public void setSimpleDouble(double simpleDouble) {
        this.simpleDouble = simpleDouble;
    } 

    public String getSimpleString() {
        return simpleString;
    } 

    public void setSimpleString(String simpleString) {
        this.simpleString = simpleString;
    } 

    public byte[] getSimpleByteArray() {
        return simpleByteArray;
    } 

    public void setSimpleByteArray(byte[] simpleByteArray) {
        this.simpleByteArray = simpleByteArray;
    } 

}
