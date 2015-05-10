package com.dudev.android.hackernews.Event;

import com.dudev.android.hackernews.model.Item;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public class OnItemUpdateEvent {

    private Item item;
    private boolean hasError;

    public OnItemUpdateEvent (Item item) {
        this.item = item;
    }

    public Item getItem () {
        return item;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
