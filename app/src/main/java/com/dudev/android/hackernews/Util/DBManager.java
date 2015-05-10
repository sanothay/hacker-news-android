package com.dudev.android.hackernews.Util;

import android.content.Context;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

/**
 * Created by soulivanh on 5/6/15 AD.
 */
public class DBManager {
    private static DB snappyDb;

    public static DB getDb(Context context) throws SnappydbException {

        if (snappyDb == null) {
            snappyDb = DBFactory.open(context);
        }

        return snappyDb;
    }
}
