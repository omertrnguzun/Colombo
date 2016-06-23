package com.riccardobusetti.colombo.data;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class CardData {

    private int id;
    private String name,code;
    private int image;

    public CardData(int id, String name, String code, int image) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
