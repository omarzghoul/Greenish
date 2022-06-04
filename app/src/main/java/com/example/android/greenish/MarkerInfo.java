package com.example.android.greenish;

import com.example.android.greenish.model.User;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class MarkerInfo implements Serializable {

   private static final long serialVersionUID = 1L;
   // java.io.NotSerializableException: com.google.android.gms.maps.model.LatLng
   public static final String HIGH_LEVEL = "HIGH";
   public static final String NORMAL_LEVEL = "NORMAL";
   public static final String LOW_LEVEL = "LOW";
   private transient LatLng latLng;
   private String title;
   private String snippet;
   private double latitude;
   private double longitude;

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   private String key;
   private User user;
   public String lastWatering;
   public String plantDate;


   private  int icon;

   public MarkerInfo() { }

   public MarkerInfo(LatLng latLng, String title, String snippet, int icon) {
      this.latLng = latLng;
      this.title = title;
      this.snippet = snippet;
      this.icon = icon;
      if (this.latLng != null) {
         this.latitude = latLng.latitude;
         this.longitude = latLng.longitude;
      }
   }

   private LatLng getLatLng() {
      return latLng;
   }

   public double getLatitude () {
      return latitude;
   }

   public double getLongitude () {
      return longitude;
   }

   public void setLatLng(LatLng latLng) {
      this.latLng = latLng;
      if (this.latLng != null) {
         this.latitude = latLng.latitude;
         this.longitude = latLng.longitude;
      }
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getSnippet() {
      return snippet;
   }

   public void setSnippet(String snippet) {
      this.snippet = snippet;
   }


   public int getIcon() {
      return icon;
   }

   public void setIcon(int icon) {
      this.icon = icon;
   }

   public void setUser(User user) {
      this.user = user;
   }

   public User getUser() {
      return user;
   }

   @Override
   public String toString() {
      return "MarkerInfo{" +
              "latLng=" + latLng +
              ", title='" + title + '\'' +
              ", snippet='" + snippet + '\'' +
              ", latitude=" + latitude +
              ", longitude=" + longitude +
              ", icon=" + icon +
              '}';
   }
}
