package com.theredspy15.thanelocker.ui.activitycontrollers;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thanelocker.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.utils.PermissionChecker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;

public class NewBoardActivity extends AppCompatActivity {

    EditText nameEditText;
    EditText deckEditText;
    Spinner truckSpinner;
    Spinner wheelsSpinner;
    Spinner griptapeSpinner;
    Spinner bearingsSpinner;
    Spinner pivotSpinner;
    EditText rAngleEditText;
    EditText fAngleEditText;
    EditText descriptionEditText;
    Spinner bdBushingsSpinner;
    Spinner rdBushingsSpinner;
    ImageView imageView;
    SwitchMaterial advanceSwitch;
    TableLayout advanceTable;

    Uri imageUri;
    Bitmap imageBitmap;
    byte[] imageBytes;
    Board board;

    boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_board);

        System.out.println("xx115-"+Board.savedBoardIds.size());
        System.out.println("xx115-"+Board.savedBoards.size());

        nameEditText = findViewById(R.id.editTextBoardName); // TODO: use view binding instead
        deckEditText = findViewById(R.id.editTextDeck);
        truckSpinner = findViewById(R.id.spinnerTrucks);
        rAngleEditText = findViewById(R.id.editTextRAngle);
        fAngleEditText = findViewById(R.id.editTextFAngle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        pivotSpinner = findViewById(R.id.spinnerPivot);
        wheelsSpinner = findViewById(R.id.spinnerWheels);
        griptapeSpinner = findViewById(R.id.spinnerGriptapes);
        bearingsSpinner = findViewById(R.id.spinnerBearings);
        imageView = findViewById(R.id.imageView);
        bdBushingsSpinner = findViewById(R.id.spinnerBdBushings);
        rdBushingsSpinner = findViewById(R.id.spinnerRdBushings);
        advanceSwitch = findViewById(R.id.advanceSwitch);
        advanceTable = findViewById(R.id.advanceTable);

        advanceSwitch.setOnCheckedChangeListener(this::toggleAdvanceMode);

        board = (Board) getIntent().getSerializableExtra("board");

        if (board != null) {
            isEditing = true;
            loadForEdit();
            Button button = findViewById(R.id.buttonCreate);
            button.setText("Apply");
            button.setOnClickListener(this::loadBoardActivity);
        } else board = new Board();

        checkAdvanceMode(board.isAdvanceMode());
    }

    private void toggleAdvanceMode(CompoundButton compoundButton, boolean isChecked) {
        checkAdvanceMode(isChecked);
    }

    private void checkAdvanceMode(boolean isChecked) {
        if (isChecked) {
            advanceSwitch.setChecked(true);
            advanceTable.setVisibility(View.VISIBLE);
            board.setAdvanceMode(true);
        } else {
            advanceSwitch.setChecked(false);
            advanceTable.setVisibility(View.GONE);
            board.setAdvanceMode(false);
        }
    }

    private void loadForEdit() {
        if (board.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
            imageView.setImageBitmap(bitmap);
        }

        String[] pivotsA = getResources().getStringArray(R.array.pivots);
        LinkedList<String> pivots = new LinkedList<>();
        Collections.addAll(pivots, pivotsA);

        String[] bushingsA = getResources().getStringArray(R.array.bushings);
        LinkedList<String> bushings = new LinkedList<>();
        Collections.addAll(bushings, bushingsA);

        String[] wheelsA = getResources().getStringArray(R.array.wheels);
        LinkedList<String> wheels = new LinkedList<>();
        Collections.addAll(wheels, wheelsA);

        String[] trucksA = getResources().getStringArray(R.array.trucks);
        LinkedList<String> trucks = new LinkedList<>();
        Collections.addAll(trucks, trucksA);

        String[] bearingsA = getResources().getStringArray(R.array.bearings);
        LinkedList<String> bearings = new LinkedList<>();
        Collections.addAll(bearings, bearingsA);

        String[] griptapeA = getResources().getStringArray(R.array.grip_tapes);
        LinkedList<String> griptape = new LinkedList<>();
        Collections.addAll(griptape, griptapeA);

        nameEditText.setText(board.getName());
        deckEditText.setText(board.getDeck());
        truckSpinner.setSelection(trucks.indexOf(board.getTrucks()));
        rAngleEditText.setText(""+board.getRearAngle());
        fAngleEditText.setText(""+board.getFrontAngle());
        descriptionEditText.setText(board.getDescription());
        pivotSpinner.setSelection(pivots.indexOf(board.getPivot()));
        wheelsSpinner.setSelection(wheels.indexOf(board.getWheels()));
        griptapeSpinner.setSelection(griptape.indexOf(board.getGripTp()));
        bearingsSpinner.setSelection(bearings.indexOf(board.getBearings()));
        bdBushingsSpinner.setSelection(bushings.indexOf(board.getBd_bushings()));
        rdBushingsSpinner.setSelection(bushings.indexOf(board.getRd_bushing()));
    }

    // used only for when finished editing an existing board
    public void loadBoardActivity(View view) {
        Intent myIntent = new Intent(this, BoardActivity.class);
        myIntent.putExtra("board", board);
        startActivity(myIntent);
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
                    imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            } else {
                imageView.setImageBitmap(imageBitmap);
            }
        }else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                imageView.setImageURI(uri);
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
            });

    ActivityResultLauncher<Void> mGetCamera = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                imageBitmap = bitmap;
                ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    imageBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 30, baoStream);
                } else {
                    imageBitmap.compress(Bitmap.CompressFormat.WEBP, 30, baoStream);
                }
                imageBytes = baoStream.toByteArray();
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
        board.setName(nameEditText.getText().toString());
        board.setDeck(deckEditText.getText().toString());
        board.setImage(imageBytes);
        if (!TextUtils.isEmpty(descriptionEditText.getText().toString()))board.setDescription(descriptionEditText.getText().toString());
        board.setTrucks(truckSpinner.getSelectedItem().toString());
        if (!TextUtils.isEmpty(rAngleEditText.getText().toString()))board.setRearAngle(Byte.parseByte(rAngleEditText.getText().toString()));
        if (!TextUtils.isEmpty(fAngleEditText.getText().toString()))board.setFrontAngle(Byte.parseByte(fAngleEditText.getText().toString()));
        board.setRd_bushing(rdBushingsSpinner.getSelectedItem().toString());
        board.setBd_bushings(bdBushingsSpinner.getSelectedItem().toString());
        board.setWheels(wheelsSpinner.getSelectedItem().toString());
        board.setBearings(bearingsSpinner.getSelectedItem().toString());
        board.setPivot(pivotSpinner.getSelectedItem().toString());
        board.setGripTp(griptapeSpinner.getSelectedItem().toString());

        Intent myIntent;
        if (isEditing) {
            myIntent = new Intent(this, BoardActivity.class);
            myIntent.putExtra("board",board);

            Board.savedBoards.put(board.getId(),board);
            Board.save();
        } else {
            Board.savedBoards.put(board.getId(), board);
            Board.savedBoardIds.add(board.getId());
            Board.save();

            myIntent = new Intent(this, BoardActivity.class);
            myIntent.putExtra("board",board);
        }
        startActivity(myIntent);
    }
}