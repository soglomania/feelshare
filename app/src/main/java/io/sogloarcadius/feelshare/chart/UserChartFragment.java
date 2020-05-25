package io.sogloarcadius.feelshare.chart;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.sogloarcadius.feelshare.R;
import io.sogloarcadius.feelshare.main.FeelShareApplication;
import io.sogloarcadius.feelshare.model.SaveMood;

public class UserChartFragment extends Fragment implements OnChartValueSelectedListener {

    private static final String TAG = "UserChartFragment";

    private PieChart mChartUser;

    private FeelShareApplication context;

    private Integer[] moodsUID;
    private String[] moodsNames;
    private Integer[] moodsImages;

    private Typeface mTfRegular;
    private Typeface mTfLight;

    private ArrayList<PieEntry> entriesUser;
    private PieDataSet dataSetUser;
    private PieData piedataUser;

    // firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMoodsDatabaseReference;
    private ChildEventListener mChildEventListener;
    private String authenticatedUserEmail;
    private List<SaveMood> moods = new ArrayList<>();


    public UserChartFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static Fragment newInstance(String title) {
        Fragment fragment = new UserChartFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = ((FeelShareApplication) getActivity().getApplicationContext());

        moodsUID = context.getMoodsUID();
        moodsNames = context.getMoodsNames();
        moodsImages = context.getMoodsImages();

        View rootView = inflater.inflate(R.layout.fragment_charts_user_layout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

        // Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMoodsDatabaseReference = mFirebaseDatabase.getReference().child("moods");
        FirebaseUser authenticatedUser = FirebaseAuth.getInstance().getCurrentUser();
        authenticatedUserEmail = authenticatedUser.getEmail();
        if (authenticatedUserEmail == null || authenticatedUserEmail.isEmpty()){
            for (UserInfo profile : authenticatedUser.getProviderData()){
                authenticatedUserEmail = profile.getEmail();
            }
        }
        if (authenticatedUserEmail != null) {
            attachDatabaseReadListener();
        } else {
            detachDatabaseReadListener();
            moods.clear();
        }

        // PieChart User
        mTfRegular = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        mChartUser = (PieChart) view.findViewById(R.id.chartuser);
        mChartUser.setOnChartValueSelectedListener(this);
        configureUserPieChart();


    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        moods.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachDatabaseReadListener();
        moods.clear();
    }





    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    SaveMood moodAdded = dataSnapshot.getValue(SaveMood.class);
                    if (moodAdded.getUserID().equals(authenticatedUserEmail)){
                        moods.add(moodAdded);
                        setUserData();
                        mChartUser.notifyDataSetChanged(); // let the chart know it's piedataWorld changed
                        mChartUser.invalidate(); // refresh
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };

            mMoodsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMoodsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    private void configureUserPieChart() {

        SpannableString user_chart_title = new SpannableString(getActivity().getResources().getString(R.string.chart1_desc));

        // all possible touch-interactions with the chart
        mChartUser.setTouchEnabled(true);

        // rotation of the chart by touch
        mChartUser.setRotationEnabled(false);


        mChartUser.setUsePercentValues(true);
        mChartUser.getDescription().setEnabled(false);
        mChartUser.setExtraOffsets(5, 10, 5, 5);

        mChartUser.setDragDecelerationFrictionCoef(0.95f);

        mChartUser.setCenterTextTypeface(mTfLight);
        mChartUser.setCenterText(user_chart_title);

        mChartUser.setDrawHoleEnabled(true);
        mChartUser.setHoleColor(Color.WHITE);

        mChartUser.setTransparentCircleColor(Color.WHITE);
        mChartUser.setTransparentCircleAlpha(110);

        mChartUser.setHoleRadius(58f);
        mChartUser.setTransparentCircleRadius(61f);

        mChartUser.setDrawCenterText(true);

        mChartUser.setRotationAngle(0);

        mChartUser.setHighlightPerTapEnabled(true);

        // add data
        setUserData();

        // legend
        Legend l = mChartUser.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        mChartUser.getLegend().setEnabled(false);


        // entry label styling
        mChartUser.setEntryLabelColor(Color.WHITE);
        mChartUser.setEntryLabelTypeface(mTfRegular);
        mChartUser.setEntryLabelTextSize(12f);

        // labels
        mChartUser.setDrawEntryLabels(false);
        piedataUser.setDrawValues(false);

    }

    private void setUserData() {

        HashMap<Integer, Integer> counter = getUserCounter();
        entriesUser = new ArrayList<PieEntry>();

        // NOTE: The order of the entriesWorld when being added to the entriesWorld array determines their position around the center of
        // the chart.
        for (int i = 0; i < moodsUID.length; i++) {
            entriesUser.add(new PieEntry((float) (counter.get(moodsUID[i])),
                    moodsNames[moodsUID[i]]
            ));
        }

        dataSetUser = new PieDataSet(entriesUser, getString(R.string.moods));

        dataSetUser.setDrawIcons(false);

        dataSetUser.setSliceSpace(3f);
        dataSetUser.setIconsOffset(new MPPointF(0, 40));
        dataSetUser.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSetUser.setColors(colors);
        //dataSetWorld.setSelectionShift(0f);

        piedataUser = new PieData(dataSetUser);
        piedataUser.setValueFormatter(new PercentFormatter());
        // remove percentage in piechart
        piedataUser.setValueTextSize(0);

        piedataUser.setValueTextColor(Color.WHITE);
        piedataUser.setValueTypeface(mTfLight);
        mChartUser.setData(piedataUser);

        // undo all highlights
        mChartUser.highlightValues(null);
        mChartUser.invalidate();
    }






    private HashMap<Integer, Integer> getUserCounter() {

        HashMap<Integer, Integer> counter = new HashMap<Integer, Integer>();
        //init values to 0
        for (int i = 0; i < moodsUID.length; i++) {
            counter.put(moodsUID[i], 0);
        }
        if (authenticatedUserEmail != null) {
            for (SaveMood saveMood : moods) {
                int inc_value = counter.get(saveMood.getMoodUID()) + 1;
                counter.put(saveMood.getMoodUID(), inc_value);
            }
            Log.v(TAG, counter.toString());

        }

        return counter;

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        Log.v(TAG, "I Clicked on " + moodsNames[(int)h.getX()] + " : " + h.getY());

        mChartUser.setCenterText(moodsNames[(int)h.getX()]);


        LinearLayout toastView = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        toastView.setLayoutParams(layoutParams);
        toastView.setGravity(Gravity.CENTER);
        toastView.setOrientation(LinearLayout.VERTICAL);


        ImageView toastImage = new ImageView(getContext());
        toastImage.setImageResource(moodsImages[(int)h.getX()]);

        TextView toastText = new TextView(getContext());
        toastText.setText(moodsNames[(int)h.getX()]);

        toastView.addView(toastImage, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        toastView.addView(toastText, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.show();

    }

    @Override
    public void onNothingSelected() {
        SpannableString user_chart_title = new SpannableString(getActivity().getResources().getString(R.string.chart1_desc));
        mChartUser.setCenterText(user_chart_title);

    }
}