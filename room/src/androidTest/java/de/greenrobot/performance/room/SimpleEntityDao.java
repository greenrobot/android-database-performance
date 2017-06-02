package de.greenrobot.performance.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SimpleEntityDao {
    @Query("SELECT * FROM SIMPLE_ENTITY_NOT_NULL")
    List<SimpleEntityNotNull> getAll();

    @Insert
    void insertAll(List<SimpleEntityNotNull> entities);

    @Insert
    void insert(SimpleEntityNotNull entity);

    @Update
    void update(SimpleEntityNotNull entity);

    @Update
    void updateAll(List<SimpleEntityNotNull> list);

    @Query("DELETE FROM SIMPLE_ENTITY_NOT_NULL")
    void deleteAll();
}