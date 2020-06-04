package de.greenrobot.performance.kripton;

import com.abubusoft.kripton.android.annotation.BindDao;
import com.abubusoft.kripton.android.annotation.BindSqlDelete;
import com.abubusoft.kripton.android.annotation.BindSqlInsert;
import com.abubusoft.kripton.android.annotation.BindSqlSelect;
import com.abubusoft.kripton.android.annotation.BindSqlUpdate;

import java.util.List;

@BindDao(SimpleEntityNotNull.class)
public interface SimpleEntityNotNullDao {

    @BindSqlInsert
    void insert(SimpleEntityNotNull entity);

    @BindSqlUpdate(where="id=${entity.id}")
    void update(SimpleEntityNotNull entity);

    @BindSqlSelect
    List<SimpleEntityNotNull> getAll();

    @BindSqlDelete
    void deleteAll();

}
