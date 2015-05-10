package com.dudev.android.hackernews.Util;

import android.content.Context;
import android.util.Log;

import com.dudev.android.hackernews.model.Item;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by soulivanh on 5/6/15 AD.
 */
public class CommonUtil {

    public static final String DB_ITEMS = "DB_ITEMS";
    public static final String DB_ITEM_IDS = "DB_ITEM_IDS";

    public static final String TAG = CommonUtil.class.getSimpleName();

    public static String getTimePassed(long time) {
        long duration  = new java.util.Date().getTime() - new java.util.Date(time).getTime();
        return TimeUtils.millisToLongDHMS(duration);
    }

    public static void saveItems(Context context, Item[] items) {

        try {
            DB db = DBFactory.open(context);
            db.put(DB_ITEMS, items);
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
    }

    public static Item[] getItems(Context context) {
        Item[] items = null;
        try {
            DB db = DBFactory.open(context);
            if (db.exists(DB_ITEMS)) {
                items = db.getObjectArray(DB_ITEMS, Item.class);
            }
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }

        return items;
    }

    public static boolean hasLoadedItems(Context context) {
        boolean exist = false;
        try {
            DB db = DBFactory.open(context);
            if (db.exists(DB_ITEMS)) {
                exist = true;
            }
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }

        return exist;
    }

    public static void saveItemIds(Context context, Integer[] ids) {
        try {
            DB db = DBFactory.open(context);
            db.put(DB_ITEM_IDS, ids);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
    }

    public static final int[] getItemIds(Context context) {
        int[] ids = null;
        try {
            DB db = DBFactory.open(context);
            Object[] list = db.getObjectArray(DB_ITEM_IDS, Integer.class);
            db.close();
            ids = new int[list.length];
            for (int i = 0; i < list.length; i++) {
                ids[i] = (int)list[i];
            }
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }

        return ids;
    }

    public static Integer[] getIntegerAsArray(int[] arr) {
        Integer[] ints = new Integer[arr.length];
        for(int i = 0; i < arr.length; i++){
            ints[i] = new Integer(arr[i]);
        }

        return ints;
    }

    public static Integer[] sortDescending(Integer[] arr) {
        Arrays.sort(arr, Collections.reverseOrder());
        return arr;
    }

    public static Item[] reverseOrder(Item[] arr) {
        Arrays.sort(arr, Collections.reverseOrder());
        return arr;
    }

    public static <T> void removeDuplicates(List<Item> list) {
        final Set<Item> encountered = new HashSet<>();
        for (Iterator<Item> iter = list.iterator(); iter.hasNext(); ) {
            final Item t = iter.next();
            final boolean first = encountered.add(t);
            if (!first) {
                iter.remove();
            }
        }
    }

    public static void delExistingItems(Context context) {
        try {
            DB db = DBFactory.open(context);
            db.del(DB_ITEMS);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
    }

    public static void saveComment(Context context, Item comment) {
        if (comment == null || comment.getId() == null) {
            return;
        }

        try {
            DB db = DBFactory.open(context);
            // Save comment.
            db.put(comment.getId(), comment);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
    }

    public static void saveReplies(Context context, Item comment, List<Item> replies) {
        if (comment == null || comment.getId() == null) {
            return;
        }

        try {
            DB db = DBFactory.open(context);
            Item[] replytemp;
            if (replies == null || replies.size() == 0) {
                replytemp = new Item[0];
            } else {
                replytemp = replies.toArray(new Item[replies.size()]);
            }

            // Save comment associated replies.
            db.put(comment.getId() + ":REPLIES", replytemp);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
    }



    public static boolean isCommentExisted(Context context, String id) {

        boolean isExisted = false;
        try {
            DB db = DBFactory.open(context);
            isExisted = db.exists(id);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
        return isExisted;
    }

    public static Item getExistingComment(Context context, String id) {

        Item comment = null;
        try {
            DB db = DBFactory.open(context);
            comment = db.getObject(id, Item.class);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }
        return comment;
    }

    public static boolean isReplyExisted(Context context, String id) {
        boolean isExisted = false;
        try {
            DB db = DBFactory.open(context);
            isExisted = db.exists(id + ":REPLIES");
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }

        return isExisted;
    }

    public static Item[] getExistingReplies(Context context, String commentId) {
        Item[] replies = null;
        try {
            DB db = DBFactory.open(context);
            replies = db.getObjectArray(commentId + ":REPLIES", Item.class);
            db.close();
        } catch (SnappydbException e) {
            Log.e(TAG, "" + e);
        }

        return replies;
    }
}
