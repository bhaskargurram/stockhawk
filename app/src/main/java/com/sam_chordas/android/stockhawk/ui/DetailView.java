package com.sam_chordas.android.stockhawk.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.hogel.android.linechartview.LineChartView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailView extends AppCompatActivity {
    private static final double MAX_Y = 1000;
    private LineChartView chartView;

    List<LineChartView.Point> points;
    String s;
    private final OkHttpClient client = new OkHttpClient();
    Request request;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String symbol = getIntent().getStringExtra("symbol");
        Log.d("bhaskar", symbol);
        setContentView(R.layout.activity_line_graph);

        chartView = (LineChartView) findViewById(R.id.chart_view);
        chartView.setManualMinY(0);
        Calendar c = Calendar.getInstance();


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String end_date = df.format(c.getTime());
        Log.d("bhaskar", "current date=" + end_date);
        c.add(Calendar.DATE, -7);
        String start_date = df.format(c.getTime());
        Log.d("bhaskar", "start date=" + start_date);

        String url = "http://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.historicaldata where symbol=\"" + symbol + "\" and startDate=\"" + start_date + "\" and endDate=\"" + end_date + "\" &diagnostics=true&env=store://datatables.org/alltableswithkeys&format=json";

        request = new Request.Builder()
                .url(url)
                .build();
        plot("Close");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.open:
                plot("Open");
                break;
            case R.id.close:
                plot("Close");
                break;
            case R.id.high:
                plot("High");
                break;
            case R.id.low:
                plot("Low");
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    void plot(String type) {
        final String typ = type;
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String res = response.body().string();
                Log.d("bhaskar", typ);
                parse(res, typ);
            }
        });

    }

    void parse(String result, String type) {
        Log.d("bhaskar", "type=" + type);
        points = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray array = obj.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
            final long length = array.length();
            final List<String> dates = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                JSONObject iter = array.getJSONObject(i);
                String date = iter.getString("Date");
                dates.add(date);
                long value = (long) iter.getDouble(type);
                long x_val = (long) (array.length() - i - 1);

                LineChartView.Point point = new LineChartView.Point(x_val, value);
                Log.d("bhaskar", "date=" + date + "x_val=" + x_val + "value=" + value);
                points.add(point);

            }
            final List<String> dates_dup = dates;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    chartView.setManualMinX(0);
                    chartView.setManualMaxX(length - 1);
                    chartView.setPoints(points);
                    chartView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(DetailView.this);
                            builderSingle.setTitle(R.string.builder_title);

                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    DetailView.this,
                                    android.R.layout.simple_list_item_1);
                            for (int i = 0; i < points.size(); i++) {
                                LineChartView.Point point = points.get(i);

                                String display = point.getX() + "=" + dates.get(i) + " value=" + point.getY();
                                Log.d("bhaskar", getApplicationContext().getResources().getConfiguration().locale.getDisplayName());
                                arrayAdapter.add(display);

                            }
                            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builderSingle.setNegativeButton(
                                    getResources().getString(R.string.okay),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builderSingle.show();

                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("bhaskar", points.toString());
    }


}