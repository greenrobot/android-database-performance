package de.greenrobot.performance.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface IndexedStringEntityDao {

    @Insert
    void insert(List<IndexedStringEntity> entities);

    @Query("SELECT * FROM IndexedStringEntity WHERE indexedString = :value")
    List<IndexedStringEntity> withIndexedString(String value);

    @Query("DELETE FROM IndexedStringEntity")
    void deleteAll();

}
