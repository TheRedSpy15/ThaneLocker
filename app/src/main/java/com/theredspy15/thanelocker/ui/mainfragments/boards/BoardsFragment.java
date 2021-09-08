package com.theredspy15.thanelocker.ui.mainfragments.boards;

import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.NewBoardActivity;
import com.theredspy15.thanelocker.utils.App;

public class BoardsFragment extends Fragment {

    private BoardsViewModel boardsViewModel;
    private FragmentBoardsBinding binding;

    Thread boardThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        boardsViewModel =
                new ViewModelProvider(this).get(BoardsViewModel.class);

        binding = FragmentBoardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.newBoardButton.setOnClickListener(this::loadCreateBoard);

        boardThread = new Thread(() -> loadBoards(App.getContext()));
        boardThread.start();
        return root;
    }

    public void loadBoards(Context context) {
        requireActivity().runOnUiThread(()->binding.boardLayout.removeAllViews());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0,20,0,20);

        if (Board.savedBoards != null) { // add boards // TODO: using a lot of same code in SessionActivity to create board button, maybe a single function for both?
            for (int board_id : Board.savedBoardIds) {
                Board board = Board.savedBoards.get(board_id);
                if (board != null) {
                    Button button = new Button(context);
                    button.setText(board.getName());
                    button.setTextSize(18);
                    button.setBackgroundColor(context.getColor(R.color.grey));
                    button.getBackground().setAlpha(64);
                    button.setPadding(0,0,0,0);
                    button.setAllCaps(false);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                    Drawable drawable = new BitmapDrawable(context.getResources(),Bitmap.createScaledBitmap(bitmap, 500, 500, false));
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);

                    button.setOnClickListener(v->{
                        Intent myIntent = new Intent(context, BoardActivity.class);
                        myIntent.putExtra("board", board);
                        startActivity(myIntent);
                    });

                    button.setOnLongClickListener(v->{
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Remove board from session");
                        alertDialog.setMessage("Are you sure?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    Board.savedBoards.remove(board.getId());
                                    Board.savedBoardIds.remove(Board.savedBoardIds.indexOf(board.getId())); // removing by object doesn't work
                                    Board.save();
                                    boardThread = new Thread(() -> loadBoards(App.getContext()));
                                    boardThread.start();
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                (dialog, which) -> dialog.dismiss());
                        alertDialog.show();
                        return false;
                    });

                    // throws null exception if changing fragments before finished loading all boards
                    if (isAdded() && !isDetached() && isVisible()) requireActivity().runOnUiThread(()->binding.boardLayout.addView(button,layout));
                }
            }
            if (Board.savedBoardIds.isEmpty()) { // no boards
                TextView textView = new TextView(context);
                textView.setText("No Boards Saved");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                requireActivity().runOnUiThread(()->binding.boardLayout.addView(textView,layout));
            }
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