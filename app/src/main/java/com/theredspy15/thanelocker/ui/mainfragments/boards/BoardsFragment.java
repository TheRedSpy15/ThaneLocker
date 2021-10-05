package com.theredspy15.thanelocker.ui.mainfragments.boards;

import android.app.Activity;
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

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Image;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.NewBoardActivity;
import com.theredspy15.thanelocker.utils.App;

public class BoardsFragment extends Fragment {

    private FragmentBoardsBinding binding;

    Context context;

    Thread boardThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBoardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        App.cleanBoards();

        binding.newBoardButton.setOnClickListener(this::loadCreateBoard);
        return root;
    }

    @Override
    public void onStart() {
        context = getContext();
        if (context == null) context = requireContext();

        super.onStart();
        boardThread = new Thread(this::loadBoards);
        boardThread.start();
    }

    public void loadBoards() {
        if (isAdded()) requireActivity().runOnUiThread(()->binding.boardLayout.removeAllViews());

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

                    if (board.getImage() != null && board.getImage().getData() != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                Image.convertImageStringToBytes(board.getImage().getData()),
                                0, Image.convertImageStringToBytes(board.getImage().getData()).length);
                        Drawable drawable = new BitmapDrawable(context.getResources(),Bitmap.createScaledBitmap(bitmap, 500, 500, false));
                        button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
                    }

                    Context finalContext = context;
                    button.setOnClickListener(v->{
                        Intent myIntent = new Intent(finalContext, BoardActivity.class);
                        myIntent.putExtra("board_id", board.getId());
                        startActivity(myIntent);
                    });

                    Context finalContext1 = context;
                    button.setOnLongClickListener(v->{
                        AlertDialog alertDialog = new AlertDialog.Builder(finalContext1).create();
                        alertDialog.setTitle(getString(R.string.delete_board));
                        alertDialog.setMessage(getString(R.string.are_you_sure));
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    Board.savedBoards.remove(board.getId());
                                    Board.savedBoardIds.remove((Integer) board.getId());
                                    boardThread = new Thread(this::loadBoards);
                                    boardThread.start();
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                                (dialog, which) -> dialog.dismiss());
                        alertDialog.show();
                        return false;
                    });

                    if (binding != null) ((Activity)context).runOnUiThread(()->binding.boardLayout.addView(button,layout));
                }
            }
            if (Board.savedBoardIds.isEmpty()) { // no boards
                TextView textView = new TextView(context);
                textView.setText(R.string.no_boards);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                if (isAdded()) requireActivity().runOnUiThread(()->binding.boardLayout.addView(textView,layout));
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