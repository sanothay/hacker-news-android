package com.dudev.android.hackernews.Util;

import android.content.Context;
import android.util.Log;

import com.dexafree.materialList.events.BusProvider;
import com.dudev.android.hackernews.Event.OnItemUpdateEvent;
import com.dudev.android.hackernews.Event.OnReplyItemUpdateEvent;
import com.dudev.android.hackernews.Event.OnTopStoryIdsUpdateEvent;
import com.dudev.android.hackernews.R;
import com.dudev.android.hackernews.model.Item;
import com.dudev.android.hackernews.model.ItemType;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public class WebService implements HackerNewsInterface {

    public static final int MAX_NUM_OF_IDS = 10;
    private Context context;
    public WebService(Context context) {
        this.context = context;
    }

    @Override
    public void getItem(String id) {
        RestClient.getInstance(context).get(context.getString(R.string.get_item_method, id),
                null,
                new AsyncHttpResponseHandler() {


                    @Override
                    public void onSuccess(int code, Header[] headers, byte[] response) {
                        try {
                            String res = new String(response);
                            System.out.println(res);
                            JSONObject obj = new JSONObject(res);
                            Gson g = new Gson();
                            Item item = g.fromJson(res, Item.class);

                            if (ItemType.valueOf(item.getType().toUpperCase()) == ItemType.COMMENT) {
                                BusProvider.getMainThreadBus().post(new OnReplyItemUpdateEvent(item));
                            } else {
                                BusProvider.getMainThreadBus().post(new OnItemUpdateEvent(item));
                            }


                        } catch (Exception e) {
                            onFailure(code, headers, response, e);
                        }
                    }

                    @Override
                    public void onFailure(int code, Header[] headers, byte[] response, Throwable e) {
                        Log.e("ERR", "" + e);
                        OnItemUpdateEvent event = new OnItemUpdateEvent(null);
                        event.setHasError(true);
                        BusProvider.getMainThreadBus().post(event);
                    }
                });
    }

    @Override
    public void getMaxItemId() {
        ;
    }

    @Override
    public void getTopStoryIds() {

        RestClient.getInstance(context).get(context.getString(R.string.get_top_story_ids_method),
                null,
                new AsyncHttpResponseHandler() {


                    @Override
                    public void onSuccess(int code, Header[] headers, byte[] response) {
                        try {
                            JSONArray obj = new JSONArray(new String(response));
                            int[] ids = new int[obj.length()];
                            for (int i = 0; i < obj.length(); i++) {
                                System.out.println(obj.get(i));
                                ids[i] = Integer.valueOf(obj.get(i).toString());
                            }

                            BusProvider.getMainThreadBus().post(new OnTopStoryIdsUpdateEvent(ids));

                        } catch (Exception e) {
                            onFailure(code, headers, response, e);
                        }
                    }

                    @Override
                    public void onFailure(int code, Header[] headers, byte[] response, Throwable e) {
                        Log.e("ERR", "" + e);
                        OnTopStoryIdsUpdateEvent event = new OnTopStoryIdsUpdateEvent(null);
                        event.setHasError(true);
                        BusProvider.getMainThreadBus().post(event);
                    }
                });

    }

}
