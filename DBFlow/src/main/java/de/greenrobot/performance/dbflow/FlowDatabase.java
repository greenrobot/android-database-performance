package de.greenrobot.performance.dbflow;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = FlowDatabase.NAME, version = FlowDatabase.VERSION)
public class FlowDatabase {

    public static final String NAME = "flowdb";

    public static final int VERSION = 1;
}
