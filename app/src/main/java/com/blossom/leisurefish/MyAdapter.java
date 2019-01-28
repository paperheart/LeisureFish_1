package com.blossom.leisurefish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import beans.Feed;

import static android.support.v4.content.ContextCompat.startActivity;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<Feed> dataList = new ArrayList<>();
    public void replaceAll(List<Feed> feedList) {
        dataList.clear();
        if (feedList != null && feedList.size() > 0) {
            dataList.addAll(feedList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.priview_item, viewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.setData(i);
        myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 取消注释 绑定详情页

                Intent intent = new Intent(myViewHolder.itemView.getContext(),Detail.class);
                intent.putExtra("VIDEO_URL",dataList.get(i).getVurl());
                intent.putExtra("USER_ID",dataList.get(i).getId());
                intent.putExtra("USER_NAME",dataList.get(i).getName());


                myViewHolder.itemView.getContext().startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            int width = ((Activity) imageView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = width/2;
            params.height =  (int) (200 + Math.random() * 400) ;
            imageView.setLayoutParams(params);
        }


        //TODO 加载预览图 .placeholder(R.mipmap.ic_launcher)
        void setData(int i) {
            if (dataList.get(i) != null) {
                String imageUrl = dataList.get(i).getIurl();
                Glide.with(itemView.getContext()).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(imageView);
            }
        }
    }


}
