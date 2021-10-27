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
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityEditProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.utils.PermissionChecker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

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
            Bitmap bitmap = BitmapFactory.decodeByteArray(Profile.localProfile.getImage(), 0, Profile.localProfile.getImage().length);
            Glide.with(this).load(bitmap).into(binding.profilePictureView);
        }

        binding.saveButton.setOnClickListener(this::save);
        binding.changePictureButton.setOnClickListener(this::changePicture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                Glide.with(this).load(selectedImage).into(binding.profilePictureView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                MotionToast.Companion.createColorToast(
                        this,
                        getString(R.string.went_wrong),
                        getString(R.string.failed_image),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, R.font.montserrat_regular)
                );
            }
        }else {
            MotionToast.Companion.createColorToast(
                    this,
                    getString(R.string.no_save_session),
                    getString(R.string.no_image_selected),
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.montserrat_regular)
            );
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Glide.with(this).load(uri).into(binding.profilePictureView);
                    imageUri = uri;

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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
        if (checkPermission()) mGetContent.launch("image/*");
    }

    private boolean checkPermission() {
        if (!PermissionChecker.checkPermissionGallery(this)) PermissionChecker.requestPermissionGallery(EditProfileActivity.this);

        return PermissionChecker.checkPermissionGallery(this);
    }

    public void save(View view) {
        Profile.localProfile.setImage(imageBytes);
        Profile.localProfile.setName(binding.editTextName.getText().toString());
        Profile.localProfile.setDescription(binding.editTextDescription.getText().toString());
        Profile.save();

        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }
}