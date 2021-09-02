package com.mamunsproject.food_delevery.ui.Comments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamunsproject.food_delevery.Adapter.MyCommentAdapter;
import com.mamunsproject.food_delevery.CallBack.ICommentCallBackListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.Model.Comment_Model;
import com.mamunsproject.food_delevery.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CommentsFragment extends BottomSheetDialogFragment implements ICommentCallBackListener {

    private CommentViewModel commentViewModel;
    private Unbinder unbinder;

    @BindView(R.id.recycler_comment)
    RecyclerView recycler_comment;

    AlertDialog dialog;
    ICommentCallBackListener listener;

    public CommentsFragment() {
        listener=this;
    }

    private static CommentsFragment instance;

    public static CommentsFragment getInstance(){
        if (instance==null){
            instance=new CommentsFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View itemView=LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_comment_fragment, container, false);

        unbinder= ButterKnife.bind(this,itemView);
        initView();
        loadCommentsFromFirebase();
        commentViewModel.getMutableLiveDataFoddList().observe(this,commentModels -> {

            MyCommentAdapter adapter=new MyCommentAdapter(getContext(),commentModels);
            recycler_comment.setAdapter(adapter);
        });
        return itemView;
    }

    private void loadCommentsFromFirebase() {
        dialog.show();
        List<Comment_Model> commentModels=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF )
                .child(Common.selectedFood.getId())
                .orderByChild("serverTimeStamp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot commentSnapshot:snapshot.getChildren()){
                            Comment_Model comment_model=commentSnapshot.getValue(Comment_Model.class );
                            commentModels.add(comment_model);
                        }
                        listener.onCommentLoadSuccess(commentModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        listener.onCommentLoadFailed(error.getMessage());
                    }
                });
    }

    private void initView() {
        commentViewModel= new ViewModelProvider(this).get(CommentViewModel.class);
        dialog=new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        recycler_comment.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,true  );
        recycler_comment.setLayoutManager(layoutManager);
        recycler_comment.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

    }

    @Override
    public void onCommentLoadSuccess(List<Comment_Model> commentModels) {

        dialog.dismiss();
        commentViewModel.setCommentList(commentModels);
    }

    @Override
    public void onCommentLoadFailed(String message) {

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
