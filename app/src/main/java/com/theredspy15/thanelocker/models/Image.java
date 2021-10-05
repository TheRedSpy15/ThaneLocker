package com.theredspy15.thanelocker.models;

import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.utils.App;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Image implements Serializable {

    private static final long serialVersionUID = 1234590L;

    private String location;
    private String data = "";

    public Image() {}

    public Image(String data, String boardId) {
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
    public void uploadImage(Context context) {
        byte[] bytes = convertImageStringToBytes(data);

        if (bytes != null && new App().isNetworkAvailable(context)) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(location);

            UploadTask uploadTask = storageRef.putBytes(bytes);
            uploadTask
                    .addOnFailureListener((OnFailureListener) exception -> System.out.println("image upload failed"))
                    .addOnSuccessListener(taskSnapshot -> System.out.println("imageuploaded"));
        }
    }

    public void downloadImage(Context context) {
        if (new App().isNetworkAvailable(context)) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(location);

            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        data = convertImageBytesToString(bytes);
                        System.out.println("downloads worked");
                    })
                    .addOnFailureListener(System.out::println);
        }
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
