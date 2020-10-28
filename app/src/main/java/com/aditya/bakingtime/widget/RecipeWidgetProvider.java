package com.aditya.bakingtime.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.aditya.bakingtime.R;

public class RecipeWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String ingredients) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget_provider);

        /*Intent intent  = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);*/

        if(ingredients!=null && !ingredients.isEmpty()){
            Intent i = new Intent(context, RecipeWidgetService.class);
            i.putExtra("ingredients",ingredients);
            views.setRemoteAdapter(R.id.lv_ingredients, i);
            views.setViewVisibility(R.id.lv_ingredients, View.VISIBLE);
            views.setViewVisibility(R.id.appwidget_text, View.GONE);
        }
        else {
            views.setViewVisibility(R.id.lv_ingredients, View.GONE);
            views.setViewVisibility(R.id.appwidget_text, View.VISIBLE);
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.lv_ingredients);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidget(Context ctx,AppWidgetManager appWidgetManager, String s, int[] appWidgetsId ){
        for (int i :appWidgetsId){
            RecipeWidgetProvider.updateAppWidget(ctx,appWidgetManager,i,s);
        }
    }

    @Override public void onEnabled(Context context) {
        super.onEnabled(context);
    }
    @Override public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}