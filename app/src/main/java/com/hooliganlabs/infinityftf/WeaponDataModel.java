package com.hooliganlabs.infinityftf;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeaponDataModel {
    public enum WeaponType {WEAPON};

    public int id;
    public WeaponType type;
    public String name;
    public int ammunition;
    public String burst;
    public String damage;
    public String saving;
    public List<String> properties;
    public DistanceDataModel distance;

    public class DistanceDataModel {
        @SerializedName("short")
        public MaxModDataModel length1;
        @SerializedName("med")
        public MaxModDataModel length2;
        @SerializedName("long")
        public MaxModDataModel length3;
        @SerializedName("max")
        public MaxModDataModel length4;
    }

    public class MaxModDataModel {
        @SerializedName("max")
        public int length;
        public int mod;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;

        if(this.getClass() != o.getClass())
            return false;

        WeaponDataModel other = (WeaponDataModel)o;
        return (other.name.equals((this.name)));
    }
}
