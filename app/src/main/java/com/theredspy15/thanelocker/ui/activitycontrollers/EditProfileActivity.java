package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityEditProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.theredspy15.thanelocker.models.Image;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.utils.PermissionChecker;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

  private ActivityEditProfileBinding binding;

  Uri imageUri;
  byte[] imageBytes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.editTextName.setText(Profile.localProfile.getName());
    binding.editTextDescription.setText(Profile.localProfile.getDescription());

    if (Profile.localProfile.getImage() != null) {
      Bitmap bitmap = BitmapFactory.decodeByteArray(
          Image.convertImageStringToBytes(
              Profile.localProfile.getImage().getData()),
          0,
          Image
              .convertImageStringToBytes(
                  Profile.localProfile.getImage().getData())
              .length);
      binding.profilePictureView.setImageBitmap(bitmap);
    }

    binding.saveButton.setOnClickListener(this::save);
    binding.changePictureButton.setOnClickListener(this::changePicture);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      try {
        final Uri imageUri = data.getData();
        final InputStream imageStream =
            getContentResolver().openInputStream(imageUri);
        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        binding.profilePictureView.setImageBitmap(selectedImage);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        Toast.makeText(this, getString(R.string.went_wrong), Toast.LENGTH_LONG)
            .show();
      }
    } else {
      Snackbar
          .make(binding.changePictureButton, R.string.no_image_selected,
                Snackbar.LENGTH_LONG)
          .setAction("Action", null)
          .show();
    }
  }

  ActivityResultLauncher<String> mGetContent = registerForActivityResult(
      new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
          binding.profilePictureView.setImageURI(uri);
          imageUri = uri;

          try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                this.getContentResolver(), imageUri);
            ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
              bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 30, baoStream);
            } else {
              bitmap.compress(Bitmap.CompressFormat.WEBP, 30, baoStream);
            }
            imageBytes = baoStream.toByteArray();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });

  public void changePicture(View view) {
    if (checkPermission())
      mGetContent.launch("image/*");
  }

  private boolean checkPermission() {
    if (!PermissionChecker.checkPermissionGallery(this))
      PermissionChecker.requestPermissionGallery(EditProfileActivity.this);

    return PermissionChecker.checkPermissionGallery(this);
  }

  public void save(View view) {
    Profile.localProfile.setImage(
        new Image(Image.convertImageBytesToString(imageBytes),
                  Profile.localProfile.getId()));
    Profile.localProfile.setName(binding.editTextName.getText().toString());
    Profile.localProfile.setDescription(
        binding.editTextDescription.getText().toString());
    Profile.save();

    Intent myIntent = new Intent(this, MainActivity.class);
    startActivity(myIntent);
  }
}
