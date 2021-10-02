package com.theredspy15.thanelocker.ui.mainfragments.friendslist;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentFriendslistBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;

import java.util.ArrayList;

public class FriendsListFragment extends Fragment {

    FragmentFriendslistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFriendslistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadProfiles();

        return root;
    }

    public void loadFriends(ArrayList<Profile> profiles) {
        binding.friendsLayout.removeAllViews();
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        if (!profiles.isEmpty()) {
            for (Profile profile : profiles) {
                Button button = new Button(getContext());
                button.setText(profile.getName());
                button.setTextSize(18);
                button.setAllCaps(false);
                button.setOnClickListener(v -> {
                    Intent myIntent = new Intent(requireContext(), BoardActivity.class);
                    myIntent.putExtra("friend", profile);
                    startActivity(myIntent);
                });
                button.setPadding(50,50,50,50);
                layout.setMargins(0,20,0,20);
                button.setBackgroundResource(R.drawable.rounded_corners);
                GradientDrawable drawable = (GradientDrawable) button.getBackground();
                drawable.setColor(requireContext().getColor(R.color.grey));
                drawable.setAlpha(64);
                binding.progressLoader.setVisibility(View.GONE);
                binding.friendsLayout.addView(button, layout);
            }
        } else {
            TextView textView = new TextView(requireContext()); // no news feeds selected
            textView.setText(R.string.no_friends);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            requireActivity().runOnUiThread(() -> binding.friendsLayout.addView(textView, layout));
            binding.progressLoader.setVisibility(View.GONE);
        }
    }

    private void loadProfiles() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the cities collection
        CollectionReference boardsRef = db.collection("profiles");

        // Create a query against the collection.
        Query query = boardsRef.whereEqualTo("id",  Profile.localProfile.getFriend_ids());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Profile> profiles = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Profile profile = document.toObject(Profile.class);
                            profiles.add(profile);
                        }
                        loadFriends(profiles);
                    }
                });
    }
}
