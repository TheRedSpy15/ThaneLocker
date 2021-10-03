package com.theredspy15.thanelocker.models;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Image implements Serializable {

    private static final long serialVersionUID = 1234590L;

    private int location;
    private String data = "";

    public Image() {}

    public Image(String data, int boardId) {
        this.data = data;
        location = boardId;
    }

    public static ArrayList<Byte> convertBytesToList(byte[] bytes) {
        if (bytes != null) {
            final ArrayList<Byte> list = new ArrayList<>();
            for (byte b : bytes) {
                list.add(b);
            }
            return list;
        } else return null;
    }

    /**
     * When doing any saving of an object that uses Image, it should save it to the firebase storage, meant for files. NOT DATABASE!
     * Save the image first, then sent its data to nothing when saving the object containing the image. Leaving only the location to reload it later
     */
    public static void uploadImage(byte[] bytes) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        UploadTask uploadTask = storageRef.putBytes(bytes);
        uploadTask.addOnFailureListener((OnFailureListener) exception -> System.out.println("image upload failed"))
                .addOnSuccessListener(taskSnapshot -> System.out.println("imageuploaded"));
    }

    public static byte[] toByteArray(List<Byte> list) {
        if (list != null) {
            final int n = list.size();
            byte[] ret = new byte[n];
            for (int i = 0; i < n; i++) {
                ret[i] = list.get(i);
            }
            return ret;
        } else return null;
    }

    /**
     * Used to save image in memory preventing long loading, and offline issues. But we can still properly store it in firestore
     */
    public static String convertImageBytesToString(byte[] image) {
        Gson gson = new Gson();
        return gson.toJson(image);
    }

    public static byte[] convertImageStringToBytes(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<byte[]>() {}.getType());
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
