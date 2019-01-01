

package com.example.android.gatho;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;


@Entity(tableName = "events")

public class Event {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @Nullable
    @ColumnInfo(name = "sid")
    private Integer sId;

    @ColumnInfo(name = "name")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "category")
    private String category;


    @ColumnInfo(name = "user_id")
    private Integer user_id;


    @ColumnInfo(name = "chat")
    private Boolean chat;

    @Nullable
    @ColumnInfo(name = "created_at")
    private String created_at;

    @ColumnInfo(name = "location")
    private LatLng location;

    public Event(Integer sId, String title,String category, String description, String date, Integer user_id, Boolean chat, String created_at, LatLng location) {
        this.sId = sId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.user_id = user_id;
        this.chat=chat;
        this.category=category;
        this.created_at = created_at;
        this.location = location;
    }
    public Event(String title, String description, String date, String created_at, LatLng location) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.created_at = created_at;
        this.location = location;
    }

    public Event() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer sid) {
        this.id = id;
    }

    public Integer getSId() {
        return this.sId;
    }

    public void setSId(Integer sid) {
        this.sId = sid;
    }

    public Boolean getChat() {
        return chat;
    }

    public void setChat(Boolean chat) {
        this.chat = chat;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
