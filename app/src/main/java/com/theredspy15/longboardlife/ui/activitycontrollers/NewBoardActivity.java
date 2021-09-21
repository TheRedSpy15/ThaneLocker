package com.theredspy15.longboardlife.ui.activitycontrollers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityNewBoardBinding;
import com.theredspy15.longboardlife.models.Board;
import com.theredspy15.longboardlife.utils.PermissionChecker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class NewBoardActivity extends AppCompatActivity {

    Uri imageUri;
    Bitmap imageBitmap;
    byte[] imageBytes;
    Board board;

    ActivityNewBoardBinding binding;

    boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_board);

        binding = ActivityNewBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.advanceSwitch.setOnCheckedChangeListener(this::toggleAdvanceMode);
        binding.buttonCreate.setOnClickListener(this::create);

        board = (Board) getIntent().getSerializableExtra("board");

        if (board != null) {
            isEditing = true;
            loadForEdit();
            Button button = findViewById(R.id.buttonCreate);
            button.setText(R.string.apply);
        } else board = new Board();

        checkAdvanceMode(board.isAdvanceMode());
    }

    private void toggleAdvanceMode(CompoundButton compoundButton, boolean isChecked) {
        checkAdvanceMode(isChecked);
    }

    private void checkAdvanceMode(boolean isChecked) {
        if (isChecked) {
            binding.advanceSwitch.setChecked(true);
            binding.advanceTable.setVisibility(View.VISIBLE);
            board.setAdvanceMode(true);
        } else {
            binding.advanceSwitch.setChecked(false);
            binding.advanceTable.setVisibility(View.GONE);
            board.setAdvanceMode(false);
        }
    }

    private void loadForEdit() {
        if (board.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
            binding.imageView.setImageBitmap(bitmap);
        }

        String[] pivotsA = getResources().getStringArray(R.array.pivots);
        ArrayList<String> pivots = new ArrayList<>();
        Collections.addAll(pivots, pivotsA);

        String[] bushingsA = getResources().getStringArray(R.array.bushings);
        ArrayList<String> bushings = new ArrayList<>();
        Collections.addAll(bushings, bushingsA);

        String[] wheelsA = getResources().getStringArray(R.array.wheels);
        ArrayList<String> wheels = new ArrayList<>();
        Collections.addAll(wheels, wheelsA);

        String[] trucksA = getResources().getStringArray(R.array.trucks);
        ArrayList<String> trucks = new ArrayList<>();
        Collections.addAll(trucks, trucksA);

        String[] bearingsA = getResources().getStringArray(R.array.bearings);
        ArrayList<String> bearings = new ArrayList<>();
        Collections.addAll(bearings, bearingsA);

        String[] griptapeA = getResources().getStringArray(R.array.grip_tapes);
        ArrayList<String> griptape = new ArrayList<>();
        Collections.addAll(griptape, griptapeA);

        binding.editTextBoardName.setText(board.getName());
        binding.editTextDeck.setText(board.getDeck());
        binding.spinnerTrucks.setSelection(trucks.indexOf(board.getTrucks()));
        binding.editTextRAngle.setText(""+board.getRearAngle());
        binding.editTextFAngle.setText(""+board.getFrontAngle());
        binding.editTextDescription.setText(board.getDescription());
        binding.spinnerPivot.setSelection(pivots.indexOf(board.getPivot()));
        binding.spinnerWheels.setSelection(wheels.indexOf(board.getWheels()));
        binding.spinnerGriptapes.setSelection(griptape.indexOf(board.getGripTp()));
        binding.spinnerBearings.setSelection(bearings.indexOf(board.getBearings()));
        binding.spinnerBdBushings.setSelection(bushings.indexOf(board.getBd_bushings()));
        binding.spinnerRdBushings.setSelection(bushings.indexOf(board.getRd_bushing()));

        imageBytes = board.getImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (imageBitmap != null) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    binding.imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.went_wrong), Toast.LENGTH_LONG).show();
                }
            } else {
                binding.imageView.setImageBitmap(imageBitmap);
            }
        } // else: no image selected
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.imageView.setImageURI(uri);
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

    ActivityResultLauncher<Void> mGetCamera = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    imageBitmap = bitmap;
                    ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        imageBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 30, baoStream);
                    } else {
                        imageBitmap.compress(Bitmap.CompressFormat.WEBP, 30, baoStream);
                    }
                    imageBytes = baoStream.toByteArray();
                }
            });

    private boolean checkPermissionGallery() {
        if (!PermissionChecker.checkPermissionGallery(this)) PermissionChecker.requestPermissionGallery(this,NewBoardActivity.this);

        return PermissionChecker.checkPermissionGallery(this);
    }

    private boolean checkPermissionCamera() {
        if (!PermissionChecker.checkPermissionCamera(this)) PermissionChecker.requestPermissionCamera(this,NewBoardActivity.this);

        return PermissionChecker.checkPermissionCamera(this);
    }

    public void fromGallery(View view) {
        if (checkPermissionGallery()) mGetContent.launch("image/*");
    }

    public void fromCamera(View view) {
        if (checkPermissionCamera()) mGetCamera.launch(null);
    }

    public void create(View view) {
        board.setName(binding.editTextBoardName.getText().toString());
        if (!TextUtils.isEmpty(binding.editTextDeck.getText().toString()))board.setDeck(binding.editTextDeck.getText().toString());
        board.setImage(imageBytes);
        if (!TextUtils.isEmpty(binding.editTextDescription.getText().toString()))board.setDescription(binding.editTextDescription.getText().toString());
        board.setTrucks(binding.spinnerTrucks.getSelectedItem().toString());
        if (!TextUtils.isEmpty(binding.editTextRAngle.getText().toString()))board.setRearAngle(Byte.parseByte(binding.editTextRAngle.getText().toString()));
        if (!TextUtils.isEmpty(binding.editTextFAngle.getText().toString()))board.setFrontAngle(Byte.parseByte(binding.editTextFAngle.getText().toString()));
        board.setRd_bushing(binding.spinnerRdBushings.getSelectedItem().toString());
        board.setBd_bushings(binding.spinnerBdBushings.getSelectedItem().toString());
        board.setWheels(binding.spinnerWheels.getSelectedItem().toString());
        board.setBearings(binding.spinnerBearings.getSelectedItem().toString());
        board.setPivot(binding.spinnerPivot.getSelectedItem().toString());
        board.setGripTp(binding.spinnerGriptapes.getSelectedItem().toString());

        Intent myIntent;
        if (isEditing) {
            Board.savedBoards.put(board.getId(),board);
            Board.save();

            myIntent = new Intent(this, BoardActivity.class);
            myIntent.putExtra("board_id",board.getId());
        } else {
            Board.savedBoards.put(board.getId(), board);
            Board.savedBoardIds.add(board.getId());
            Board.save();

            myIntent = new Intent(this, BoardActivity.class);
            myIntent.putExtra("board_id",board.getId());
        }
        startActivity(myIntent);
    }
}