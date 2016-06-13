package com.example.chukc.pulltozoomlistview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.chukc.pulltozoomlistview.view.listview.PullableListView;
import com.example.chukc.pulltozoomlistview.view.listview.PullableListViewRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullableListView listView;
    private MainAdapter adapter;
    private List<String> listText= new ArrayList<String>();
    private static final ArrayList<Integer> listImg = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*上拉加载由PullableListView1控制 下拉刷新由PullToRefreshLayout1控制*/
        listView = (PullableListView) findViewById(R.id.listview);
        listView.setPullRefreshEnable(new PullableListViewRefreshListener() {
            @Override
            public void onRefresh() {
                for (int i = 0; i < 10; i++) {
                    listText.add("哈哈");
                }
                adapter.notifyDataSetChanged();
                listView.stopRefresh();
            }
        });
        listView.setOnLoadListener(new PullableListView.OnLoadListener() {
            @Override
            public void onLoad(PullableListView pullableListView) {
                load();
            }
        });

        adapter = new MainAdapter(this,listText);
        listView.setAdapter(adapter);

        listImg.add(R.mipmap.bgtop);
        listImg.add(R.mipmap.a );
        listImg.add(R.mipmap.b );
        listImg.add(R.mipmap.c );
        listView.setImgUrls(listImg);
    }


    private void load(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    if(listText.size()<30)
                        listText.add("哈哈");
                }
                if(listText.size()<10){
                    listView.setFinishedFooter();
                    adapter.notifyDataSetChanged();
                }else{
                    if(listText.size()>=30){
                        listView.setFinishedFooter();
                        Toast.makeText(MainActivity.this,"没有更多数据",Toast.LENGTH_LONG).show();
                    }else{
                        adapter.notifyDataSetChanged();
                        listView.finishLoading();
                    }
                }
            }
        }, 5000);
    }
}
