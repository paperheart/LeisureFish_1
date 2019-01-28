package com.blossom.leisurefish;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import beans.Feed;
import beans.FeedResponse;
import beans.PostVideoResponse;
import network.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    MyAdapter adapter;
    public List<Feed> mFeeds;
    ImageButton plus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       fetchFeed();
        int permission_0 = ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_1 = ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.INTERNET);
        int permission_2 = ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_NETWORK_STATE);
        int permission_3 = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO);
        int permission_4 = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA);
        if(permission_0 != PackageManager.PERMISSION_GRANTED
                || permission_1 != PackageManager.PERMISSION_GRANTED
                || permission_2 != PackageManager.PERMISSION_GRANTED
                || permission_3 != PackageManager.PERMISSION_GRANTED
                || permission_4 != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this , new String[]{
                      android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , android.Manifest.permission.INTERNET
                    , android.Manifest.permission.ACCESS_NETWORK_STATE
                    , android.Manifest.permission.RECORD_AUDIO
                    , android.Manifest.permission.CAMERA
            },1);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        plus = findViewById(R.id.ib_plus);


        recyclerView = findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter = new MyAdapter());


        plus.setOnClickListener(new View.OnClickListener() {
            @Override
                      public void onClick(View view) {
                try {
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                }catch (Exception e)
                {
                    Toast.makeText(MainActivity.this,"Click failed",Toast.LENGTH_LONG).show();
                }
                      }
                  });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        /**
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        */
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void fetchFeed(){

        getResponse(new Callback<FeedResponse>() {
            @Override public void onResponse(Call<FeedResponse> call, final Response<FeedResponse> response){
                if (recyclerView  != null ) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mFeeds = response.body().getFeedList();
                            adapter.replaceAll(mFeeds);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }


            }
            @Override public void onFailure(Call<FeedResponse> call, Throwable t) {

            }
        });
    }



    public  void getResponse(Callback<FeedResponse> callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.108.10.39:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.create(Service.class).getFeed().
                enqueue(callback);
    }


}
