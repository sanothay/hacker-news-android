package com.dudev.android.hackernews.Event;

/**
 * Created by soulivanh on 5/5/15 AD.
 */
public class OnTopStoryIdsUpdateEvent {
    private int[] ids;
    private boolean hasError;

    public OnTopStoryIdsUpdateEvent(int[] ids) {
        this.ids = ids;
    }

    public int[] getIds() {
        return ids;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
