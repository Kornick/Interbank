package com.kornick.interbank;

import android.app.Application;

import java.util.ArrayList;
//extends Fragment не сработало на 4.0.4
public class InterBank extends Application {
    private ArrayList<ExchangeRates> arrayList;
    private ArrayList<ExchangeRates> interList;
    private int mYear;
    private int mMonth;
    private int mDay;
    private String graphCurrency;

    public ArrayList<ExchangeRates> getExchangeRates(){
        return  arrayList;
    }

    public  void setExchangeRates(ArrayList<ExchangeRates> arrayList){
        this.arrayList = arrayList;
    }

    public void getDate(int mYear, int mMonth, int mDay) {
        mYear = this.mYear;
        mMonth = this.mMonth;
        mDay = this.mDay;
    }

    public void setDate(int mYear, int mMonth, int mDay) {
        this.mYear = mYear;
        this.mMonth = mMonth;
        this.mDay = mDay;
    }

    public String getGraphCurrency() {
        return this.graphCurrency;
    }

    public void setGraphCurrency(String graphCurrency) {
        this.graphCurrency = graphCurrency;
    }

    public ArrayList<ExchangeRates> getInterList() {
        return this.interList;
    }

    public void setInterList(ArrayList<ExchangeRates> interList) {
        this.interList = interList;
    }
}
