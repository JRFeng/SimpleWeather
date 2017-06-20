package com.demo.simpleweather.activity;

import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.demo.simpleweather.R;
import com.demo.simpleweather.SwApplication;
import com.demo.simpleweather.adapter.HotCityAdapter;

import java.util.LinkedList;

public class AddCityActivity extends AppCompatActivity {
    public static final String KEY_RESULT ="new_city";

    private Toolbar toolbar;
    private RecyclerView rvCityList;

    private String[] hotCities;

    private String[] keyA;
    private String[] keyB;
    private String[] keyC;
    private String[] keyD;
    private String[] keyE;
    private String[] keyF;
    private String[] keyG;
    private String[] keyH;
    private String[] keyJ;
    private String[] keyK;
    private String[] keyL;
    private String[] keyM;
    private String[] keyN;
    private String[] keyP;
    private String[] keyQ;
    private String[] keyR;
    private String[] keyS;
    private String[] keyT;
    private String[] keyW;
    private String[] keyX;
    private String[] keyY;
    private String[] keyZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        //保存颜色值
        SwApplication.setData(getIntent().getIntExtra("weatherColorId", R.color.colorBlue));

        //findView
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rvCityList = (RecyclerView) findViewById(R.id.rvCityList);


        //initView
        toolbar.setBackgroundResource(getIntent().getIntExtra("weatherColorId", R.color.colorBlue));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //*****************************private***************************

    private void initView() {
        loadAllCities();

        VirtualLayoutManager vlManager = new VirtualLayoutManager(this);
        DelegateAdapter delegateAdapter = new DelegateAdapter(vlManager);
        rvCityList.setAdapter(delegateAdapter);
        rvCityList.setLayoutManager(vlManager);

        RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        recycledViewPool.setMaxRecycledViews(0, 10);
        rvCityList.setRecycledViewPool(recycledViewPool);

        LinkedList<DelegateAdapter.Adapter> adapters = new LinkedList<>();

        HotCityAdapter hotCityAdapter = new HotCityAdapter(this, hotCities);
        adapters.add(hotCityAdapter);



        delegateAdapter.setAdapters(adapters);
    }

    private void loadAllCities() {
        Resources resources = getResources();

        hotCities = resources.getStringArray(R.array.hotCities);

        keyA = resources.getStringArray(R.array.keyA);
        keyB = resources.getStringArray(R.array.keyB);
        keyC = resources.getStringArray(R.array.keyC);
        keyD = resources.getStringArray(R.array.keyD);
        keyE = resources.getStringArray(R.array.keyE);
        keyF = resources.getStringArray(R.array.keyF);
        keyG = resources.getStringArray(R.array.keyG);
        keyH = resources.getStringArray(R.array.keyH);
        keyJ = resources.getStringArray(R.array.keyJ);
        keyK = resources.getStringArray(R.array.keyK);
        keyL = resources.getStringArray(R.array.keyL);
        keyM = resources.getStringArray(R.array.keyM);
        keyN = resources.getStringArray(R.array.keyN);
        keyP = resources.getStringArray(R.array.keyP);
        keyQ = resources.getStringArray(R.array.keyQ);
        keyR = resources.getStringArray(R.array.keyR);
        keyS = resources.getStringArray(R.array.keyS);
        keyT = resources.getStringArray(R.array.keyT);
        keyW = resources.getStringArray(R.array.keyW);
        keyX = resources.getStringArray(R.array.keyX);
        keyY = resources.getStringArray(R.array.keyY);
        keyZ = resources.getStringArray(R.array.keyZ);
    }
}
