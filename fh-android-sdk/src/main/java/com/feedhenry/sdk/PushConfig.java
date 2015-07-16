package com.feedhenry.sdk;

import java.util.ArrayList;
import java.util.List;

public class PushConfig {

    private String alias;
    private List<String> categories = new ArrayList<String>();

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
