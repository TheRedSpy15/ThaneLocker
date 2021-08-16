package com.theredspy15.thanelocker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thanelocker.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class NewBoardActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_board);

        nameEditText = findViewById(R.id.editTextBoardName);
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
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                imageView.setImageURI(uri);
                imageUri = uri;
            });

    public void fromGallery(View view) {
        mGetContent.launch("image/*");
    }

    public void fromCamera(View view) {

    }

    public void create(View view) {
        Board board = new Board();
        board.setName(nameEditText.getText().toString());
        //board.setTags();
        board.setImage(uriToBitmap(imageUri));
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

        MainActivity.savedBoards.add(board);
        MainActivity.saveData(this);
    }

    private BitmapDataObject uriToBitmap(Uri selectedFileUri) {
        final Bitmap[] image = {null};
        final BitmapDataObject[] bitmapDataObject = {null};
        Thread thread = new Thread(() -> {
            ParcelFileDescriptor parcelFileDescriptor =
                    null;
            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedFileUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert parcelFileDescriptor != null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image[0] = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmapDataObject[0] = new BitmapDataObject(image[0]);
        });thread.start();
        return bitmapDataObject[0];
    }
}