package com.mamunsproject.food_delevery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.bumptech.glide.Glide;
import com.mamunsproject.food_delevery.EventBus.BestDealItemClick;
import com.mamunsproject.food_delevery.Model.BestDealsModel;
import com.mamunsproject.food_delevery.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BestDealAdapter extends LoopingPagerAdapter<BestDealsModel> {


    @BindView(R.id.img_best_deal)
    ImageView image_best_deal;
    @BindView(R.id.text_best_deal)
    TextView text_best_deal;

    Unbinder unbinder;

    public BestDealAdapter(Context context, List<BestDealsModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deals,container,false);
    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {

        unbinder= ButterKnife.bind(this,convertView);
        Glide.with(convertView).load(itemList.get(listPosition).getImage()).into(image_best_deal);
        text_best_deal.setText(itemList.get(listPosition).getName());

        convertView.setOnClickListener(view -> {

            EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(listPosition)));
        });
    }
}
