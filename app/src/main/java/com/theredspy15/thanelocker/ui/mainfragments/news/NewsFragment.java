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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentNewsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class NewsFragment extends Fragment {

    private FragmentNewsBinding binding;

    public static List<SyndEntry> entries;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.swipeRefreshLayout.setOnRefreshListener(this::initFeed);
        binding.swipeRefreshLayout.setRefreshing(true);
        initFeed();

        return root;
    }

    private void initFeed() {
        Thread thread = new Thread(() -> {
            try  {
                List<SyndEntry> entries;
                entries=getFeed();
                NewsFragment.entries=entries;
                while (!isAdded()) {} // fixes a rare occurrence of crash when switching fragments too fast
                requireActivity().runOnUiThread(()->displayEntries(entries));
            } catch (FeedException e) {
                feedError();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void feedError() {
        requireActivity().runOnUiThread(()->binding.feedLayout.removeAllViews());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        TextView textView = new TextView(requireContext()); // no news feeds selected
        textView.setText(R.string.failed_news);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextSize(18);
        requireActivity().runOnUiThread(() -> binding.feedLayout.addView(textView, layout));
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
                button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getUri()))));
                button.setPadding(50,50,50,50);
                layout.setMargins(0,20,0,20);
                button.setBackgroundResource(R.drawable.rounded_corners);
                GradientDrawable drawable = (GradientDrawable) button.getBackground();
                drawable.setColor(requireContext().getColor(R.color.grey));
                drawable.setAlpha(64);
                binding.feedLayout.addView(button, layout);
            }
        } else if (entries == null || entries.isEmpty()) {
            TextView textView = new TextView(requireContext()); // no news feeds selected
            textView.setText(R.string.no_news);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            requireActivity().runOnUiThread(() -> binding.feedLayout.addView(textView, layout));
        }
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    public List<SyndEntry> getFeed() throws FeedException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;

        if (MainActivity.preferences.getBoolean("downhill254",true)) {
            String url = "https://downhill254.com/blog/feed";
            try {
                feed = input.build(new XmlReader(new URL(url)));
            } catch (IOException e) {
                e.printStackTrace();
                MotionToast.Companion.createColorToast(
                        requireActivity(),
                        getString(R.string.failed_news),
                        getString(R.string.failed_connect_downhill254),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.montserrat_regular)
                );
            }
        }

        if (MainActivity.preferences.getBoolean("longboardbrand",false)) {
            String url = "https://longboardbrand.com/blog/feed";
            try {
                feed = input.build(new XmlReader(new URL(url)));
            } catch (IOException e) {
                e.printStackTrace();
                MotionToast.Companion.createColorToast(
                        requireActivity(),
                        getString(R.string.failed_news),
                        getString(R.string.failed_connect_longboardbrand),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.montserrat_regular)
                );
            }
        }

        if (MainActivity.preferences.getBoolean("basementskate",false)) {
            String url = "https://www.basementskate.com.au/blog/feed/";
            try {
                feed = input.build(new XmlReader(new URL(url)));
            } catch (IOException e) {
                e.printStackTrace();
                MotionToast.Companion.createColorToast(
                        requireActivity(),
                        getString(R.string.failed_news),
                        getString(R.string.failed_connect_basementskate),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.montserrat_regular)
                );
            }
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
