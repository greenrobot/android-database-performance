package de.greenrobot.performance.kripton;

import com.abubusoft.kripton.android.annotation.BindDao;
import com.abubusoft.kripton.android.annotation.BindSqlDelete;
import com.abubusoft.kripton.android.annotation.BindSqlInsert;
import com.abubusoft.kripton.android.annotation.BindSqlSelect;

import java.util.List;

@BindDao(IndexedStringEntity.class)
public interface IndexedStringEntityDao {

    @BindSqlInsert
    void insert(IndexedStringEntity entity);

    @BindSqlSelect(jql="SELECT * FROM IndexedStringEntity WHERE indexedString = ${value}")
    List<IndexedStringEntity> withIndexedString(String value);

    @BindSqlDelete(jql="DELETE FROM IndexedStringEntity")
    void deleteAll();

}
