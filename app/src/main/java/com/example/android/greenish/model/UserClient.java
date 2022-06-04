package com.example.android.greenish.model;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.greenish.MarkerInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserClient extends Application {

   private User user;
   private MarkerInfo markerInfo;

   @Override
   public void onCreate() {
      super.onCreate();

   }

   public User getUser() {
      if (user == null) {
         fetchUserData();
      }
      return user;
   }

   public void setUser(User user) {
      this.user = user;
   }

   public void fetchUserData() {
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null)
         {
             DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
             reference.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                     user = snapshot.child("users").child(currentUser.getUid())
                             .getValue(User.class);

                     if (user != null) {

                        user = new User(
                                 user.firstName, user.plant, user.watering
                         );


                     } else {
                         return;
                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });
         }
   }

}