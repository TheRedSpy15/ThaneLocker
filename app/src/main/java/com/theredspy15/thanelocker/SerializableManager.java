package com.theredspy15.thanelocker;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class SerializableManager {

    /**
     * Saves a serializable object.
     *
     * @param context The application context.
     * @param objectToSave The object to save.
     * @param fileName The name of the file.
     * @param <T> The type of the object.
     */

    public static LinkedList<Board> savedBoards = new LinkedList<>();
    public static LinkedList<Session> savedSessions = new LinkedList<>();

    public static <T extends Serializable> void saveSerializable(Context context, T objectToSave, String fileName) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(objectToSave);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a serializable object.
     *
     * @param context The application context.
     * @param fileName The filename.
     * @param <T> The object type.
     *
     * @return the serializable object.
     */

    public static<T extends Serializable> T readSerializable(Context context, String fileName) {
        T objectToReturn = null;

        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            objectToReturn = (T) objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return objectToReturn;
    }

    /**
     * Removes a specified file.
     *
     * @param context The application context.
     * @param filename The name of the file.
     */

    public static void removeSerializable(Context context, String filename) {
        context.deleteFile(filename);
    }

    public static void loadSavedData(Context context) {
        savedBoards = SerializableManager.readSerializable(context,"savedBoards.thanel");
        savedSessions = SerializableManager.readSerializable(context,"savedSessions.thanel");

        if (new File("savedBoards.thanel").exists())
            savedBoards = SerializableManager.readSerializable(context,"savedBoards.thanel");
        if (new File("savedSessions.thanel").exists())
            savedBoards = SerializableManager.readSerializable(context,"savedSessions.thanel");
    }

    public static void saveData(Context context) {
        SerializableManager.saveSerializable(context,savedBoards,"savedBoards");
        SerializableManager.saveSerializable(context,savedSessions,"savedSessions");
    }

}
