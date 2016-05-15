package com.kornick.interbank;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String TAG = "myLog";
    ListView listView;
    ArrayList<ExchangeRates> arrayList;
    ArrayList<ExchangeRates> interList;
    ExchangeAdapter adapterE;
    String graphCurrency = "";

    Button btnUSD;
    Button btnRUB;
    Button btnEUR;


    InterBank app;

    Calendar curDate;
    private static int mYear;
    private static int mMonth;
    private static int mDay;

    Handler exchangeHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj == null)
                return false;

            ArrayList<ExchangeRates> array = (ArrayList<ExchangeRates>) msg.obj;

            Collections.sort(array, new Comparator<ExchangeRates>() {
                @Override
                public int compare(ExchangeRates er1, ExchangeRates er2) {
                    int result = er2.currency.compareTo(er1.currency);//reverse compare
                    if (result == 0) {
                        result = er2.pointDate.compareTo(er1.pointDate);//reverse compare otherwise er1 compareto er2
                    }
                    return result;
                }
            });

            if (msg.arg1 == 0) {
                interList.clear();
            }
            //Оставляем только первую строку валюты
            String currency = "";
            int i = 0;
            while (i < array.size()) {
                ExchangeRates er1 = array.get(i);
                if (er1.currency.equals(currency)) {
                    ++i;
                    if (msg.arg1 == 0) {
                        interList.add(er1);
                    }
                    continue;
                }
                currency = er1.currency;

                if (msg.arg1 == 0){
                    interList.add(er1);
                }

                //Второй вызов по вчерашней дате. Находим сегодняшнюю дату и смотрим разницу
                if (msg.arg1 == 1) {
                    int i2 = 0;
                    while (i2 < arrayList.size()) {
                        ExchangeRates er2 = arrayList.get(i2);
                        if (er2.currency.equals(currency)) {
                            er2.trendask = er2.ask - er1.ask;
                            er2.trendbid = er2.bid - er1.bid;
                            break;
                        }
                        ++i2;
                    }
                } else {
                    arrayList.add(er1);//Добавляем только первую строку с уникальной валютой
                }
                ++i;
            }

            GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dataUrl;

            switch (msg.arg1){
                case 2:
                    adapterE = new ExchangeAdapter(MainActivity.this, arrayList);
                    adapterE.rollUp();
                    listView.setAdapter(adapterE);
                    format = new SimpleDateFormat("dd.MM.yyyy");
                    TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
                    txtTitle.setText(format.format(cal.getTime()));

                    Collections.sort(interList, new Comparator<ExchangeRates>() {
                        @Override
                        public int compare(ExchangeRates er1, ExchangeRates er2) {
                            int result = er1.pointDate.compareTo(er2.pointDate);
                            return result;
                        }
                    });

                    setGraph(graphCurrency);
                    break;
                case 1:
                    dataUrl = "http://api.minfin.com.ua/nbu/19152bb8f814183b62a4653fefbeedd1e34ff05d/";
                    if (!format.format(curDate.getTime()).equals(format.format(cal.getTime()))) {
                        dataUrl += format.format(cal.getTime());
                    }
                    httpRequestObject(dataUrl, 2);
                    break;
                case 0:
                    cal.add(Calendar.DATE, -1);
                    dataUrl = "http://api.minfin.com.ua/mb/19152bb8f814183b62a4653fefbeedd1e34ff05d/";
                    dataUrl += format.format(cal.getTime());
                    httpRequestArray(dataUrl, 1);//вчерашняя дата
                    break;
            }

                return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the application instance
        app = (InterBank)getApplication();
        curDate = Calendar.getInstance();

        listView = (ListView) findViewById(R.id.listView);

        View headView = getLayoutInflater().inflate(R.layout.head, null);
        View footView = getLayoutInflater().inflate(R.layout.grath, null);
        listView.addHeaderView(headView);
        listView.addFooterView(footView);

        btnUSD = (Button) findViewById(R.id.btnUSD);
        btnRUB = (Button) findViewById(R.id.btnRUB);
        btnEUR = (Button) findViewById(R.id.btnEUR);
        btnUSD.setOnClickListener(this);
        btnRUB.setOnClickListener(this);
        btnEUR.setOnClickListener(this);

        interList = new ArrayList<ExchangeRates>();

        if (app.getExchangeRates() != null) {
            arrayList = app.getExchangeRates();
            adapterE = new ExchangeAdapter(MainActivity.this, arrayList);
            listView.setAdapter(adapterE);

            app.getDate(mYear, mMonth, mDay);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay);
            TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
            txtTitle.setText(format.format(cal.getTime()));

            interList = app.getInterList();
            graphCurrency = app.getGraphCurrency();
            setGraph(graphCurrency);

        }
        else {
            arrayList = new ArrayList<ExchangeRates>();
            // get the current date
            mYear = curDate.get(Calendar.YEAR);
            mMonth = curDate.get(Calendar.MONTH);
            mDay = curDate.get(Calendar.DAY_OF_MONTH);

            refresh();
        }

    }

    @Override
    protected void onPause() {
        app.setExchangeRates(arrayList);
        app.setInterList(interList);
        app.setDate(mYear, mMonth, mDay);
        app.setGraphCurrency(graphCurrency);
        super.onPause();
    }

    public void setGraph(String currency){
        btnUSD.setSelected(false);
        btnRUB.setSelected(false);
        btnEUR.setSelected(false);
        switch (currency){
            case "usd":
                btnUSD.setSelected(true);
                graphCurrency = "usd";
                break;
            case "rub":
                btnRUB.setSelected(true);
                graphCurrency = "rub";
                break;
            case "eur":
                btnEUR.setSelected(true);
                graphCurrency = "eur";
                break;
        }
        
        int i = 0;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        ArrayList<String> time = new ArrayList<String>();

        ArrayList<ExchangeRates> grathList = new ArrayList<ExchangeRates>();
        while (i < interList.size()) {
            ExchangeRates er1 = interList.get(i);
            if (currency.equalsIgnoreCase(er1.currency)){
                grathList.add(er1);
                time.add(format.format(er1.pointDate));
            }
            ++i;
        }
        DataPoint[] dataPointBid = new DataPoint[grathList.size()];
        DataPoint[] dataPointAsk = new DataPoint[grathList.size()];
        i = 0;
        while (i < grathList.size()) {
            ExchangeRates er1 = grathList.get(i);
            dataPointBid[i] = new DataPoint(i, er1.bid);
            dataPointAsk[i] = new DataPoint(i, er1.ask);
            ++i;
        }
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPointBid);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(4);
        series.setColor(Color.BLUE);
        graph.addSeries(series);

        series = new LineGraphSeries<DataPoint>(dataPointAsk);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(4);
        series.setColor(Color.RED);
        graph.addSeries(series);

        if (time.size() == 0) return;

        // use static labels for horizontal and vertical labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        if (time.size() == 2) {
            staticLabelsFormatter.setHorizontalLabels(new String[]{time.get(0), time.get(time.size()-1)});
        } else if (time.size() > 2) {
            staticLabelsFormatter.setHorizontalLabels(new String[]{time.get(0), time.get(time.size()/2), time.get(time.size() - 1)});
        } else {
            staticLabelsFormatter.setHorizontalLabels(new String[]{"10:00"});
        }
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getViewport().setXAxisBoundsManual(true);
    }

    public void refresh() {
        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        arrayList.clear();
        if (graphCurrency.isEmpty()) {
            graphCurrency = "usd";
        }

        String dataUrl = "http://api.minfin.com.ua/mb/19152bb8f814183b62a4653fefbeedd1e34ff05d/";
        GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (!format.format(curDate.getTime()).equals(format.format(cal.getTime()))) {
            dataUrl += format.format(cal.getTime());
        }
        httpRequestArray(dataUrl, 0);
    }

    private void httpRequestArray(String dataUrl, final int arg1){
        // url is the URL to download.
        AsyncHttpClient.getDefaultInstance().executeJSONArray(new AsyncHttpRequest(Uri.parse(dataUrl), "GET"), new AsyncHttpClient.JSONArrayCallback() {
            // Callback is invoked with any exceptions/errors, and the result, if available.
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse response, JSONArray result) {
                ArrayList<ExchangeRates> array = new ArrayList<ExchangeRates>();
                if (e != null) {
                    Log.d(TAG, e.toString());
                    return;
                } else {
                    Log.d(TAG, "I got a JSONArray: " + result);
                    for (int i = 0; i < result.length(); ++i) {
                        try {
                            array.add(new ExchangeRates(result.getJSONObject(i)));
                        } catch (JSONException e1) {
                            Log.d(TAG, e1.toString());
                        }
                    }
                }

                    Message msg = Message.obtain();
                    msg.obj = array;
                    msg.arg1 = arg1;
                    exchangeHandler.sendMessage(msg);

            }

            @Override
            public void onConnect(AsyncHttpResponse response) {
                super.onConnect(response);
            }

            @Override
            public void onProgress(AsyncHttpResponse response, long downloaded, long total) {
                super.onProgress(response, downloaded, total);
            }
        });
    }

    private void httpRequestObject(String dataUrl, final int arg1){
        // url is the URL to download.
        AsyncHttpClient.getDefaultInstance().executeJSONObject(new AsyncHttpRequest(Uri.parse(dataUrl), "GET"), new AsyncHttpClient.JSONObjectCallback() {
            // Callback is invoked with any exceptions/errors, and the result, if available.
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse response, JSONObject result) {
                ArrayList<ExchangeRates> array = new ArrayList<ExchangeRates>();
                if (e != null) {
                    Log.d(TAG, e.toString());
                    //return;
                } else {
                    Log.d(TAG, "I got a JSONObject: " + result);
                    for (int i = 0; i < result.names().length(); i++) {
                        try {
                            array.add(new ExchangeRates(result.names().getString(i), result, true));
                        } catch (JSONException e1) {
                            Log.d(TAG, e1.toString());
                        }
                    }
                }

                    Message msg = Message.obtain();
                    msg.obj = array;
                    msg.arg1 = arg1;
                    exchangeHandler.sendMessage(msg);

            }
        });
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        MenuItem dateMenu = menu.findItem(R.id.action_date);
        dateMenu.setTitle(format.format(cal.getTime()));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);

        GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        MenuItem dateMenu = menu.findItem(R.id.action_date);
        dateMenu.setTitle(format.format(cal.getTime()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_date:
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
                break;
            case R.id.action_refresh:
                Log.d(TAG,"Refresh");
                refresh();
                //Toast.makeText(MainActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_web:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.minfin.com.ua/currency/"));
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUSD:
                setGraph("usd");
                break;
            case R.id.btnRUB:
                setGraph("rub");
                break;
            case R.id.btnEUR:
                setGraph("eur");
                break;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

       @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mYear = year; mMonth = month; mDay = day;
            ((MainActivity) getActivity()).refresh();
        }
    }
}
