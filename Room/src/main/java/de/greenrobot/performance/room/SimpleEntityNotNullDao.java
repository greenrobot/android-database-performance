package de.greenrobot.performance.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SimpleEntityNotNullDao {

    @Insert
    void insert(SimpleEntityNotNull entity);

    @Insert
    void insert(List<SimpleEntityNotNull> entities);

    @Update
    void update(SimpleEntityNotNull entity);

    @Update
    void update(List<SimpleEntityNotNull> entities);

    @Query("SELECT * FROM SimpleEntityNotNull")
    List<SimpleEntityNotNull> getAll();

    @Query("DELETE FROM SimpleEntityNotNull")
    void deleteAll();

}
