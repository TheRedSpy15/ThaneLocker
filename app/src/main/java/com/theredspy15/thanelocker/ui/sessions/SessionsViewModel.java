package com.theredspy15.thanelocker.ui.sessions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SessionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SessionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Coming Soon!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}