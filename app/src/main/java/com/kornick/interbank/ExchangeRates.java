package com.kornick.interbank;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ExchangeRates {

    int id;// - id точки,
    Date pointDate;// - дата обновления точки,
    Date pdate;// - дата,
    float bid = 0;// - продажа,
    float ask = 0;// - покупка,
    float nbu = 0;// - покупка,
    float trendbid = 0;// - продажа,
    float trendask = 0;// - покупка,
    float nbuTrend = 0;// - продажа,
    String currency = "";// - валюта,
    String title = "";// Необходим для выводи заголовка

    ExchangeRates(String title){
        this.title = title;
    }

    ExchangeRates(JSONObject jsonObject){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getInt("id");
            }
            if (jsonObject.has("pointDate")) {
                this.pointDate = (Date) format.parse(jsonObject.getString("pointDate"));
            }
            this.pdate = (Date) format.parse(jsonObject.getString("date"));
            this.bid = (float) jsonObject.getDouble("bid");
            this.ask = (float) jsonObject.getDouble("ask");
            this.trendbid = (float) jsonObject.getDouble("trendBid");
            this.trendask = (float) jsonObject.getDouble("trendAsk");
            this.currency = jsonObject.getString("currency");
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.toString());
        } catch (ParseException e) {
            Log.d(MainActivity.TAG, e.toString());
        }
    }

    ExchangeRates(String currency, JSONObject jsonObject1, Boolean nbu){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            JSONObject jsonObject = jsonObject1.getJSONObject(currency);
            this.currency = currency;
            this.pdate = (Date) format.parse(jsonObject.getString("date"));
            if (nbu) {
                this.nbu = (float) jsonObject.getDouble("bid");
                this.nbuTrend = (float) jsonObject.getDouble("trendBid");
            }
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.toString());
        } catch (ParseException e) {
            Log.d(MainActivity.TAG, e.toString());
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
