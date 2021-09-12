package com.mamunsproject.food_delevery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mamunsproject.food_delevery.CallBack.IRecyclerClickListener;
import com.mamunsproject.food_delevery.Common.Common;
import com.mamunsproject.food_delevery.EventBus.CategorClick;
import com.mamunsproject.food_delevery.Model.CategoryModel;
import com.mamunsproject.food_delevery.R;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MyCategoryAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.categoryImage);
        holder.category_name.setText(categoryModelList.get(position).getName());

        //Event
        holder.setListener((view, pos) -> {
            Common.categorySelected=categoryModelList.get(pos);

            EventBus.getDefault().postSticky(new CategorClick(true,categoryModelList.get(pos)));
        });
    }


    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public List<CategoryModel> getListCategory() {

        return categoryModelList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;
        @BindView(R.id.img_category)
        ImageView categoryImage;
        @BindView(R.id.text_category)
        TextView category_name;

        IRecyclerClickListener listener;


        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }




        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            listener.onItemClickListener(v,getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (categoryModelList.size()==1){
            return Common.DEFAULT_COLUMN_COUNT;

        }else {
            if (categoryModelList.size()%2==0){
                return Common.DEFAULT_COLUMN_COUNT;
            }else {
                return (position>1&&position==categoryModelList.size()-1)?Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
            }
        }

    }
}
