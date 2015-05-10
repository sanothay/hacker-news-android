package com.dudev.android.hackernews.model;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public enum ItemType {

    JOB ("job"),
    STORY ("story"),
    COMMENT ("comment"),
    POLL ("poll"),
    POLL_OPT ("poll_opt"),
    OTHER ("other");

    private String type;
    ItemType(String comment) {
        type = comment;
    }

    public String getType() {
        return type;
    }
}
