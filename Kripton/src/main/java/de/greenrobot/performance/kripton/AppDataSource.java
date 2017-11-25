package de.greenrobot.performance.kripton;

import com.abubusoft.kripton.android.annotation.BindDataSource;

@BindDataSource(daoSet = {IndexedStringEntityDao.class, SimpleEntityNotNullDao.class}, fileName = "kripton-test.db", version = 1, log = false)
public interface AppDataSource {


}
