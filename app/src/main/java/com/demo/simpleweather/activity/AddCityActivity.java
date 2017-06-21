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
import com.demo.simpleweather.adapter.CityNameAdapter;
import com.demo.simpleweather.adapter.TitleAdapter;

import java.util.LinkedList;

public class AddCityActivity extends AppCompatActivity {
    public static final String KEY_RESULT = "new_city";

    private RecyclerView rvCityList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        //保存颜色值
        SwApplication.setSharedData(getIntent().getIntExtra("weatherColorId", R.color.colorBlue));

        //findView
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        delegateAdapter.setAdapters(createAdapters());
    }

    private LinkedList<DelegateAdapter.Adapter> createAdapters() {
        LinkedList<DelegateAdapter.Adapter> adapters = new LinkedList<>();

        String[] titles = {"热门城市：", "A", "B", "C", "D", "E",
                "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q",
                "R", "S", "T", "W", "X", "Y", "Z"};

        String[][] cities = loadAllCities();

        for (int i = 0; i < titles.length; i++) {
            adapters.add(new TitleAdapter(this, titles[i]));
            adapters.add(new CityNameAdapter(this, cities[i]));
        }

        return adapters;
    }

    private String[][] loadAllCities() {
        Resources resources = getResources();

        String[] hotCities = resources.getStringArray(R.array.hotCities);

        String[] keyA = resources.getStringArray(R.array.keyA);
        String[] keyB = resources.getStringArray(R.array.keyB);
        String[] keyC = resources.getStringArray(R.array.keyC);
        String[] keyD = resources.getStringArray(R.array.keyD);
        String[] keyE = resources.getStringArray(R.array.keyE);
        String[] keyF = resources.getStringArray(R.array.keyF);
        String[] keyG = resources.getStringArray(R.array.keyG);
        String[] keyH = resources.getStringArray(R.array.keyH);
        String[] keyJ = resources.getStringArray(R.array.keyJ);
        String[] keyK = resources.getStringArray(R.array.keyK);
        String[] keyL = resources.getStringArray(R.array.keyL);
        String[] keyM = resources.getStringArray(R.array.keyM);
        String[] keyN = resources.getStringArray(R.array.keyN);
        String[] keyP = resources.getStringArray(R.array.keyP);
        String[] keyQ = resources.getStringArray(R.array.keyQ);
        String[] keyR = resources.getStringArray(R.array.keyR);
        String[] keyS = resources.getStringArray(R.array.keyS);
        String[] keyT = resources.getStringArray(R.array.keyT);
        String[] keyW = resources.getStringArray(R.array.keyW);
        String[] keyX = resources.getStringArray(R.array.keyX);
        String[] keyY = resources.getStringArray(R.array.keyY);
        String[] keyZ = resources.getStringArray(R.array.keyZ);

        return new String[][]{hotCities, keyA, keyB, keyC, keyD, keyE,
                keyF, keyG, keyH, keyJ, keyK, keyL, keyM, keyN, keyP,
                keyQ, keyR, keyS, keyT, keyW, keyX, keyY, keyZ};
    }
}
