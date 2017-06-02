package de.greenrobot.performance.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndexedEntityDao {
    @Insert
    void insertAll(List<IndexedStringEntity> entities);

    @Query("SELECT * FROM INDEXED_STRING_ENTITY WHERE INDEXED_STRING = :indexedString")
    List<IndexedStringEntity> getAllByString(String indexedString);

    @Query("DELETE FROM INDEXED_STRING_ENTITY")
    void deleteAll();
}