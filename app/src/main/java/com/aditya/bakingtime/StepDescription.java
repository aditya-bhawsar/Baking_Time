package com.aditya.bakingtime;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aditya.bakingtime.model.RecipeStep;
import com.aditya.bakingtime.util.JsonUtil;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StepDescription extends AppCompatActivity {

    static boolean continuePlayback = false;
    static long mResumePosition;

    private static SimpleExoPlayer player;
    static BandwidthMeter bandwidthMeter;
    static TrackSelection.Factory videoTrackSelectionFactory;
    static TrackSelector trackSelector;
    static LoadControl loadControl;
    static Uri mp4VideoUri;
    static DefaultBandwidthMeter bandwidthMeterA;
    static DefaultDataSourceFactory dataSourceFactory;
    static ExtractorsFactory extractorsFactory;
    static MediaSource videoSource;

    @BindView(R.id.player_view) SimpleExoPlayerView mPlayerView;
    @BindView(R.id.desc_linear_lay) LinearLayout mStepLinearLayout;
    @BindView(R.id.next_btn) Button mNextBtn;
    @BindView(R.id.back_btn) Button mBackBtn;
    @BindView(R.id.step_name_tv) TextView mNameStepTv;
    @BindView(R.id.step_desc_tv) TextView mStepDetailTv;

    ImageView mDefaultIv;

    boolean mLandscaped= true;
    List<RecipeStep> mStepsList;
    static int mCurrentPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_description);
        ButterKnife.bind(this);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(findViewById(R.id.landscape_lay)==null){
            mLandscaped = false;
            mDefaultIv = findViewById(R.id.img_default);
        }

        mStepsList = JsonUtil.parseJsonSteps(getIntent().getStringExtra("steps"));
        if(mCurrentPos==1){ mCurrentPos = getIntent().getIntExtra("position",0); }
        if(!continuePlayback) setUi();

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuePlayback = false;
                mCurrentPos++;
                if(player!=null)player.stop();
                setUi();
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuePlayback = false;
                mCurrentPos--;
                if(player!=null)player.stop();
                setUi();
            }
        });
    }

    public void setUi(){
        switch (mCurrentPos){
            case -1:
                mStepLinearLayout.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.GONE);
                return;
            case 0:
                mStepLinearLayout.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.VISIBLE);
                mBackBtn.setVisibility(View.INVISIBLE);
                mNextBtn.setVisibility(View.VISIBLE);
                break;
            default:
                mStepLinearLayout.setVisibility(View.VISIBLE);
                mPlayerView.setVisibility(View.VISIBLE);
                if(mStepsList.size()-1==mCurrentPos){
                    mNextBtn.setVisibility(View.INVISIBLE);
                    mBackBtn.setVisibility(View.VISIBLE);
                }
                else {
                    mBackBtn.setVisibility(View.VISIBLE);
                    mNextBtn.setVisibility(View.VISIBLE);
                }
        }
        mStepDetailTv.setText(mStepsList.get(mCurrentPos).getDescription());
        mNameStepTv.setText(mStepsList.get(mCurrentPos).getShortDescription());

        if(mStepsList.get(mCurrentPos).getVideoURL()==null || mStepsList.get(mCurrentPos).getVideoURL().isEmpty()){
            if(player!=null) player.seekTo(0);
            if(!mLandscaped) mDefaultIv.setVisibility(View.VISIBLE);
            mPlayerView.setVisibility(View.GONE);
        }
        else {
            if(mLandscaped) mStepLinearLayout.setVisibility(View.GONE);
            initializePlayer();
        }
    }

    public void initializePlayer(){
        bandwidthMeter = new DefaultBandwidthMeter();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        mPlayerView = new SimpleExoPlayerView(this);
        mPlayerView = findViewById(R.id.player_view);
        mPlayerView.setVisibility(View.VISIBLE);
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();
        mPlayerView.setPlayer(player);
        mp4VideoUri = JsonUtil.videoUri(mStepsList.get(mCurrentPos).getVideoURL());

        bandwidthMeterA = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeterA);
        extractorsFactory = new DefaultExtractorsFactory();
        videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        player.prepare(loopingSource);
        player.addListener(new ExoPlayer.EventListener() {
            @Override public void onTimelineChanged(Timeline timeline, Object manifest, int x) {}
            @Override public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}
            @Override public void onLoadingChanged(boolean isLoading) {}
            @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {}
            @Override public void onRepeatModeChanged(int repeatMode) {}
            @Override public void onPlayerError(ExoPlaybackException error) {}
            @Override public void onPositionDiscontinuity(int x) {}
            @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
            @Override public void onSeekProcessed() {}
            @Override public void onShuffleModeEnabledChanged(boolean x) {}
        });
        if(continuePlayback) { player.seekTo(mResumePosition); }
        player.setPlayWhenReady(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(continuePlayback) {
            setUi();
            continuePlayback = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            mResumePosition = Math.max(0, player.getCurrentPosition());
            continuePlayback = true;
            player.stop();
            player.release();
        }
    }
}
