package com.example.planer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.planer.currecnyconverter.CurrencyConverter;
import com.example.planer.data.CountryDriver;

import java.util.ArrayList;

public class CurrencyConverterActivity extends AppCompatActivity {

    // TODO
    CurrencyConverter cC;

    /**
     * @param { String } home
     * @param { String } destination
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        // Okay, so instead of having a list of countries I need to
        // get the home and destination country from intent extras

        Intent intent = getIntent();

        // Create new currency converter and pass in home and destination
        this.cC = new CurrencyConverter(
                intent.getStringExtra("home"),
                intent.getStringExtra("destination")
        );

        // Get home text view
        // Get destination text view
        TextView homeCountry = findViewById(R.id.cc_home_country);
        TextView destinationCountry = findViewById(R.id.cc_destination_country);

        // Add home country to textView
        // Add destination country to destination textView
        homeCountry.setText(cC.getHome());
        destinationCountry.setText(cC.getDestination());

        // Update cC rates
        TextView rate = findViewById(R.id.conversion_rate);
        cC.updateRate(getApplicationContext(), () -> {
            rate.setText("" + cC.getRate());
        });

        // I scoured the internet for a sleek way to set on click listeners
        // I tried putting it in the XML but I couldn't find a attribute : (
        // I like how simple the code is. I didn't want to change the aesthetic.
        setUpGrossLookingOnClickListeners();
    }

    interface SetRateCallBack {

    }


    /**
     * Home to Destination
     */
    public void convertCurrencyHD(View view) {
        EditText home = findViewById(R.id.currency_input);
        EditText destination = findViewById(R.id.currency_output);
        double homeValue = Double.parseDouble(home.getText().toString());
        destination.setText(new Double(cC.convertHD(homeValue)).toString());
    }


    /**
     * Destination to Home
     */
    public void convertCurrencyDH(View view) {
        EditText home = findViewById(R.id.currency_input);
        EditText destination = findViewById(R.id.currency_output);
        double destinationValue = Double.parseDouble(destination.getText().toString());
        destination.setText(new Double(cC.convertDH(destinationValue)).toString());
    }

    // Ew stinky listeners. This belongs at the bottom of the file.
    // Nasty
    private void setUpGrossLookingOnClickListeners() {
        EditText home = findViewById(R.id.currency_input);
        EditText destination = findViewById(R.id.currency_output);

        // So weird. I don't like
        home.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (!s.toString().equals("")) {
                    destination.setText(new Double(cC.convertHD(Double.valueOf(s.toString()))).toString());
                }
            }
        });
    }
}

