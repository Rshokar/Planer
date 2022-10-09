package com.example.planer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.planer.favourite.FavouriteCountriesAdapter;
import com.example.planer.favourite.FavouriteCountry;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment {
    ArrayList<FavouriteCountry> favouriteCountries = new ArrayList<>();

    public FavouriteFragment() {
    }

    public FavouriteFragment(ArrayList<String> countries) {
        for (String country : countries) {
            favouriteCountries.add(new FavouriteCountry(country));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setAdapter(new FavouriteCountriesAdapter(favouriteCountries));
        return view;
    }
}