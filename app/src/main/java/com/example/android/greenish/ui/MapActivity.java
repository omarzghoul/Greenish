package com.example.android.greenish.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.android.greenish.R;
import com.example.android.greenish.model.User;
import com.example.android.greenish.model.UserClient;
import com.example.android.greenish.util.GpsUtils;
import com.example.android.greenish.fragment.*;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // constants
    private static final String TAG = "log_trace";
    static final String[] PERMISSIONS = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
    static final int REQ_CODE = 12321;

    // vars
    private boolean isLocationPermissionsGranted = false;

    // Widgets - mirror objects -
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // customize color
        doCustomColoring();

        // init view.
        init();

        prepareNavView();

        if (checkSelfPermissions()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new MapFragment())
                    .commit();

        } else {

            //////////////
            GpsUtils.showLocationPrompt(MapActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == GpsUtils.REQUEST_CHECK_SETTING)
        {
            openFragment(new MapFragment());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init()
    {
        ImageButton menuNavBtn = findViewById(R.id.menuNavigatorButton);
        menuNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer = findViewById(R.id.drawerLayout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

    }



    private void prepareNavView()
    {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        User user = ((UserClient) getApplicationContext()).getUser();
        if (user != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.userNameNavHeaderTextView);
            String userName = user.firstName == null? user.email : user.firstName;
            navUsername.setText(userName);

            ImageView imgView = (ImageView) headerView.findViewById(R.id.userPhotoNavHeaderImageView);
            imgView.setImageResource(R.drawable.default_tree);
        } else {
            Toast.makeText(this, "WELCOME", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.nav_home:
                item.setChecked(true);
                openFragment(new MapFragment());

                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_profile:
                item.setChecked(true);
                openFragment(new ProfileFragment());

                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_settings:
                item.setChecked(true);
                openFragment(new SettingsFragment());
                Log.d(TAG, "onNavigationItemSelected: nav_settings");
                drawer.closeDrawer(GravityCompat.START);
                break;


            case R.id.nav_contact_us:

                break;
            case R.id.nav_help:

                break;
            case R.id.nav_logout:
                showDialog();
                break;
        }
        return true;
    }

    private <T extends Fragment> void openFragment(T obj)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, obj)
                .commit();
    }

    private void showDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are u sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent logoutIntent = new Intent(MapActivity.this, LoginActivity.class);
                        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        startActivity(logoutIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Snackbar.make(drawer, "//  Action for 'Cancel' Button.", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }).create()
                .show();
    }


    private boolean checkSelfPermissions () {
        isLocationPermissionsGranted = false;

        // Unnecessary to check Build.VERSION.SDK_INT; SDK_INT is always >= 28
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {

            isLocationPermissionsGranted = true;
        } else {
            requestPermissions(PERMISSIONS, REQ_CODE);
        }

        return isLocationPermissionsGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isLocationPermissionsGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    protected void doCustomColoring () {
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.nav_bar_color, null));
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}