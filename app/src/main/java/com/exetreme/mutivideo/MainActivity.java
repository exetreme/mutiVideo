package com.exetreme.mutivideo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.exetreme.mutivideo.Interface.IVideoLoadListener;
import com.exetreme.mutivideo.Model.VideoModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import edmt.dev.videoplayer.VideoPlayerRecyclerView;
import edmt.dev.videoplayer.adapter.VideoPlayerRecyclerAdapter;
import edmt.dev.videoplayer.model.MediaObject;
import edmt.dev.videoplayer.utils.VerticalSpacingItemDecorator;

public class MainActivity extends AppCompatActivity implements IVideoLoadListener {

    @BindView(R.id.shimmerFrameLayout)
    ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.video_player)
    VideoPlayerRecyclerView videoPlayerRecyclerView;
    IVideoLoadListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
    }

    private void init() {
        ButterKnife.bind(this);
        listener=this;

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        videoPlayerRecyclerView.setLayoutManager(layoutManager);
        VerticalSpacingItemDecorator decorator=new VerticalSpacingItemDecorator(10);
        videoPlayerRecyclerView.addItemDecoration(decorator);
        
        loadVideoFromFirebase();
    }

    private void loadVideoFromFirebase() {
        ArrayList<MediaObject> videoList=new ArrayList<>();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference("Video")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    for (DataSnapshot videoSnapShot:dataSnapshot.getChildren()){
                                        VideoModel videoModel = videoSnapShot.getValue(VideoModel.class);
                                        MediaObject mediaObject=new MediaObject(
                                                videoModel.getName(),
                                                videoModel.getMediaurl(),
                                                videoModel.getThumbnail(),""
                                        );
                                        videoList.add(mediaObject);
                                    }
                                    listener.onVideoLoadSuccess(videoList);

                                }
                                else{
                                    listener.onVideoLoadFailed("Video not found");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
listener.onVideoLoadFailed(databaseError.getMessage());
                            }
                        });
            }
        },3000);
    }

    @Override
    public void onVideoLoadSuccess(ArrayList<MediaObject> videoList) {
        videoPlayerRecyclerView.setMediaObjects(videoList);
        VideoPlayerRecyclerAdapter adapter = new VideoPlayerRecyclerAdapter(videoList,initGlide());
        videoPlayerRecyclerView.setAdapter(adapter);
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);
        return Glide.with(this).setDefaultRequestOptions(options);
    }

    @Override
    public void onVideoLoadFailed(String message) {
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}