package de.greenrobot.performance.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface SimpleEntityNotNullDao {

    @Insert
    void insert(SimpleEntityNotNull entity);

    @Update
    void update(SimpleEntityNotNull entity);

    @Query("DELETE FROM SimpleEntityNotNull")
    void deleteAll();

}
