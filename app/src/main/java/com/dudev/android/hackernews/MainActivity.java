package com.dudev.android.hackernews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.dexafree.materialList.cards.BasicButtonsCard;
import com.dexafree.materialList.cards.BasicImageButtonsCard;
import com.dexafree.materialList.cards.BasicListCard;
import com.dexafree.materialList.cards.BigImageButtonsCard;
import com.dexafree.materialList.cards.BigImageCard;
import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.cards.WelcomeCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.events.BusProvider;
import com.dexafree.materialList.events.DataSetChangedEvent;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.dudev.android.hackernews.Event.OnItemUpdateEvent;
import com.dudev.android.hackernews.Event.OnTopStoryIdsUpdateEvent;
import com.dudev.android.hackernews.Util.CommonUtil;
import com.dudev.android.hackernews.Util.ListUtils;
import com.dudev.android.hackernews.Util.WebService;
import com.dudev.android.hackernews.listener.EndlessRecyclerOnScrollListener;
import com.dudev.android.hackernews.model.Item;
import com.dudev.android.hackernews.model.ItemType;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.dudev.android.hackernews.R.id.swipeRefreshLayout;

public class MainActivity extends ActionBarActivity {

    public static final int MAX_ROW_NUM = 50;
    private int counter = 0;
    private Context mContext;
    private MaterialListView mListView;
    private List<Item> newItemList = new ArrayList<Item>();
    private List<Integer> itemIdList = new ArrayList<Integer>();
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private WeakReference<MainActivity> activityWeakReference;
    private EndlessRecyclerOnScrollListener endlessScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
        @Override
        public void onLoadMore(int current_page) {

           // Not required!
        }
    };
    private PullRefreshLayout layout;
    private boolean forceRefresh;
    private ProgressWheel progressWheelWheel;
    private LinearLayout loading_more_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityWeakReference = new WeakReference<MainActivity>(this);
        // Save a reference to the context
        mContext = this;

        // Bind the MaterialListView to a variable
        mListView = (MaterialListView) findViewById(R.id.material_listview);
        progressWheelWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        progressWheelWheel.setBarColor(Color.BLUE);
        loading_more_layout = (LinearLayout) findViewById(R.id.loading_more_layout);

        // Set the dismiss listener
        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                // Recover the tag linked to the Card
                String tag = card.getTag().toString();
            }
        });

        // Add the ItemTouchListener
        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
                Item item = (Item)view.getTag();
                Bundle bundle = getItemAsBundle(item);
                Intent detailPage = new Intent(MainActivity.this, DetailActivity.class);
                detailPage.putExtra(DetailActivity.EXTRA_ITEM_DATA, bundle);
                startActivity(detailPage);
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                Log.d("LONG_CLICK", view.getTag().toString());
            }
        });

        layout = (PullRefreshLayout) findViewById(swipeRefreshLayout);
        layout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh = true;
                endlessScrollListener.forceReset();
                // start refresh
                itemIdList.clear();
                new WebService(MainActivity.this).getTopStoryIds();
            }
        });

        mListView.setLayoutManager(linearLayoutManager);
        mListView.setOnScrollListener(endlessScrollListener);

        // Directly fetch data from database if ever been loaded.
        if (CommonUtil.hasLoadedItems(this)) {
            Item[] items = CommonUtil.getItems(this);
            if (items != null || items.length > 0) {
                loading_more_layout.setVisibility(View.GONE);

                itemIdList.clear();
                int[] ids = CommonUtil.getItemIds(MainActivity.this);
                Integer[] tmp = CommonUtil.getIntegerAsArray(ids);
                for (int i = 0; i < tmp.length; i++) {
                    itemIdList.add(tmp[i]);
                }

                // Update + display all existing data on the list.
                updateItemList(items);

            } else {
                loading_more_layout.setVisibility(View.VISIBLE);
                new WebService(MainActivity.this).getTopStoryIds();
            }
        } else {

            // We haven't been loaded, let's get the most recent top story ids.
            // consequently, also get item data for displaying on the list.
            loading_more_layout.setVisibility(View.VISIBLE);
            new WebService(MainActivity.this).getTopStoryIds();
        }
    }

    private Bundle getItemAsBundle(Item item) {
        Bundle bundle = new Bundle();
        bundle.putString(Item.TAG_ID, item.getId());
        bundle.putString(Item.TAG_AUTHOR_HANDLE, item.getBy());
        bundle.putLong(Item.TAG_TIME, item.getTime());
        bundle.putString(Item.TAG_COMMENT, TextUtils.isEmpty(item.getText()) ? "" : item.getText());
        if (item.getKids() == null || item.getKids().length == 0) {
            bundle.putStringArrayList(Item.TAG_COMMENT_IDS, new ArrayList<String>());
        } else {
            bundle.putStringArrayList(Item.TAG_COMMENT_IDS, new ArrayList<String>(Arrays.asList(item.getKids())));
        }
        bundle.putString(Item.TAG_TITLE, item.getTitle());
        return bundle;
    }

    public void updateItemList(Item[] items) {
        if (items == null || items.length == 0) {
            return;
        }

        TreeMap<Integer, Item> sortedMap = getNonDuplicatedAndSortedItem(items);
        int  counter = 0;
        for (Integer integer : sortedMap.keySet()) {
            mListView.add(getCard(sortedMap.get(integer), ++counter));
        }
        mListView.onNotifyDataSetChanged(new DataSetChangedEvent());
        itemIdList.clear();
    }

    private TreeMap<Integer, Item> getNonDuplicatedAndSortedItem(Item[] items) {
        List<Item> nonDuplicatedList = Arrays.asList(items);
        HashSet hashSett = new HashSet(nonDuplicatedList);

        HashMap<Integer, Item> tbl = new HashMap<Integer, Item>();
        Iterator<Item> iter = hashSett.iterator();
        while(iter.hasNext()) {
            Item i = iter.next();
            tbl.put(Integer.parseInt(i.getId()), i);
        }

        TreeMap<Integer, Item> sortedMap = new TreeMap<Integer, Item>(Collections.reverseOrder());
        for (Map.Entry entry : tbl.entrySet()) {
            sortedMap.put((Integer) entry.getKey(), (Item)entry.getValue());
        }

        return sortedMap;
    }


    @Subscribe
    public void onTopStoryIdsUpdate(OnTopStoryIdsUpdateEvent event) {

        // Keep trying until success, interval at 1 minute.
        if (event.isHasError()) {
            Toast.makeText(MainActivity.this, "Communicating with server failed, will try " +
                    "again in 1 minute", Toast.LENGTH_LONG).show();
            loading_more_layout.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading_more_layout.setVisibility(View.VISIBLE);
                    new WebService(MainActivity.this).getTopStoryIds();
                }
            }, 1000 * 60);
            return;
        }

        System.out.println("total ids: " + event.getIds().length);
        int[] ids = event.getIds();
        Integer[] tmp = CommonUtil.getIntegerAsArray(ids);
        tmp = CommonUtil.sortDescending(tmp);
        itemIdList.clear();
        for (int i = 0; i < tmp.length; i++) {
            itemIdList.add(tmp[i]);
        }
        CommonUtil.saveItemIds(MainActivity.this, tmp);

        if (itemIdList.size() > MAX_ROW_NUM) {
            counter = MAX_ROW_NUM;
        } else {
            counter = itemIdList.size();
        }

        if (counter > 0) {
            newItemList.clear();
            int id = itemIdList.get(counter - 1);
            new WebService(this).getItem("" + id);
            itemIdList.remove(counter - 1);
        }
    }

    @Subscribe
    public void updateItemList(OnItemUpdateEvent event) {

        boolean shouldIgnore = false;
        if (event.getItem() == null || TextUtils.isEmpty(event.getItem().getType())) {
            shouldIgnore = true;
        }
        if (ItemType.valueOf(event.getItem().getType().toUpperCase()) != ItemType.STORY) {
            shouldIgnore = true;
        }

        // skip the item if error occurred.
        // move to next item on the list.
        if (!shouldIgnore && !event.isHasError()) {
            newItemList.add(event.getItem());
        }

        --counter;
        if (counter == 0) {
            // If force refresh by the user, clear the list
            // and existing data. Start new...
            if (forceRefresh) {
                mListView.clear();
                CommonUtil.delExistingItems(MainActivity.this);
                forceRefresh = false;
            } else {
                loading_more_layout.setVisibility(View.GONE);
            }

            // Merge existing data if any with new ones.
            Item[] existingItemList = CommonUtil.getItems(MainActivity.this);
            List<Item> allItemList = new ArrayList<Item>();
            if (existingItemList == null) {
                for (Item item : newItemList) {
                    allItemList.add(item);
                }
            } else {
                for (Item item : existingItemList) {
                    allItemList.add(item);
                }

                for (Item item : newItemList) {
                    allItemList.add(item);
                }
            }

            // Remove duplicated items on the list if any.
            ListUtils.removeDuplicate(allItemList);

            // Sort items by date of presence.
            Collections.sort(allItemList, Item.itemComparator);

            // Store all items (existing + new) in database for data caching.
            CommonUtil.saveItems(MainActivity.this, allItemList.toArray(new Item[allItemList.size()]));

            // Display all items (existing + new) on the list.
            mListView.clear();
            for (int i = 0; i < allItemList.size(); i++) {
                mListView.add(getCard(allItemList.get(i), i + 1));
            }
            mListView.onNotifyDataSetChanged(new DataSetChangedEvent());
            layout.setRefreshing(false);
        } else {

            // Get next item data on the list.
            int nextItemIndex = counter - 1;
            new WebService(this).getItem("" + itemIdList.get(nextItemIndex));
            itemIdList.remove(nextItemIndex);
        }
    }

    private Card getCard(Item item, int index) {
        SimpleCard card;
        card = new BasicButtonsCard(this);
        card.setDescription(item.getText());
        card.setTitle(item.getTitle());
        card.setTag(item);
        ((BasicButtonsCard) card).setPoint(item.getScore() + " points");
        ((BasicButtonsCard) card).setAuthor("by " + item.getBy());
        ((BasicButtonsCard) card).setTime(CommonUtil.getTimePassed(item.getTime() * 1000L) + " ago");
        ((BasicButtonsCard) card).setUrl(item.getUrl());
        ((BasicButtonsCard) card).setRightButtonText("Open Website");
        ((BasicButtonsCard) card).setRightButtonTextColorRes(R.color.accent_material_dark);
        ((BasicButtonsCard) card).setOnRightButtonPressedListener(new OnButtonPressListener() {
            @Override
            public void onButtonPressedListener(View view, Card card) {
                BasicButtonsCard c = (BasicButtonsCard)card;
                if (!TextUtils.isEmpty(c.getUrl())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(c.getUrl()));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Sorry website is currently not available", LENGTH_SHORT).show();
                }
            }
        });
        card.setDismissible(true);

        return card;
    }
    @Override
    public void onResume() {
        super.onResume();

        if (activityWeakReference.get() != null && !activityWeakReference.get().isFinishing()) {
            endlessScrollListener.reset(0, true);
            BusProvider.getMainThreadBus().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (activityWeakReference.get() != null && !activityWeakReference.get().isFinishing()) {
            endlessScrollListener.reset(0, true);
            BusProvider.getMainThreadBus().unregister(this);
        }
    }


    private void fillArray() {
        for (int i = 0; i < 5; i++) {
            Card card = getRandomCard(i);
            mListView.add(card);
        }
    }

    private Card getRandomCard(final int position) {
        String title = "Card number " + (position + 1);
        String description = "Lorem ipsum dolor sit amet";

        int type = position % 6;

        SimpleCard card;
        Drawable icon;

        switch (type) {

            case 0:
                card = new SmallImageCard(this);
                card.setDescription(description);
                card.setTitle(title);
                card.setDrawable(R.drawable.ic_launcher);
                card.setDismissible(true);
                card.setTag("SMALL_IMAGE_CARD");
                return card;

            case 1:
                card = new BigImageCard(this);
                card.setDescription(description);
                card.setTitle(title);
                //card.setDrawable(R.drawable.photo);
                card.setDrawable("https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png");
                card.setTag("BIG_IMAGE_CARD");
                return card;

            case 2:
                card = new BasicImageButtonsCard(this);
                card.setDescription(description);
                card.setTitle(title);
                card.setDrawable(R.drawable.dog);
                card.setTag("BASIC_IMAGE_BUTTON_CARD");
                ((BasicImageButtonsCard) card).setLeftButtonText("LEFT");
                ((BasicImageButtonsCard) card).setRightButtonText("RIGHT");

                if (position % 2 == 0)
                    ((BasicImageButtonsCard) card).setDividerVisible(true);

                ((BasicImageButtonsCard) card).setOnLeftButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "You have pressed the left button", LENGTH_SHORT).show();
                        ((SimpleCard) card).setTitle("CHANGED ON RUNTIME");
                    }
                });

                ((BasicImageButtonsCard) card).setOnRightButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "You have pressed the right button on card " + ((SimpleCard) card).getTitle(), LENGTH_SHORT).show();
                        mListView.remove(card);
                    }
                });
                card.setDismissible(true);

                return card;

            case 3:
                card = new BasicButtonsCard(this);
                card.setDescription(description);
                card.setTitle(title);
                card.setTag("BASIC_BUTTONS_CARD");
                ((BasicButtonsCard) card).setLeftButtonText("LEFT");
                ((BasicButtonsCard) card).setRightButtonText("RIGHT");
                ((BasicButtonsCard) card).setRightButtonTextColorRes(R.color.accent_material_dark);

                if (position % 2 == 0)
                    ((BasicButtonsCard) card).setDividerVisible(true);

                ((BasicButtonsCard) card).setOnLeftButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "You have pressed the left button", LENGTH_SHORT).show();
                    }
                });

                ((BasicButtonsCard) card).setOnRightButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "You have pressed the right button", LENGTH_SHORT).show();
                    }
                });
                card.setDismissible(true);


                return card;

            case 4:
                card = new WelcomeCard(this);
                card.setTitle("Welcome Card");
                card.setDescription("I am the description");
                card.setTag("WELCOME_CARD");
                ((WelcomeCard) card).setSubtitle("My subtitle!");
                ((WelcomeCard) card).setButtonText("Okay!");
                ((WelcomeCard) card).setOnButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "Welcome!", LENGTH_SHORT).show();
                    }
                });

                if (position % 2 == 0)
                    ((WelcomeCard) card).setBackgroundColorRes(R.color.background_material_dark);
                card.setDismissible(true);

                return card;

            case 5:
                card = new BasicListCard(this);
                card.setTitle("List Card");
                card.setDescription("Take a itemIdList");
                BasicListAdapter adapter = new BasicListAdapter(this);
                adapter.add("Text1");
                adapter.add("Text2");
                adapter.add("Text3");
                card.setTag("LIST_CARD");
                ((BasicListCard) card).setAdapter(adapter);
                /*
                ((BasicListCard) card).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    	// Do what ever you want...
                    }
                });
                */
                card.setDismissible(true);

                return card;

            default:
                card = new BigImageButtonsCard(this);
                card.setDescription(description);
                card.setTitle(title);
                card.setDrawable(R.drawable.photo);
                card.setTag("BIG_IMAGE_BUTTONS_CARD");
                ((BigImageButtonsCard) card).setLeftButtonText("ADD CARD");
                ((BigImageButtonsCard) card).setRightButtonText("RIGHT BUTTON");

                if (position % 2 == 0) {
                    ((BigImageButtonsCard) card).setDividerVisible(true);
                }

                ((BigImageButtonsCard) card).setOnLeftButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        Log.d("ADDING", "CARD");

                        mListView.add(generateNewCard());
                        makeText(mContext, "Added new card", LENGTH_SHORT).show();
                    }
                });

                ((BigImageButtonsCard) card).setOnRightButtonPressedListener(new OnButtonPressListener() {
                    @Override
                    public void onButtonPressedListener(View view, Card card) {
                        makeText(mContext, "You have pressed the right button", LENGTH_SHORT).show();
                    }
                });
                card.setDismissible(true);


                return card;

        }

    }

    private Card generateNewCard() {
        SimpleCard card = new BasicImageButtonsCard(this);
        card.setDrawable(R.drawable.dog);
        card.setTitle("I'm new");
        card.setDescription("I've been generated on runtime!");
        card.setTag("BASIC_IMAGE_BUTTONS_CARD");

        return card;
    }

    private void addMockCardAtStart(){
        BasicImageButtonsCard card = new BasicImageButtonsCard(this);
        card.setDrawable(R.drawable.dog);
        card.setTitle("Hi there");
        card.setDescription("I've been added on top!");
        card.setLeftButtonText("LEFT");
        card.setRightButtonText("RIGHT");
        card.setTag("BASIC_IMAGE_BUTTONS_CARD");

        card.setDismissible(true);

        mListView.addAtStart(card);
    }
}
