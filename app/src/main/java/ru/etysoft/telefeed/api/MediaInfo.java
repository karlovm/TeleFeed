package ru.etysoft.telefeed.api;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MediaInfo {

    @PrimaryKey
    private int id;
    private int height, width;
    private int type;

    public static int TYPE_VIDEO = 0;
    public static int TYPE_IMAGE= 1;

    public MediaInfo(int id, int height, int width, int type) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
