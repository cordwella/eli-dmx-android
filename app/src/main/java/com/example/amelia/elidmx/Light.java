package com.example.amelia.elidmx;

/**
 * Created by amelia on 7/09/15.
 */
public class Light {
    private int id;
    public int value;
    private String name;
    private String category;
    private Boolean isEditable;
    public Light(String currName, int currId, int currValue, String currCategory, Boolean currIsEditable){
        this.name = currName;
        this.id = currId;
        this.category = currCategory;
        this.value = currValue;
        this.isEditable = currIsEditable;
    }

    public String getName() {
        return name;
    }
    public String getCategory(){
        return category;
    }
    public int getId(){
        return id;
    }
    public Boolean isEditable(){
        return isEditable;
    }
}
