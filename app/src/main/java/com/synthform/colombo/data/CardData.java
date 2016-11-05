package com.synthform.colombo.data;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class CardData {

    private int id;
    private String name, code, hex;

    public CardData(int id, String name, String code, String hex) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.hex = hex;
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

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
