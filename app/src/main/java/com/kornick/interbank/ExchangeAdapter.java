package com.kornick.interbank;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExchangeAdapter extends BaseAdapter{

    Context context;
    LayoutInflater lInflater;
    ArrayList<ExchangeRates> objects;

    public ExchangeAdapter(Context context, ArrayList<ExchangeRates> objects) {
        this.context = context;
        this.objects = objects;

        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.itemexchange, parent, false);
        }

        ExchangeRates exchangeRates = getExchangeRates(position);

        TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
        textTitle.setText(exchangeRates.title);
        TableLayout lineRow = (TableLayout) view.findViewById(R.id.lineRow);
        lineRow.setBackgroundColor(Color.WHITE);

        ImageView imgFlag = (ImageView) view.findViewById(R.id.imgFlag);
        imgFlag.setVisibility(view.INVISIBLE);

        if (!exchangeRates.title.isEmpty()) {
            lineRow.setBackgroundColor(Color.DKGRAY);
            ((TextView) view.findViewById(R.id.textCurrency)).setText("");
            ((TextView) view.findViewById(R.id.textAsk)).setText("");
            ((TextView) view.findViewById(R.id.textBid)).setText("");
            ((TextView) view.findViewById(R.id.textTrendAsk)).setText("");
            ((TextView) view.findViewById(R.id.textTrendBid)).setText("");
            ((TextView) view.findViewById(R.id.textNbu)).setText("");
            ((TextView) view.findViewById(R.id.textNbuTrend)).setText("");
            return view;
        }

        int flag = -1;
        switch (exchangeRates.currency.toLowerCase()){
            case "usd": flag = R.drawable.f_united_states; break;
            case "rub": flag = R.drawable.f_russia; break;
            case "eur": flag = R.drawable.f_european_union; break;
        }

        if (flag != -1){
            imgFlag.setVisibility(view.VISIBLE);
            imgFlag.setImageDrawable(context.getResources().getDrawable(flag));
        }
        ((TextView) view.findViewById(R.id.textCurrency)).setText(exchangeRates.currency.toUpperCase());
        ((TextView) view.findViewById(R.id.textAsk)).setText(String.format("%.4f", exchangeRates.ask));
        ((TextView) view.findViewById(R.id.textBid)).setText(String.format("%.4f", exchangeRates.bid));
        ((TextView) view.findViewById(R.id.textNbu)).setText(String.format("%.4f", exchangeRates.nbu));


        TextView textTrendAsk = (TextView) view.findViewById(R.id.textTrendAsk);
        textTrendAsk.setText(Float.compare(exchangeRates.trendask, 0) == 0 ? "" : String.format("%+.4f", exchangeRates.trendask));
        textTrendAsk.setTextColor(exchangeRates.trendask < 0 ? Color.RED : Color.GREEN);
        TextView textTrendBid = (TextView) view.findViewById(R.id.textTrendBid);
        textTrendBid.setText(Float.compare(exchangeRates.trendbid, 0) == 0 ? "" : String.format("%+.4f", exchangeRates.trendbid));
        textTrendBid.setTextColor(exchangeRates.trendbid < 0 ? Color.RED : Color.GREEN);
        TextView textNbuTrend = (TextView) view.findViewById(R.id.textNbuTrend);
        textNbuTrend.setText(Float.compare(exchangeRates.nbuTrend, 0) == 0 ? "" : String.format("%+.4f", exchangeRates.nbuTrend));
        textNbuTrend.setTextColor(exchangeRates.nbuTrend < 0 ? Color.RED : Color.GREEN);

        return view;
    }


    public void rollUp(){
        Collections.sort(objects, new Comparator<ExchangeRates>() {
            @Override
            public int compare(ExchangeRates er1, ExchangeRates er2) {
                if (!er2.title.isEmpty()) return 1;

                int result = er2.currency.compareTo(er1.currency);//reverse compare
                return result;
            }
        });

        String currency = "";
        int i = 0;
        while (i < objects.size()) {
            ExchangeRates er1 = objects.get(i);
            if (!er1.currency.isEmpty() && er1.currency.equals(currency)) {

                continue;
            }
            currency = er1.currency;
            ++i;
            int i2 = i;
            while (i2 < objects.size()) {
                ExchangeRates er2 = objects.get(i2);
                if (er2.currency.equals(currency)) {
                    er1.ask += er2.ask;
                    er1.bid += er2.bid;
                    er1.trendask += er2.trendask;
                    er1.trendbid += er2.trendbid;
                    er1.nbu += er2.nbu;
                    er1.nbuTrend += er2.nbuTrend;
                    objects.remove(i2);
                    continue;
                }
                break;
            }

        }

    }

    // товар по позиции
    ExchangeRates getExchangeRates(int position) {
        return ((ExchangeRates) getItem(position));
    }
}
