package com.example.android.greenish.dialog;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.greenish.MarkerInfo;
import com.example.android.greenish.R;
import com.example.android.greenish.model.User;
import com.example.android.greenish.model.UserClient;
import com.example.android.greenish.util.DateUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MarkerInfoDialog extends DialogFragment {


    private static String key;
    private static String age;
    private static String lastWatering;
    private static String needForWater;
    private static String plantedBy;
    private static boolean isUserIn = false;
    private MarkerInfo markerInfo;
    private User userPlant;

    private MarkerInfoDialog() {

    }

    private MarkerInfoDialog(MarkerInfo markerInfo) {
        this.markerInfo = markerInfo;
        userPlant = markerInfo.getUser();
    }

    public static MarkerInfoDialog newInstance(String key, String age, String lastWatering, String needForWater, String plantedBy,
                                               boolean isUserIn) {
        MarkerInfoDialog.key = key;
        MarkerInfoDialog.age = age;
        MarkerInfoDialog.lastWatering = lastWatering;
        MarkerInfoDialog.needForWater = needForWater;
        MarkerInfoDialog.plantedBy = plantedBy;
        MarkerInfoDialog.isUserIn = isUserIn;

        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.setKey(key);
        markerInfo.plantDate = age;
        User user = new User();
        user.firstName = plantedBy;
        markerInfo.setUser(user);

        return new MarkerInfoDialog(markerInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.marker_info_dialog, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    User user;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setDialogProperties();


        user = ((UserClient) requireActivity().getApplication()).getUser();
        ImageView imageView = view.findViewById(R.id.imageViewMarkerInfoDialog);


        TextView ageTxtView = view.findViewById(R.id.ageTextViewMarkerInfoDialog);
        String ageAsString = "Age: " + age;
        ageTxtView.setText(ageAsString);

        TextView lastWateringTextView = view.findViewById(R.id.lastWateringTextViewMarkerInfoDialog);
        String lastWateringAsString = "Last watering: " + lastWatering;

        lastWateringTextView.setText(lastWateringAsString);


        TextView needForWaterTextView = view.findViewById(R.id.needForWaterTextViewMarkerInfoDialog);
        String waterNeed = "Need for water: " + needForWater;
        needForWaterTextView.setText(waterNeed);

        TextView plantedByTextView = view.findViewById(R.id.plantedByTextViewMarkerInfoDialog);
        String planter = "Planted by: " + plantedBy;
        plantedByTextView.setText(planter);

        ImageButton actionButton = view.findViewById(R.id.wateringATreeFloatingActionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inform - send back - to map activity/fragment ...
                if (isUserIn) {
                    postTreeData(markerInfo);
                    Toast.makeText(getActivity(), "Watering a tree  !!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "u 're out of range", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_bottom);
        anim.setDuration(500);
        view.setAnimation(anim);
        super.onViewCreated(view, savedInstanceState);
    }


    DatabaseReference reference  = FirebaseDatabase.getInstance().getReference();



    private void postTreeData(MarkerInfo markerInfo) {
        markerInfo.lastWatering = DateUtils.dateHelper(DateUtils.DATE_USE_SLASH);
        markerInfo.setUser(user);

        user.watering = user.watering+1;
        String uid = FirebaseAuth.getInstance().getUid();
        reference.child("users").child(uid)
                .child("watering").setValue(user.watering);

        reference.child("MarkerInfo").child(markerInfo.getKey())
                .child("lastWatering").setValue(markerInfo.lastWatering);

        Toast.makeText(getContext(), "Key: " + markerInfo.getKey(), Toast.LENGTH_SHORT).show();
    }

    private void setDialogProperties() {
        if (getDialog() != null)
        {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // background
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, // width
                    WindowManager.LayoutParams.WRAP_CONTENT); // height

            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM; // Align dialog to the BOTTOM of screen;
            window.setAttributes(params);

        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
