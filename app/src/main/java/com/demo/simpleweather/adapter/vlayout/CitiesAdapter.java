package com.demo.simpleweather.adapter.vlayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.demo.simpleweather.SWApplication;
import com.demo.simpleweather.activity.AddCityActivity;
import com.demo.simpleweather.R;


public class CitiesAdapter extends DelegateAdapter.Adapter<CitiesAdapter.ViewHolder> {
    private Activity mActivity;
    private String[] mHotCities;
    private int color;

    public CitiesAdapter(Activity activity, String[] hotCities) {
        mActivity = activity;
        mHotCities = hotCities;
        color = mActivity.getResources().getColor(SWApplication.getSharedData()); //getColor方法是API23才弃用的
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        GridLayoutHelper layoutHelper = new GridLayoutHelper(3);
        layoutHelper.setPadding(
                SWApplication.getPx(32),
                SWApplication.getPx(8),
                SWApplication.getPx(0),
                SWApplication.getPx(8));
//        layoutHelper.setHGap(SWApplication.getInstance().getPx(32));

        layoutHelper.setBgColor(mActivity.getResources().getColor(android.R.color.white));//getColor方法是API23才弃用的
        layoutHelper.setAutoExpand(false);

        return layoutHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.city_name_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvHotCityName.setText(mHotCities[position]);
        holder.tvHotCityName.setTextColor(color);

        GradientDrawable gradientDrawable = (GradientDrawable) holder.tvHotCityName.getBackground();
        gradientDrawable.setStroke(SWApplication.getPx(1), color);

        holder.tvHotCityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(mActivity)
                        .setTitle("添加？")
                        .setMessage("当前选择的是 : " + holder.tvHotCityName.getText().toString())
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(AddCityActivity.KEY_RESULT, holder.tvHotCityName.getText().toString());
                                mActivity.setResult(Activity.RESULT_OK, resultIntent);
                                mActivity.finish();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHotCities.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotCityName;

        ViewHolder(View itemView) {
            super(itemView);
            //注释掉这些类型转换是因为Lint检测出这些类型转换是多于的，如果后续有需求，可以取消注释。
            tvHotCityName = /*(TextView)*/ itemView.findViewById(R.id.tvHotCityName);
        }
    }
}
