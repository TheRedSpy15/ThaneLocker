package com.theredspy15.thanelocker.ui.mainfragments.boards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.customviews.BoardView;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.NewBoardActivity;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.Purchasing;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class BoardsFragment extends Fragment {

    private FragmentBoardsBinding binding;

    Context context;

    Thread boardThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBoardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        App.cleanBoards();

        if (!MainActivity.preferences.getBoolean("subscribe",false)) enableLimit();

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

    private void enableLimit() {
        if (Board.savedBoards.size() >= 2) {
            binding.newBoardButton.setText(R.string.free_limit_reached);
            binding.newBoardButton.setOnClickListener(v->{
                Intent myIntent = new Intent(requireContext(), MainActivity.class);
                startActivity(myIntent);
                new Purchasing(requireContext()).subscribe(requireContext(),requireActivity());
            });
        }
    }

    public void loadBoards() {
        if (isAdded()) requireActivity().runOnUiThread(()->binding.boardLayout.removeAllViews());

        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0,20,0,20);

        if (Board.savedBoards != null) { // add boards // TODO: using a lot of same code in SessionActivity to create board button, maybe a single function for both?
            for (int board_id : Board.savedBoardIds) {
                Board board = Board.savedBoards.get(board_id);
                if (board != null) {
                    BoardView boardView = new BoardView(context);
                    boardView.setTextName(board.getName());
                    boardView.setTextDistance("Distance: 05.3 Miles");
                    boardView.setTextSpeed("Average Speed: 14 MPH");
                    boardView.setBackgroundColor(context.getColor(R.color.grey));
                    boardView.getBackground().setAlpha(30);
                    boardView.setPadding(0,0,0,0);

                    if (board.getImage() != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                        boardView.setBitmap(bitmap);
                    } else {
                        boardView.setDrawable(AppCompatResources.getDrawable(requireContext(),R.drawable.ic_baseline_image_24));
                    }

                    Context finalContext = context;
                    boardView.setOnClickListener(v->{
                        Intent myIntent = new Intent(finalContext, BoardActivity.class);
                        myIntent.putExtra("board_id", board.getId());
                        startActivity(myIntent);
                    });

                    boardView.setOnLongClickListener(v->{
                        MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                                .setTitle(getString(R.string.delete_board))
                                .setAnimation("58413-delete-icon-animation.json")
                                .setMessage(getString(R.string.are_you_sure))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                                    dialogInterface.dismiss();
                                    Board.savedBoards.remove(board.getId());
                                    Board.savedBoardIds.remove((Integer) board.getId());
                                    boardThread = new Thread(this::loadBoards);
                                    boardThread.start();
                                })
                                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                                .build();
                        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        mDialog.show();
                        return false;
                    });

                    if (binding != null) ((Activity)context).runOnUiThread(()->binding.boardLayout.addView(boardView,layout));
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