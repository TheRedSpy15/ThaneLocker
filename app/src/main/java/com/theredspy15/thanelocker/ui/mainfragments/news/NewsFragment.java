package com.theredspy15.thanelocker.ui.mainfragments.news;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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

        if (entries == null) {
            Thread thread = new Thread(() -> {
            try  {
                List<SyndEntry> entries;
                entries=getFeed();
                NewsFragment.entries=entries;
                requireActivity().runOnUiThread(()->displayEntries(entries));
            } catch (Exception e) {
                e.printStackTrace();
            }
            });
            thread.start();
        } else displayEntries(entries);

        return root;
    }

    public void displayEntries(List<SyndEntry> entries) {
        if (entries != null) {
            for (SyndEntry entry: entries) {
                Button button = new Button(getContext());
                button.setText(entry.getTitle());
                button.setTextSize(18);
                button.setAllCaps(false);
                button.setPadding(50,50,50,50);
                //button.setBackgroundColor(getResources().getColor(R.color.grey));
                button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getUri()))));
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layout.setMargins(0,20,0,20);
                button.setBackgroundResource(R.drawable.rounded_corners);
                GradientDrawable drawable = (GradientDrawable) button.getBackground();
                drawable.setColor(getResources().getColor(R.color.grey));
                binding.progressLoader.setVisibility(View.GONE);
                binding.feedLayout.addView(button, layout);
            }
        }
    }

    public List<SyndEntry> getFeed() throws IOException, FeedException {
        String url = "https://downhill254.com/blog/feed";
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
        return feed.getEntries();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
