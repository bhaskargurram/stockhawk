package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.DetailView;

public class WidgetIntentService extends IntentService {

    public WidgetIntentService() {
        super("WidgetIntentSevice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("bhaskar", "inside WidgetIntentService");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget);


            final int N = appWidgetIds.length;
            for (int i = 0; i < N; ++i) {
                RemoteViews remoteViews = updateWidgetListView(this,
                        appWidgetIds[i]);
                appWidgetManager.updateAppWidget(appWidgetIds[i],
                        remoteViews);
                remoteViews.setEmptyView(R.id.listview_widget, R.id.widget_empty);
                Intent launchIntent = new Intent(this, DetailView.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                remoteViews.setPendingIntentTemplate(R.id.listview_widget, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }


        }


    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId) {


        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.widget_layout);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // svcIntent.put("cursor", cursor);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.listview_widget, svcIntent);
        //setting an empty view in case of no data
        // remoteViews.setEmptyView(R.id.recycler_view_widget, R.id.empty_view);
        return remoteViews;
    }
}
