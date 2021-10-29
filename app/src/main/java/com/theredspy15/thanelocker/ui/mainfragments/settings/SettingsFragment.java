package com.theredspy15.thanelocker.ui.mainfragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentSettingsBinding;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getChildFragmentManager().beginTransaction().replace(R.id.settingsLayout, new MyPreferenceFragment()).commit();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
        }

        /**
         * Inflate Preferences
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);

            Preference myPref = (Preference) findPreference("satellite");
            if (!MainActivity.preferences.getBoolean("subscribe",false)) {
                if (myPref != null) {
                    myPref.setEnabled(false);
                }
            }
        }
    }
}
