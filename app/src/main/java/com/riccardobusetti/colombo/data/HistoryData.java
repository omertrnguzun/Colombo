package com.riccardobusetti.colombo.data;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class HistoryData {

    private int identifier;
    private String title, link;

    public HistoryData(int identifier, String title, String link) {
        this.identifier = identifier;
        this.title = title;
        this.link = link;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setId(int id) {
        this.identifier = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
