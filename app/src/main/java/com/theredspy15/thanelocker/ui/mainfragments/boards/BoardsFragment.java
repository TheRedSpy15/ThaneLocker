package com.theredspy15.thanelocker.ui.mainfragments.boards;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.example.thanelocker.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.NewBoardActivity;

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

        binding.newBoardButton.setOnClickListener(this::loadCreateBoard);

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

        if (Board.savedBoards != null) { // add boards
            for (short board_id : Board.savedBoardIds) {
                Board board = Board.savedBoards.get(board_id);
                Button button = new Button(getContext());
                button.setText(board.getName());
                button.setTextSize(18);
                button.setBackgroundColor(getResources().getColor(R.color.grey));
                button.setPadding(0,0,0,0);
                button.setAllCaps(false);
                button.setOnClickListener(v->{
                    Intent myIntent = new Intent(getContext(), BoardActivity.class);
                    myIntent.putExtra("board", board);
                    startActivity(myIntent);
                });
                Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 500, 500, false));
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
                binding.boardLayout.addView(button,layout);
            }
        }
        if (Board.savedBoardIds.isEmpty()) { // no boards
            TextView textView = new TextView(requireContext());
            textView.setText("No Boards Saved");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            binding.boardLayout.addView(textView,layout);
        }
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