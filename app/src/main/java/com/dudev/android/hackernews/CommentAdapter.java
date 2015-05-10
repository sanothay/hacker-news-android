package com.dudev.android.hackernews;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudev.android.hackernews.Util.CommonUtil;
import com.dudev.android.hackernews.model.Item;
import com.dudev.android.hackernews.widget.NoteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by soulivanh on 5/8/15 AD.
 */
public class CommentAdapter extends BaseAdapter {

    private Item[] items;
    private Context context;
    private TextView rowHeaderTextView;
    private TextView commentTextView;
    private LinearLayout expandable;
    private Button expandable_toggle_button;
    private List<List<Item>> replyItems = new ArrayList<List<Item>>();

    public CommentAdapter(Context context, int resource, Item[] items) {
        super();

        Arrays.sort(items, Item.itemComparator);
        this.items = items;
        this.context = context;

        for (int i = 0; i < items.length; i++) {
            replyItems.add(new ArrayList<Item>());
        }
    }

    @Override
    public Item getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(items[position].getId());
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = null;
        view = LayoutInflater.from(context).inflate(R.layout.comment_list_item, parent, false);

        rowHeaderTextView = (TextView)view.findViewById(R.id.rowHeaderTextView);
        commentTextView = (TextView)view.findViewById(R.id.commentTextView);
        expandable = (LinearLayout)view.findViewById(R.id.expandable);
        expandable_toggle_button = (Button) view.findViewById(R.id.expandable_toggle_button);
        if (replyItems.get(position).size() == 0) {
            expandable_toggle_button.setVisibility(View.GONE);
        }

        fillData(items[position]);

        // Sort descending.
        List<Item> sortedReplyItems = replyItems.get(position);
        Collections.sort(sortedReplyItems, Item.itemComparator);
        for (int i = 0; i < sortedReplyItems.size(); i++) {
            String replyItemHeader = getItemHeader(sortedReplyItems.get(i));
            NoteTextView t = new NoteTextView(expandable.getContext());
            t.setText(Html.fromHtml(replyItemHeader + sortedReplyItems.get(i).getText()));

            LinearLayout ll = new LinearLayout(expandable.getContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(0, 0, 0, 50);

            expandable.addView(t, layoutParams);
        }

        return view;
    }

    private String getItemHeader(Item item) {
        String src = "";
        if (TextUtils.isEmpty(item.getTitle())) {
            src = String.format("<p><b>By %s %s ago</b></p>", item.getBy(),
                    CommonUtil.getTimePassed(item.getTime() * 1000L));
        } else {
            src = String.format("<p><b>By %s %s ago</b></p><p>%s</p>", item.getBy(),
                    CommonUtil.getTimePassed(item.getTime() * 1000L), item.getTitle());
        }

        return src;
    }

    public void addReplyItem(Item commentItem, Item replyItem) {
        int index = getCommentItemIndex(commentItem);
        if (index != -1) {
            replyItems.get(index).add(replyItem);
        }
    }

    private int getCommentItemIndex(Item commentItem) {
        int counter = -1;
        for (Item item : items) {
            ++counter;
            if (item.equals(commentItem)) {
                return counter;
            }
        }

        return -1;
    }

    private void fillData(Item item) {
        rowHeaderTextView.setText(Html.fromHtml(getItemHeader(item)));
        commentTextView.setText(TextUtils.isEmpty(item.getText()) ? "" : Html.fromHtml(item.getText()));
    }
}
