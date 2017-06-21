package com.demo.simpleweather.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.SingleLayoutHelper;

import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.demo.simpleweather.R;
import com.demo.simpleweather.SwApplication;

public class TitleAdapter extends DelegateAdapter.Adapter<TitleAdapter.ViewHolder> {
    private Context mContext;
    private String mTitle;

    public TitleAdapter(Context context, String title) {
        mContext = context;
        mTitle = title;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new StickyLayoutHelper(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.vlayout_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(mTitle);
        holder.title.setTextColor(mContext.getResources().getColor(SwApplication.getSharedData()));
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }
}
