package com.riccardobusetti.colombo.data;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class HistoryData {

    private int id;
    private String title, link;

    public HistoryData(int id, String title, String link) {
        this.id = id;
        this.title = title;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
