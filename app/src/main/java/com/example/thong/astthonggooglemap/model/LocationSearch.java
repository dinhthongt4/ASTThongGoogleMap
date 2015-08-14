
package com.example.thong.astthonggooglemap.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by thong on 05/08/2015.
 */

public class LocationSearch {

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private Location location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
