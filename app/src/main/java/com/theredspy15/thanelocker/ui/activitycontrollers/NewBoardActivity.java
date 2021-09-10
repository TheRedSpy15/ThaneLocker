package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thanelocker.R;
import com.theredspy15.thanelocker.models.Board;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class NewBoardActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE = 1;

    EditText nameEditText;
    Spinner truckSpinner;
    Spinner wheelsSpinner;
    Spinner griptapeSpinner;
    Spinner bearingsSpinner;
    Spinner pivotSpinner;
    EditText rAngleEditText;
    EditText fAngleEditText;
    EditText descriptionEditText;
    EditText riserEditText;
    Spinner bdBushingsSpinner;
    Spinner rdBushingsSpinner;
    ImageView imageView;

    Uri imageUri;
    Bitmap imageBitmap;
    byte[] imageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_board);

        nameEditText = findViewById(R.id.editTextBoardName); // TODO: use view binding instead
        truckSpinner = findViewById(R.id.spinnerTrucks);
        rAngleEditText = findViewById(R.id.editTextRAngle);
        fAngleEditText = findViewById(R.id.editTextFAngle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        pivotSpinner = findViewById(R.id.spinnerPivot);
        wheelsSpinner = findViewById(R.id.spinnerWheels);
        griptapeSpinner = findViewById(R.id.spinnerGriptapes);
        bearingsSpinner = findViewById(R.id.spinnerBearings);
        riserEditText = findViewById(R.id.editTextRiser);
        imageView = findViewById(R.id.imageView);
        bdBushingsSpinner = findViewById(R.id.spinnerBdBushings);
        rdBushingsSpinner = findViewById(R.id.spinnerRdBushings);
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

    public void fromGallery(View view) {
        mGetContent.launch("image/*");
    }

    public void fromCamera(View view) {
        mGetCamera.launch(null);
    }

    public void create(View view) {
        Board board = new Board();
        board.setName(nameEditText.getText().toString());
        board.setImage(imageBytes);
        board.setDescription(descriptionEditText.getText().toString());
        board.setTrucks(truckSpinner.getSelectedItem().toString());
        board.setRearAngle(Byte.parseByte(rAngleEditText.getText().toString()));
        board.setFrontAngle(Byte.parseByte(fAngleEditText.getText().toString()));
        board.setRd_bushing(rdBushingsSpinner.getSelectedItem().toString());
        board.setBd_bushings(bdBushingsSpinner.getSelectedItem().toString());
        board.setWheels(wheelsSpinner.getSelectedItem().toString());
        board.setBearings(bearingsSpinner.getSelectedItem().toString());
        board.setPivot(pivotSpinner.getSelectedItem().toString());
        board.setRiserHt(Double.parseDouble(riserEditText.getText().toString()));
        board.setGripTp(griptapeSpinner.getSelectedItem().toString());

        Board.savedBoards.put(board.getId(), board);
        Board.savedBoardIds.add(board.getId());
        Board.save();

        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }
}