package com.aditya.bakingtime.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.aditya.bakingtime.widget.ListRemoteViewFactory;

public class RecipeWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory(intent.getStringExtra("ingredients"), this.getApplicationContext());
    }
}