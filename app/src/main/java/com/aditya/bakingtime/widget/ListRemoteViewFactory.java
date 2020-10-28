package com.aditya.bakingtime.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.aditya.bakingtime.R;
import com.aditya.bakingtime.model.RecipeIngredient;
import com.aditya.bakingtime.util.JsonUtil;

import java.util.List;

public class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory{

    private List<RecipeIngredient> mListIngredients;
    private Context mCtx;

    public ListRemoteViewFactory(String s, Context mCtx) {
        this.mListIngredients = JsonUtil.parseJsonIngredients(s);
        this.mCtx = mCtx;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {}

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        if(mListIngredients==null) return 0;
        else return mListIngredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RecipeIngredient ingredient = mListIngredients.get(position);
        RemoteViews remoteViews  = new RemoteViews(mCtx.getPackageName(), R.layout.widget_ingredient_view);

        String ing = ingredient.getIngredient() + " (" +
                ingredient.getQuantity() + " " +
                ingredient.getMeasure() + ")";

        remoteViews.setTextViewText(R.id.ingredient_tv, ing);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}