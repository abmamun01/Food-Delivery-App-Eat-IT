package com.mamunsproject.food_delevery.Adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.DataUtils;
import com.mamunsproject.food_delevery.Model.Comment_Model;
import com.mamunsproject.food_delevery.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.MyViewHolder> {

    Context context;
    List<Comment_Model> comment_modelList;

    public MyCommentAdapter(Context context, List<Comment_Model> comment_modelList) {
        this.context = context;
        this.comment_modelList = comment_modelList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_commment,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Long timeStamp=Long.valueOf(comment_modelList.get(position).getCommentTimeStamp().get("timeStamp").toString());
        holder.txt_comment_date.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
        holder.txt_comment.setText(comment_modelList.get(position).getComment());
        holder.text_comment_name.setText(comment_modelList.get(position).getName());
        holder.ratingBar.setRating(comment_modelList.get(position).getRatingValue());
    }

    @Override
    public int getItemCount() {
        return comment_modelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;
        @BindView(R.id.txt_comment_date)
        TextView txt_comment_date;
        @BindView(R.id.txt_comment)
        TextView txt_comment;
        @BindView(R.id.txt_comment_name)
        TextView text_comment_name;
        @BindView(R.id.rating_bar)
        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
        }
    }
}
