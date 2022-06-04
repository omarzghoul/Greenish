package com.example.android.greenish.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.greenish.fragment.callbacks.BottomSheetListener;
import com.example.android.greenish.R;
import com.example.android.greenish.model.User;
import com.example.android.greenish.model.UserClient;
import com.example.android.greenish.dialog.BottomSheetDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

   // constants
   private static final int RESULT_OK = -1;
   private static final String TAG = "log_trace";


   // vars
   Context context;
   ActivityResultLauncher<String> chooseImageLauncher;
   ActivityResultLauncher<Intent> takePhotoLauncher;
   private BottomSheetListener listener = new BottomSheetListener() {
      @Override
      public void onItemClicked(int userAction) {
         switch (userAction)
         {
            case CHOOSE_FROM_GALLERY:
               chooseImageLauncher.launch("image/*"); // MIME_TYPE
               break;

            case TAKE_A_PHOTO:
               if (isCameraPresentInPhone())
               {
                  Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                  if (intent.resolveActivity(context.getPackageManager()) != null) {
                     takePhotoLauncher.launch(intent);
                  }
               }
               break;

            case CANCEL:
            default:
               Snackbar.make(ivProfile, "NO_ACTION_PERFORMED", Snackbar.LENGTH_SHORT)
                       .setTextColor(Color.parseColor("#FFDADDA2"))
                       .show();
         }
      }
   };

   // widgets
   ImageView ivProfile;

   public ProfileFragment() {

   }

   /**
    * TODO: - error - Fragment is attempting to registerForActivityResult after being created.
    *  Fragments must call registerForActivityResult() before they are created (i.e. initialization, onAttach(), or onCreate()).
    */
   @Override
   public void onAttach(@NonNull Context context) {
      this.context = context;
      // step #1, register for activity result.
      registerForContracts();
      super.onAttach(context);
   }



   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_profile, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      User user = ((UserClient) context.getApplicationContext()).getUser();
      if (user == null) {
         Toast.makeText(context, "user is null", Toast.LENGTH_SHORT).show();

         super.onViewCreated(view, savedInstanceState);
         return;
      }

      TextView usernameTxt = view.findViewById(R.id.userNameTextView);
      String userName = user.firstName;
      String userNameStr = "User Name :" + " " + userName;
      usernameTxt.setText(userNameStr);


      ivProfile = view.findViewById(R.id.userPhotoImageView);
      ivProfile.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // step #2, show dialog.
            BottomSheetDialog dialog = BottomSheetDialog.newInstance(listener);
            dialog.show(getParentFragmentManager(), null);
         }
      });

      TextView wateringFieldTxt = view.findViewById(R.id.wateringPlantsTextView);
      String wateringCountStr = "Watering: " + user.watering;
      wateringFieldTxt.setText(wateringCountStr);

      TextView plantsFieldTxt = view.findViewById(R.id.pottedPlantsTextView);
      String plantsCountStr = "Plant: " + user.plant;
      plantsFieldTxt.setText(plantsCountStr);

      super.onViewCreated(view, savedInstanceState);
   }



   private boolean isCameraPresentInPhone()
   {
      return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
   }

   private void registerForContracts()
   {
      takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
              new ActivityResultCallback<ActivityResult>() {
                 @Override
                 public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    {
                       Bundle bundle = result.getData().getExtras();
                       Bitmap bitmap = (Bitmap) bundle.get("data");
                       ivProfile.setImageBitmap(bitmap);
                    }
                 }
              });

      chooseImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
              new ActivityResultCallback<Uri>() {
                 @Override
                 public void onActivityResult(Uri result) {
                    if (result != null)
                    {
                       ivProfile.setImageURI(result);
                    }
                 }
              });
   }


   @Override
   public void onDestroy() {
      if(chooseImageLauncher != null) { // releasing the underlying result callback
         chooseImageLauncher.unregister();
         chooseImageLauncher = null;
      }
      if(takePhotoLauncher != null) { // releasing the underlying result callback
         takePhotoLauncher.unregister();
         takePhotoLauncher = null;
      }
      super.onDestroy();
   }


}
