package com.example.planer;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planer.currecnyconverter.CurrencyConverter;
import com.example.planer.favourite.FavouriteCountriesAdapter;
import com.example.planer.favourite.FavouriteCountry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.planer.ranking.CovidRestriction;
import com.example.planer.ranking.RecommendationLevel;
import com.example.planer.ranking.Visa;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Map;

public class SearchFragment extends Fragment {
    private FirebaseFirestore db;
    private String homeCountry;
    private String countrySelected;

    private CardView scoreCard;
    private TextView score;
    private TextView airport;
    private TextView visaInfoText;
    private TextView advisory;
    private ImageView riskLevelIcon;
    private TextView rate;

    private String visaContent;
    private String advisoryContent;

    DecimalFormat df = new DecimalFormat("#.#");
    private TextView weatherCity;
    private TextView conditions;
    private ImageView conditionsIcon;
    private TextView temperature;

    // Currency Converter
    private CurrencyConverter cC = new CurrencyConverter();

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            db = FirebaseFirestore.getInstance();
            homeCountry = getArguments().getString("home");
            countrySelected = getArguments().getString("country");
            System.out.println(countrySelected);

            MyAsyncTask task = new MyAsyncTask();
            task.execute();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // OnClickListeners to switch to new activities when clicking cards (Visa, Covid, Weather)
        CardView visa = requireView().findViewById(R.id.visaCard);
        visa.setOnClickListener(this::gotoVisa);

        CardView travelRestrictions = requireView().findViewById(R.id.travelRestrictionsCard);
        travelRestrictions.setOnClickListener(this::gotoRestrictions);

        CardView weather = requireView().findViewById(R.id.weatherCard);
        weather.setOnClickListener(this::gotoWeather);

        CardView currency = requireView().findViewById(R.id.currencyCard);
        currency.setOnClickListener(this::gotoCurrency);

        score = view.findViewById(R.id.score);
        scoreCard = view.findViewById(R.id.score_card);
        airport = view.findViewById(R.id.airportInfo);
        visaInfoText = view.findViewById(R.id.visaInfo);
        advisory = view.findViewById(R.id.restrictionCovidDetail);
        riskLevelIcon = view.findViewById(R.id.imageView);
        rate = view.findViewById(R.id.currencyBlank);
        weatherCity = view.findViewById(R.id.currentWeather);
        conditions = view.findViewById(R.id.conditions);
        conditionsIcon = view.findViewById(R.id.conditionsIcon);
        temperature = view.findViewById(R.id.temp);
    }

    private void updateDataFromCountries() {
        db.collection("countries")
                .document(countrySelected)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> group = document.getData();
                        assert group != null;
                        group.forEach((key, value) -> {
                            if (key.equalsIgnoreCase("airport")) {
                                airport.setText(value.toString());
                            } else if (key.equalsIgnoreCase("advisory")) {
                                advisoryContent = value.toString();
                                advisory.setText(advisoryContent);
                                getRiskLevelImage(advisoryContent);
                            }
                        });
                        calculateScore();
                    }
                });
    }

    private void updateVisaCard() {
        db.collection("visa")
                .document(homeCountry)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> group = document.getData();
                        assert group != null;
                        group.forEach((key, value) -> {
                            if (key.equalsIgnoreCase(countrySelected)) {
                                visaContent = value.toString();
                                visaInfoText.setText(visaContent);
                            }
                        });
                    }
                });
    }

    private void updateWeather() {
        // get countrySelected, query db for capital, send capital into api call, pick apart json and update views
        db.collection("countries")
                .document(countrySelected)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        Map<String, Object> group = doc.getData();
                        assert group != null;
                        group.forEach((key, value) -> {
                            if (key.equalsIgnoreCase("capital")) {
                                String currWeather = "Current weather in " + value.toString();
                                String capital = value.toString();
                                weatherCity.setText(currWeather);

                                // Call api
                                String APPID = BuildConfig.WEATHER_KEY;
                                String URL = "http://api.openweathermap.org/data/2.5/weather";
                                String tempUrl = URL + "?q=" + capital + "&appid=" + APPID;
                                StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                        tempUrl, response -> {
                                    Log.d("response", response);
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                                        JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");

                                        String iconCode = jsonObjectWeather.getString("icon");
                                        String iconUrl = "http://openweathermap.org/img/wn/";
                                        iconUrl += iconCode + "@4x.png";
                                        Glide.with(this).load(iconUrl).into(conditionsIcon);

                                        // Get and store relevant data from your JSONObjects
                                        String description = jsonObjectWeather.getString("description");
                                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                                        String tempStr = "" + df.format(temp) + "°C";

                                        // Set views' values using data gathered above
                                        conditions.setText(description);
                                        temperature.setText(tempStr);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }, error -> Toast.makeText(getContext(),
                                        error.toString().trim(), Toast.LENGTH_SHORT).show());
                                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                                requestQueue.add(stringRequest);
                            }
                        });
                    }
                });
    }

    private void updateCurrency() {
        while (homeCountry == null || countrySelected == null) {
            // do nothing
        }
        cC.setHome(homeCountry);
        cC.setDestination(countrySelected);
        cC.updateRate(getContext(), () -> {
            rate.setText(cC.toString());
        });
    }


    public void gotoWeather(View view) {
        Intent intent = new Intent(getActivity(), WeatherActivity.class);
        startActivity(intent);
    }

    public void gotoCurrency(View view) {
        Intent intent = new Intent(getActivity(), CurrencyConverterActivity.class);
        intent.putExtra("home", homeCountry);
        intent.putExtra("destination", countrySelected);
        startActivity(intent);
    }

    public void gotoVisa(View view) {
        Intent intent = new Intent(getActivity(), VisaActivity.class);
        startActivity(intent);
    }

    public void gotoRestrictions(View view) {
        Intent intent = new Intent(getActivity(), TravelRestrictionsActivity.class);
        startActivity(intent);
    }

    private void getRiskLevelImage(String advisory) {
        if (advisory.contains("normal")) {
            riskLevelIcon.setImageResource(R.drawable.normal);
            return;
        }
        if (advisory.contains("caution")) {
            riskLevelIcon.setImageResource(R.drawable.high_caution);
            return;
        }
        if (advisory.contains("non-essential")) {
            riskLevelIcon.setImageResource(R.drawable.avoid_non_essential);
            return;
        }
        if (advisory.contains("all travel")) {
            riskLevelIcon.setImageResource(R.drawable.no_travel);
        }
    }

    private void calculateScore() {
        int totalScore = 0;
        int factors = 0;

        if (visaContent != null) {
            totalScore += Visa.findScoreByDescription(visaContent);
            factors++;
        }
        if (advisoryContent != null) {
            totalScore += CovidRestriction.findScoreByDescription(advisoryContent);
            factors++;
        }

        double scaledScore = (factors != 0) ? (double) totalScore / factors : 0;

        changeScoreCardBackground(scaledScore);

        DecimalFormat df = new DecimalFormat("###.##");
        score.setText(df.format(scaledScore));
    }

    private void changeScoreCardBackground(double score) {
        RecommendationLevel recommendation = RecommendationLevel.findRecommendationLevel(score);
        switch (recommendation) {
            case STRONGLY_RECOMMENDED:
                scoreCard.setCardBackgroundColor(Color.GREEN);
                return;
            case RECOMMENDED:
                scoreCard.setCardBackgroundColor(Color.CYAN);
                return;
            case URGENCY_ONLY:
                scoreCard.setCardBackgroundColor(Color.parseColor("#FFA500"));
                return;
            case NOT_RECOMMENDED:
                scoreCard.setCardBackgroundColor(Color.GRAY);
        }
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            updateVisaCard();
            updateDataFromCountries();
            updateWeather();
            updateCurrency();
            return null;
        }
    }
}
