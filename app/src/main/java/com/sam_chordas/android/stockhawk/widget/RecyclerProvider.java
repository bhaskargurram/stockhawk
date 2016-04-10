package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by bhaskar on 3/4/16.
 */
public class RecyclerProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private int appWidgetId;
    private static final int CURSOR_LOADER_ID = 0;
    Cursor cursor;
    int size;


    public RecyclerProvider(Context context, Intent intent, Cursor cursor, int size) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        this.cursor = cursor;
        this.size = size;
        Log.d("", "inside Recycler Provider");
        cursor.moveToFirst();
        //Toast.makeText(context,"inside",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.d("bhaskar", "inside onDataSetChanged");
        cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI
                , new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        cursor.moveToFirst();
        size = cursor.getCount();
        Log.d("bhaskar", "cursor size=" + cursor.getCount());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public RemoteViews getViewAt(int position) {
        //Log.d("", "inside " + cursor.getString(cursor.getColumnIndex("symbol")) + " " + cursor.getCount());

        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_row);

        cursor.moveToPosition(position);
        Log.i("", "inside " + position);
        String stock_symbol = cursor.getString(cursor.getColumnIndex("symbol"));
        remoteView.setTextViewText(R.id.stock_symbol, stock_symbol);
        remoteView.setTextViewText(R.id.bid_price, cursor.getString(cursor.getColumnIndex("bid_price")));
        if (Utils.showPercent) {
            remoteView.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            remoteView.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("change")));
        }

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);


        } else {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }


        Intent fillIntent = new Intent();
        fillIntent.putExtra("symbol", stock_symbol);
        remoteView.setOnClickFillInIntent(R.id.widget_row, fillIntent);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
