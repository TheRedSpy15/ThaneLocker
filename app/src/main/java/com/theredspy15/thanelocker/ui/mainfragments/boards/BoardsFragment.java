package com.theredspy15.thanelocker.ui.mainfragments.boards;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import com.example.thanelocker.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.NewBoardActivity;
import com.theredspy15.thanelocker.utils.SavedDataManager;

import java.io.FileNotFoundException;

public class BoardsFragment extends Fragment {

    private BoardsViewModel boardsViewModel;
    private FragmentBoardsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        boardsViewModel =
                new ViewModelProvider(this).get(BoardsViewModel.class);

        binding = FragmentBoardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        try {
            loadBoards();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return root;
    }

    public void loadBoards() throws FileNotFoundException {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0,20,0,20);

        if (SavedDataManager.savedBoards != null) {
            for (Board board: SavedDataManager.savedBoards) {
                Button button = new Button(getContext());
                button.setText(board.getName());
                button.setTextSize(18);
                button.setBackgroundColor(getResources().getColor(R.color.grey));
                button.setPadding(50,150,50,150);
                button.setAllCaps(false);
                button.setOnClickListener(v->{
                    Intent myIntent = new Intent(getContext(), BoardActivity.class);
                    myIntent.putExtra("board", board);
                    startActivity(myIntent);
                });
                Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                Drawable drawable = new BitmapDrawable(this.getResources(),bitmap);
                button.setCompoundDrawables(drawable,null,null,null);
                binding.boardLayout.addView(button,layout);
            }
        }
        // TODO: move to xml and attach to bottom of screen
        Button button = new Button(getContext());
        button.setText("Add Board");
        button.setTextSize(18);
        button.setPadding(50,50,50,50);
        button.setBackgroundColor(getResources().getColor(R.color.grey));
        button.setOnClickListener(this::loadCreateBoard);
        button.setBackgroundResource(R.drawable.rounded_corners);
        GradientDrawable drawable = (GradientDrawable) button.getBackground();
        drawable.setColor(getResources().getColor(R.color.design_default_color_primary));
        binding.boardLayout.addView(button, layout);
    }

    public void loadCreateBoard(View view) {
        Intent myIntent = new Intent(getContext(), NewBoardActivity.class);
        startActivity(myIntent);
    }
            
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}