package com.example.pointnews.model;

import com.google.gson.annotations.SerializedName;

public class Source {
    @SerializedName("id")
    private String id; // ID unik sumber (bisa null)

    @SerializedName("name")
    private String name; // Nama sumber (misal: CNN, BBC News)

    // Constructor
    public Source(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters (opsional)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}