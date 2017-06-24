package com.demo.simpleweather.adapter.vlayout;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;

import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.demo.simpleweather.R;
import com.demo.simpleweather.SWApplication;

public class TitlesAdapter extends DelegateAdapter.Adapter<TitlesAdapter.ViewHolder> {
    private Context mContext;
    private String mTitle;

    public TitlesAdapter(Context context, String title) {
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
        holder.title.setTextColor(mContext.getResources().getColor(SWApplication.getSharedData()));
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            //注释掉这些类型转换是因为Lint检测出这些类型转换是多于的，如果后续有需求，可以取消注释。
            title = /*(TextView)*/ itemView.findViewById(R.id.tvTitle);
        }
    }
}
