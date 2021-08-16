package com.theredspy15.thanelocker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.Serializable;

public class BitmapDataObject implements Serializable {

    private Bitmap currentImage;

    public BitmapDataObject(Bitmap bitmap)
    {
        setCurrentImage(bitmap);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getCurrentImage().compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        out.writeInt(byteArray.length);
        out.write(byteArray);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        int bufferLength = in.readInt();

        byte[] byteArray = new byte[bufferLength];

        int pos = 0;
        do {
            int read = in.read(byteArray, pos, bufferLength - pos);

            if (read != -1) {
                pos += read;
            } else {
                break;
            }

        } while (pos < bufferLength);

        setCurrentImage(BitmapFactory.decodeByteArray(byteArray, 0, bufferLength));

    }

    public Bitmap getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(Bitmap currentImage) {
        this.currentImage = currentImage;
    }
}