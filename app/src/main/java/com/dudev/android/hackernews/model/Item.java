package com.dudev.android.hackernews.model;

import com.dudev.android.hackernews.Util.CommonUtil;

import org.joda.time.DateTime;

import java.util.Comparator;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public class Item implements Comparator<Item> {

    public static Comparator<Item> itemComparator = new Comparator<Item>() {
        @Override
        public int compare(Item item1, Item item2) {

            DateTime dateTime1 = new DateTime(item1.getTime() * 1000L);
            DateTime dateTime2 = new DateTime(item2.getTime() * 1000L);

            return dateTime2.compareTo(dateTime1);
        }
    };

    public static final String TAG_ID = "id";
    public static final String TAG_AUTHOR_HANDLE = "by";
    public static final String TAG_TIME = "time";
    public static final String TAG_COMMENT = "text";
    public static final String TAG_COMMENT_IDS = "kids";
    public static final String TAG_TITLE = "title";

    private String id;          // The item's unique id.
    private boolean deleted;    // true if the item is deleted.
    private String type;        // The type of item. One of "job", "story", "comment", "poll", or "pollopt".
    private String by;      // The username of the item's author.
    private long time;          // Creation date of the item, in Unix Time.
    private String text;        // The comment, story or poll text. HTML.
    private boolean dead;       // true if the item is dead.
    private String parent;      // The item's parent. For comments, either another comment or the relevant story.
                                // For pollopts, the relevant poll.
    private String[] kids;        // The ids of the item's comments, in ranked display order.
    private String url;         // The URL of the story.
    private String score;       // The story's score, or the votes for a pollopt.
    private String title;       // The title of the story, poll or job.
    private String parts;       // A list of related pollopts, in display order.
    private int descendants;    // In the case of stories or polls, the total comment count.

    public Item() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String[] getKids() {
        return kids;
    }

    public void setKids(String[] kids) {
        this.kids = kids;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParts() {
        return parts;
    }

    public void setParts(String parts) {
        this.parts = parts;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    @Override
    public int compare(Item item1, Item item2) {
        DateTime dateTime1 = new DateTime(item1.getTime() * 1000L);
        DateTime dateTime2 = new DateTime(item2.getTime() * 1000L);

        return dateTime2.compareTo(dateTime1);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Item)) {
            return false;
        }
        Item item = (Item)o;
        if ((Integer.parseInt(id) == Integer.parseInt(item.getId()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {

        return id + ":" + CommonUtil.getTimePassed(time * 1000L);
    }

}
