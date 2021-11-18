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

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentNewsBinding;
import com.prof.rssparser.Article;
import com.prof.rssparser.Channel;
import com.prof.rssparser.OnTaskCompleted;
import com.prof.rssparser.Parser;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.nio.charset.Charset;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class NewsFragment extends Fragment {

    private FragmentNewsBinding binding;

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
        binding.feedLayout.removeAllViews();
        getFeeds();
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

    public synchronized void displayEntries(List<Article> articles) {
        try {
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout.setMargins(0, 20, 0, 20);

            if (articles != null) {
                for (Article entry: articles) {
                    Button button = new Button(requireContext());
                    button.setText(entry.getTitle());
                    button.setTextSize(18);
                    button.setAllCaps(false);
                    button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()))));
                    button.setPadding(50,50,50,50);
                    layout.setMargins(0,20,0,20);
                    button.setBackgroundResource(R.drawable.rounded_corners);
                    GradientDrawable drawable = (GradientDrawable) button.getBackground();
                    drawable.setColor(requireContext().getColor(R.color.grey));
                    drawable.setAlpha(30);
                    requireActivity().runOnUiThread(()->binding.feedLayout.addView(button,layout));
                }
            } else if (articles == null || articles.isEmpty()) {
                TextView textView = new TextView(requireContext()); // no news feeds selected
                textView.setText(R.string.no_news);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                requireActivity().runOnUiThread(() -> binding.feedLayout.addView(textView, layout));
            }
            binding.swipeRefreshLayout.setRefreshing(false);
        } catch (NullPointerException | IllegalStateException e) {
            // TODO something
        }
    }

    public void getFeeds() { // TODO: sort by date when using more than one source
        Parser parser = new Parser.Builder()
                .charset(Charset.forName("ISO-8859-7"))
                .context(requireContext())
                .cacheExpirationMillis(24L * 60L * 60L * 100L)
                .build();

        parser.onFinish(new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(@NonNull Channel channel) {
                displayEntries(channel.getArticles());
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (e.getMessage() != null) {
                    MotionToast.Companion.createColorToast(
                            requireActivity(),
                            getString(R.string.failed_news),
                            e.getMessage(),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(requireContext(), R.font.roboto)
                    );
                }
            }
        });

        if (MainActivity.preferences.getBoolean("downhill254",true))
            parser.execute("https://downhill254.com/blog/feed");
        if (MainActivity.preferences.getBoolean("longboardbrand",true))
            parser.execute("https://longboardbrand.com/blog/feed");
        if (MainActivity.preferences.getBoolean("basementskate",true))
            parser.execute("https://www.basementskate.com.au/blog/feed/");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
