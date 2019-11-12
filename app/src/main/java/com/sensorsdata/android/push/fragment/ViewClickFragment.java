package com.sensorsdata.android.push.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.sensorsdata.android.push.R;


public class ViewClickFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, RatingBar.OnRatingBarChangeListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    public ViewClickFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_view_click, container, false);
        view.findViewById(R.id.tv_click).setOnClickListener(this);
        view.findViewById(R.id.btn_click).setOnClickListener(this);
        view.findViewById(R.id.imageButton_click).setOnClickListener(this);
        view.findViewById(R.id.cb_click).setOnClickListener(this);
        view.findViewById(R.id.rb_click).setOnClickListener(this);
        ((Switch)view.findViewById(R.id.switch_click)).setOnCheckedChangeListener(this);
        ((ToggleButton)view.findViewById(R.id.toggle_click)).setOnCheckedChangeListener(this);
        view.findViewById(R.id.ctv_click).setOnClickListener(this);
        view.findViewById(R.id.iv_click).setOnClickListener(this);
        ((Spinner)view.findViewById(R.id.spinner)).setOnItemSelectedListener(this);
        view.findViewById(R.id.seekbar_click).setOnClickListener(this);
        ((SeekBar)view.findViewById(R.id.seekbar_click)).setOnSeekBarChangeListener(this);
        ((RatingBar)view.findViewById(R.id.ratingbar_click)).setOnRatingBarChangeListener(this);
        ((RadioGroup)view.findViewById(R.id.rg_click)).setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
