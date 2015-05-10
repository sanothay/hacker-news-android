package com.dudev.android.hackernews;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dexafree.materialList.events.BusProvider;
import com.dudev.android.hackernews.Event.OnItemUpdateEvent;
import com.dudev.android.hackernews.Event.OnReplyItemUpdateEvent;
import com.dudev.android.hackernews.Util.CommonUtil;
import com.dudev.android.hackernews.Util.WebService;
import com.dudev.android.hackernews.model.Item;
import com.squareup.otto.Subscribe;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class DetailActivity extends ActionBarActivity {

    public static final String EXTRA_ITEM_DATA = "ITEM_DATA";

    private String itemId;
    private String authorHandle;
    private long time;
    private String title;
    private String comment;
    private List<String> comment_ids;
    private HashMap<Item, List<Item>> commentTable = new HashMap<Item, List<Item>>();

    private TextView titleTextView;
    private ListView commentListView;
    private List<Item> replyItems = new ArrayList<Item>();
    private List<Item> commentItems = new ArrayList<Item>();
    private Item currentCommentItem;
    private int replyCounter = 0;
    private int commentCounter = 0;
    private CommentAdapter commentAdapter;
    private LinearLayout loading_more_layout;
    public static final int MAX_COMMENT_SHOWN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getBundleExtra(EXTRA_ITEM_DATA);

        initFields(bundle);
        initUi();
        fillData();
    }

    private void fillData() {
        if (comment_ids == null || comment_ids.size() == 0) {
            Toast.makeText(DetailActivity.this, "No comments available yet", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDlg();

        replyItems.clear();

        if (comment_ids.size() >= MAX_COMMENT_SHOWN) {
            commentCounter = MAX_COMMENT_SHOWN;
        } else {
            commentCounter = comment_ids.size();
        }

        int firstCommentIndex = comment_ids.size() - commentCounter;
        String id = comment_ids.get(firstCommentIndex);
        if (CommonUtil.isCommentExisted(DetailActivity.this, id)) {
            // get all comments from db.
            getComments();
            setAdapter();
            hideProgressDlg();
        } else {

            // get all comments from webservice.
            getFirstComment(id);
        }
    }

    private void getComments() {

        for (int i = 0; i < comment_ids.size(); i++) {
            String commentId = comment_ids.get(i);
            Item comment = CommonUtil.getExistingComment(DetailActivity.this, commentId);
            if (comment == null) {
                continue;
            }
            commentItems.add(comment);
            Item[] replies = CommonUtil.getExistingReplies(DetailActivity.this, commentId);
            commentTable.put(comment, replies == null ? new ArrayList<Item>() : Arrays.asList(replies));
        }
    }

    private void getFirstComment(String firstCommentId) {
        new WebService(DetailActivity.this).getItem(firstCommentId);
    }

    private void showProgressDlg() {
        loading_more_layout.setVisibility(View.VISIBLE);
    }
    private void hideProgressDlg() {
        loading_more_layout.setVisibility(View.GONE);
    }

    @Subscribe
    public void updateReplyItemList(OnReplyItemUpdateEvent event) {

        if (event.getItem().getParent().equalsIgnoreCase(itemId)) {
            OnItemUpdateEvent e = new OnItemUpdateEvent(event.getItem());
            updateCommentItemList(e);
            return;
        }

        Item item = event.getItem();
        replyItems.add(item);
        --replyCounter;
        if (replyCounter == 0) {

            commentTable.put(currentCommentItem, replyItems);
            CommonUtil.saveReplies(DetailActivity.this, currentCommentItem, replyItems);

            if (commentCounter > 0) {
                getNextComment();
            } else {
                setAdapter();
                hideProgressDlg();
            }
        } else {
            getNextReply();
        }

    }

    private void getNextReply() {
        int index = currentCommentItem.getKids().length - replyCounter;
        String id = currentCommentItem.getKids()[index];
        new WebService(DetailActivity.this).getItem(id);
    }

    private void getNextComment() {
        int nextCommentIndex = comment_ids.size() - commentCounter;
        String id = comment_ids.get(nextCommentIndex);
        new WebService(DetailActivity.this).getItem(id);
    }

    private void setAdapter() {
        Item[] itemArr = commentItems.toArray(new Item[commentItems.size()]);
        commentAdapter = new CommentAdapter(DetailActivity.this, R.layout.comment_list_item,
                itemArr);
        for (int i = 0; i < commentItems.size(); i++) {
            Item currentComment = commentItems.get(i);
            List<Item> replies = commentTable.get(currentComment);
            if (replies != null) {
                for (int j = 0; j < replies.size(); j++) {
                    commentAdapter.addReplyItem(currentComment, replies.get(j));
                }
            }
        }

        SlideExpandableListAdapter adapter = new SlideExpandableListAdapter(
                commentAdapter,
                R.id.expandable_toggle_button,
                R.id.expandable);

        commentListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void updateCommentItemList(OnItemUpdateEvent event) {

        if (event.isHasError()) {
            return;
        }

        Item comment = event.getItem();
        commentItems.add(comment);
        currentCommentItem = comment;
        --commentCounter;

        CommonUtil.saveComment(DetailActivity.this, currentCommentItem);

        if (comment.getKids() != null && comment.getKids().length > 0) {
            getFirstReplies(comment);
        } else {
            if (commentCounter > 0) {
                int nextCommentIndex = comment_ids.size() - commentCounter;
                String id = comment_ids.get(nextCommentIndex);
                new WebService(DetailActivity.this).getItem(id);
            } else {
                setAdapter();
                hideProgressDlg();
            }
        }


    }

    private void getFirstReplies(Item comment) {
        replyCounter = comment.getKids().length;
        String id = comment.getKids()[0];
        new WebService(DetailActivity.this).getItem(id);
    }

    private void initUi() {
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setVisibility(View.GONE);
        commentListView = (ListView) findViewById(R.id.commentListView);
//        titleTextView.setText(Html.fromHtml(getOriginalComment()));
        loading_more_layout = (LinearLayout) findViewById(R.id.loading_more_layout);
    }

    private String getOriginalComment() {
        String full = "<p>" + title +  "<br>" + "By " + authorHandle + " " + CommonUtil.getTimePassed(time * 1000L)
                + "<br>" + comment + "</p>";

        return full;
    }

    private void initFields(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        itemId = bundle.getString(Item.TAG_ID);
        authorHandle = bundle.getString(Item.TAG_AUTHOR_HANDLE);
        time = bundle.getLong(Item.TAG_TIME);
        title = bundle.getString(Item.TAG_TITLE);
        comment = bundle.getString(Item.TAG_COMMENT);
        comment_ids = bundle.getStringArrayList(Item.TAG_COMMENT_IDS);
        Collections.sort(comment_ids);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getMainThreadBus().register(this);
    }

    @Override
    public void onPause() {
        super.onResume();
        BusProvider.getMainThreadBus().unregister(this);
    }
}
