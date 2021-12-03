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
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.Purchasing;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseCrashlytics.getInstance().log("displaying settings fragment");

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

            Preference satellitePref = (Preference) findPreference("satellite");
            if (!MainActivity.preferences.getBoolean("subscribe",false)) {
                if (satellitePref != null) {
                    satellitePref.setEnabled(false);
                }
            }

            Preference premiumPref = (Preference) findPreference("getpremium");
            if (MainActivity.preferences.getBoolean("subscribe",false)) {
                premiumPref.setVisible(false);
            } else {
                premiumPref.setOnPreferenceClickListener(preference -> {
                    new Purchasing(requireContext()).subscribe(requireContext(),requireActivity());
                    return false;
                });
            }
        }
    }
}
