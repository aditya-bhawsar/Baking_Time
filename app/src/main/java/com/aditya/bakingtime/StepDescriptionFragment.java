package com.aditya.bakingtime;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aditya.bakingtime.model.RecipeStep;
import com.aditya.bakingtime.util.JsonUtil;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class StepDescriptionFragment extends Fragment{

    Context mContext;

    boolean mLandscaped;
    int mStepPosition = -1;
    String mStepsData;
    List<RecipeStep> mListSteps;
    SimpleExoPlayer mExoPlayer;
    SimpleExoPlayerView mPlayerView;
    MediaSessionCompat mMediaSession;
    PlaybackStateCompat.Builder mStateBuilder;

    LinearLayout mStepLinearLayout;
    TextView mNameStepTv,mStepDetailTv;
    Button mNextBtn,mBackBtn;
    ImageView mDefaultIv;

    public StepDescriptionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        mMediaSession = new MediaSessionCompat(mContext, "StepDescription");
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setMediaButtonReceiver(null);
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY| PlaybackStateCompat.ACTION_PLAY_PAUSE| PlaybackStateCompat.ACTION_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MySessionCallback());
        mMediaSession.setActive(true);
    }

    @Override
    public void onDetach() {
        if(mExoPlayer!=null){
            mExoPlayer.release();
            mExoPlayer = null;
        }
        mMediaSession.setActive(false);
        mMediaSession = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.step_description_fragment,container,false);

        mLandscaped = view.findViewById(R.id.landscape_lay)!=null;
        mPlayerView = view.findViewById(R.id.player_view);
        mStepLinearLayout = view.findViewById(R.id.desc_linear_lay);
        mNextBtn = view.findViewById(R.id.next_btn);
        mBackBtn = view.findViewById(R.id.back_btn);
        mNameStepTv = view.findViewById(R.id.step_name_tv);
        mStepDetailTv = view.findViewById(R.id.step_desc_tv);

        if(savedInstanceState!=null){
            mStepsData = savedInstanceState.getString("stepData");
            setSteps(mStepsData);
            mStepPosition = savedInstanceState.getInt("stepPos");
        }

        if(!mLandscaped) mDefaultIv = view.findViewById(R.id.img_default);
        setUI();

        if(savedInstanceState!=null && savedInstanceState.getLong("playerPos")!=0 && savedInstanceState.getLong("playerPos")!=C.TIME_UNSET){
            mExoPlayer.seekTo(savedInstanceState.getLong("playerPos"));
            System.out.println("Ye Raha "+savedInstanceState.getLong("playerPos"));
        }

        mBackBtn.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {setPositionFromRv(mStepPosition-1); }});
        mNextBtn.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {  setPositionFromRv(mStepPosition+1); }});

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("stepData",mStepsData);
        outState.putInt("stepPos",mStepPosition);
        if(mListSteps.get(mStepPosition).getVideoURL()!=null && !mListSteps.get(mStepPosition).getVideoURL().isEmpty()){
            outState.putLong("playerPos", Math.max(0, mExoPlayer.getCurrentPosition()));
        }
        super.onSaveInstanceState(outState);
    }

    public void setUI(){
        if(mExoPlayer!=null)mExoPlayer.setPlayWhenReady(false);
        if(mDefaultIv!=null){mDefaultIv.setVisibility(View.GONE);}
        switch (mStepPosition){
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
                if(mListSteps.size()-1==mStepPosition){
                    mNextBtn.setVisibility(View.INVISIBLE);
                    mBackBtn.setVisibility(View.VISIBLE);
                }
                else {
                    mBackBtn.setVisibility(View.VISIBLE);
                    mNextBtn.setVisibility(View.VISIBLE);
                }
        }
        mStepDetailTv.setText(mListSteps.get(mStepPosition).getDescription());
        mNameStepTv.setText(mListSteps.get(mStepPosition).getShortDescription());

        if(mListSteps.get(mStepPosition).getVideoURL()==null || mListSteps.get(mStepPosition).getVideoURL().isEmpty()){
            if(mExoPlayer!=null) mExoPlayer.seekTo(0);
            if(!mLandscaped) mDefaultIv.setVisibility(View.VISIBLE);
            mPlayerView.setVisibility(View.GONE);
        }
        else {
            if(mLandscaped) mStepLinearLayout.setVisibility(View.GONE);
            initializePlayer();
        }
    }

    public void initializePlayer(){
        TrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext,trackSelector,loadControl);
        mExoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState){
                    case ExoPlayer.STATE_BUFFERING:
                        break;
                    case ExoPlayer.STATE_ENDED:
                        break;
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_READY:
                        if(playWhenReady){
                            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,mExoPlayer.getCurrentPosition(),1f);
                        }
                        else {
                            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,mExoPlayer.getCurrentPosition(),1f);
                        }
                        mMediaSession.setPlaybackState(mStateBuilder.build());
                        break;
                }
            }
            @Override public void onRepeatModeChanged(int repeatMode) {}
            @Override public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}
            @Override public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}
            @Override public void onLoadingChanged(boolean isLoading) {}
            @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
            @Override public void onPlayerError(ExoPlaybackException error) {}
            @Override public void onPositionDiscontinuity(int reason) {}
            @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
            @Override public void onSeekProcessed() {}
        });
        ((RecipeDetails)getActivity()).setPlayer(mExoPlayer);
        String userAgent = Util.getUserAgent(mContext,getString(R.string.app_name));
        Uri mediaUri = Uri.parse(mListSteps.get(mStepPosition).getVideoURL());
        MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(mContext,userAgent),new DefaultExtractorsFactory(), null,null);
        mExoPlayer.prepare(mediaSource, false,false);
        mPlayerView.setPlayer(mExoPlayer);
        mExoPlayer.setPlayWhenReady(true);
    }

    public void setSteps(String steps){
        mStepsData = steps;
        mListSteps = JsonUtil.parseJsonSteps(steps);
    }
    //public void setPositionFromActivity(int pos){ if(mStepPosition==-1) mStepPosition = pos; }
    public void setPositionFromRv(int pos) {
        mStepPosition = pos;
        setUI();
    }


    public class MySessionCallback extends MediaSessionCompat.Callback {
        @Override public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
            super.onPlay();
        }
        @Override public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
            super.onPause();
        }
    }
}