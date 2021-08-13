package Com.mariapublishers.mariaexecutive;

import ir.mirrajabi.searchdialog.core.Searchable;

public class SearchModel implements Searchable {
    private String title;

    public SearchModel(String mTitle) {
        this.title = mTitle;
    }

    public void setTitle(String mTitle) {
        this.title = mTitle;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
