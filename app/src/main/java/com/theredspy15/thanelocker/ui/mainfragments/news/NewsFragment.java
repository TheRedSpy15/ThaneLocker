package com.theredspy15.thanelocker.ui.mainfragments.news;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentNewsBinding;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class NewsFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private FragmentNewsBinding binding;

    public static List<SyndEntry> entries;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newsViewModel =
                new ViewModelProvider(this).get(NewsViewModel.class);

        binding = FragmentNewsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Thread thread = new Thread(() -> {
            try  {
                List<SyndEntry> entries;
                entries=getFeed();
                NewsFragment.entries=entries;
                while (!isAdded()) {} // fixes a rare occurrence of crash when switching fragments too fast
                requireActivity().runOnUiThread(()->displayEntries(entries));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        return root;
    }

    public void displayEntries(List<SyndEntry> entries) {
        binding.feedLayout.removeAllViews();
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        if (entries != null) {
            for (SyndEntry entry: entries) {
                Button button = new Button(getContext());
                button.setText(entry.getTitle());
                button.setTextSize(18);
                button.setAllCaps(false);
                button.setPadding(50,50,50,50);
                layout.setMargins(0,20,0,20);
                button.setBackgroundResource(R.drawable.rounded_corners);
                GradientDrawable drawable = (GradientDrawable) button.getBackground();
                drawable.setColor(requireContext().getColor(R.color.grey));
                drawable.setAlpha(64);
                binding.progressLoader.setVisibility(View.GONE);
                binding.feedLayout.addView(button, layout);
            }

            if (entries.isEmpty()) {
                TextView textView = new TextView(requireContext()); // no news feeds selected
                textView.setText("No news");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                requireActivity().runOnUiThread(() -> binding.feedLayout.addView(textView, layout));
            }
        } else {
            TextView textView = new TextView(requireContext()); // no news feeds selected
            textView.setText("No news");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            requireActivity().runOnUiThread(() -> binding.feedLayout.addView(textView, layout));
        }
    }

    public List<SyndEntry> getFeed() throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;

        if (MainActivity.preferences.getBoolean("downhill254",true)) {
            String url = "https://downhill254.com/blog/feed";
            feed = input.build(new XmlReader(new URL(url)));
        }

        if (MainActivity.preferences.getBoolean("longboardbrand",false)) {
            String url = "https://longboardbrand.com/blog/feed";
            feed = input.build(new XmlReader(new URL(url)));
        }

        if (feed != null) return feed.getEntries();
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
