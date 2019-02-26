package com.ses.zebra.pssdemo_2019.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ses.zebra.pssdemo_2019.R;

public class NoMapFragment extends Fragment {

    public NoMapFragment() {
        // Required empty public constructor
    }

    public static NoMapFragment newInstance() {
        return new NoMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_map, container, false);
    }

}
