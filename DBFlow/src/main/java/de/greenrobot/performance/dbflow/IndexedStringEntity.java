package de.greenrobot.performance.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Simple entity with a string property that is indexed.
 */
@Table(database = FlowDatabase.class)
public class IndexedStringEntity extends BaseModel {

    @PrimaryKey(autoincrement = false)
    public long _id;

    @Column @Index
    public String indexedString;

}
