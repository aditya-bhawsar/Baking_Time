package com.aditya.bakingtime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aditya.bakingtime.model.Recipe;
import com.aditya.bakingtime.util.JsonUtil;
import com.aditya.bakingtime.network.NetworkUtils;
import com.aditya.bakingtime.adapters.RecipeAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.ClickListener , LoaderManager.LoaderCallbacks<String> {

    List<Recipe> recipeList;
    RecipeAdapter mAdapter;

    @BindView(R.id.rv_list_recipe) RecyclerView mRecipeRv;
    @BindView(R.id.msg_tv) TextView mTvMsg;
    @BindView(R.id.iv_no_internet)ImageView mIvError;
    @BindView(R.id.load_pb)ProgressBar mPbLoader;
    @BindView(R.id.state_lay)LinearLayout mStateLay;

    public static final int LOADER_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new RecipeAdapter(recipeList,this);

        int spanCount =1;
        if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE){spanCount = 3;}

        mRecipeRv.setLayoutManager(new GridLayoutManager(this,spanCount));
        mRecipeRv.setAdapter(mAdapter);

        mStateLay.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) { if (!mTvMsg.getText().equals(getString(R.string.data_is_loading_please_wait))) {callData();} }});
        callData();
    }

    public void showLoader(){
        mRecipeRv.setVisibility(View.GONE);
        mStateLay.setVisibility(View.VISIBLE);
        mIvError.setVisibility(View.GONE);
        mPbLoader.setVisibility(View.VISIBLE);
        mTvMsg.setText(R.string.data_is_loading_please_wait);
    }
    public void showError(){
        mRecipeRv.setVisibility(View.GONE);
        mStateLay.setVisibility(View.VISIBLE);
        mIvError.setVisibility(View.VISIBLE);
        mPbLoader.setVisibility(View.GONE);
        mTvMsg.setText(R.string.something_went_wrong_try_again);
    }

    public void showData(String s){
        List<Recipe> list= JsonUtil.parseJsonMain(s);
        if(!list.isEmpty()){
            mRecipeRv.setVisibility(View.VISIBLE);
            mStateLay.setVisibility(View.GONE);
            recipeList = list;
            mAdapter.refresh(recipeList);
        }else {showError();}
    }

    public void callData(){
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(LOADER_ID, null, this);
    }

    @Override public void onClick(int pos) {
        Recipe recipe = recipeList.get(pos);
        Intent i = new Intent(MainActivity.this, RecipeDetails.class);
        i.putExtra("name",recipe.getName());
        i.putExtra("servings",recipe.getServings());
        i.putExtra("steps",recipe.getSteps());
        i.putExtra("ingredients",recipe.getIngredients());
        startActivity(i);
    }


    @SuppressLint("StaticFieldLeak") @NonNull @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new AsyncTaskLoader<String>(this){
            String mPrevData;
            @Override protected void onStartLoading() {
                super.onStartLoading();
                if(mPrevData!=null){ deliverResult(mPrevData); }
                else {
                    showLoader();
                    forceLoad();
                }
            }
            @Override public void deliverResult(@Nullable String data) {
                super.deliverResult(data);
                mPrevData = data;
            }
            @Nullable @Override public String loadInBackground() {
                String result = null;
                URL url = NetworkUtils.buildUrl();
                try { result = NetworkUtils.getResponseFromHttpUrl(url); }
                catch (IOException e) { e.printStackTrace(); }
                return result;
            }
        };
    }

    @Override public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        if(s!=null){ showData(s); }
        else{showError();}
    }
    @Override public void onLoaderReset(@NonNull Loader<String> loader) {}
}