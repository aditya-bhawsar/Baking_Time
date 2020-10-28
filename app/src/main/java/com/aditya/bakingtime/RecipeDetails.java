package com.aditya.bakingtime;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.aditya.bakingtime.model.RecipeIngredient;
import com.aditya.bakingtime.model.RecipeStep;
import com.aditya.bakingtime.util.JsonUtil;
import com.aditya.bakingtime.adapters.RecipeStepAdapter;
import com.aditya.bakingtime.widget.RecipeWidgetProvider;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetails extends AppCompatActivity implements RecipeStepAdapter.ClickListener {

    @BindView(R.id.rv_steps) RecyclerView rvSteps;
    @BindView(R.id.tv_ingredients) TextView ingredientsTv;
    @BindView(R.id.tv_servings_indicator) TextView servingsTv;

    boolean mTwoPane;
    List<RecipeStep> mStepList = new ArrayList<>();
    List<RecipeIngredient> mIngredientList= new ArrayList<>();
    String mStepData;

    RecipeStepAdapter recipeStepAdapter;
    FragmentManager fm;
    SimpleExoPlayer mExoPlayer;
    int currentPosition = -1;
    StepDescriptionFragment stepDescriptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        ButterKnife.bind(this);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setSteps(getIntent().getStringExtra("steps"));
        setIngredients(getIntent().getStringExtra("ingredients"));

        int servings = getIntent().getIntExtra("servings",0);

        servingsTv.setText(getString(R.string.servings,servings));

        for(RecipeIngredient ingredient:mIngredientList){
            String ing = ingredient.getIngredient() + " (" +
                    ingredient.getQuantity() + " " +
                    ingredient.getMeasure() + ")";

            if(ingredientsTv.getText()==null || ingredientsTv.getText().equals("")){ ingredientsTv.setText(ing); }
            else{ ingredientsTv.setText(getString(R.string.ingredients_concat,ingredientsTv.getText(),ing)); }
        }
        if(!ingredientsTv.getText().toString().isEmpty() && ingredientsTv.getText()!=null){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetsId = appWidgetManager.getAppWidgetIds(new ComponentName(this, RecipeWidgetProvider.class));
            RecipeWidgetProvider.updateAllWidget(this,appWidgetManager,getIntent().getStringExtra("ingredients"),appWidgetsId);
        }

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        rvSteps.setLayoutManager(linearLayoutManager);

        mTwoPane= findViewById(R.id.two_pane_lay) != null;

        recipeStepAdapter = new RecipeStepAdapter(mStepList, this, mTwoPane);
        rvSteps.setAdapter(recipeStepAdapter);

        if(mTwoPane){
            fm = getSupportFragmentManager();
            stepDescriptionFragment = new StepDescriptionFragment();
            stepDescriptionFragment.setSteps(mStepData);
            fm.beginTransaction().replace(R.id.step_desc_lay, stepDescriptionFragment).commit();
        }
    }

    @Override
    public void onClick(int pos) {
        currentPosition = pos;
        if(mTwoPane){
            stepDescriptionFragment.setPositionFromRv(pos);
        }
        else {
            Intent i = new Intent(RecipeDetails.this, StepDescription.class);
            i.putExtra("name",getIntent().getStringExtra("name"));
            i.putExtra("steps",mStepData);
            i.putExtra("position",currentPosition);
            startActivity(i);
        }

    }

    public void setSteps(String s){
        mStepData = s;
        mStepList.clear();
        mStepList = JsonUtil.parseJsonSteps(s);
    }

    public void setIngredients(String s){
        mIngredientList.clear();
        mIngredientList= JsonUtil.parseJsonIngredients(s);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPlayer(SimpleExoPlayer simpleExoPlayer){
        mExoPlayer = simpleExoPlayer;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mTwoPane && mExoPlayer!=null){
            mExoPlayer.stop();
            mExoPlayer.release();
        }
    }
}