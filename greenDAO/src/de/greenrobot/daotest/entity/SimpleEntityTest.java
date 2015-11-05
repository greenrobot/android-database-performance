package de.greenrobot.daotest.entity;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import de.greenrobot.daotest.SimpleEntity;
import de.greenrobot.daotest.SimpleEntityDao;

public class SimpleEntityTest extends AbstractDaoTestLongPk<SimpleEntityDao, SimpleEntity> {

    public SimpleEntityTest() {
        super(SimpleEntityDao.class);
    }

    @Override
    protected SimpleEntity createEntity(Long key) {
        SimpleEntity entity = new SimpleEntity();
        entity.setId(key);
        return entity;
    }

}
