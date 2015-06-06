package com.maximum.fastride;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.R;
import com.maximum.fastride.adapters.CarsAdapter;
import com.maximum.fastride.utils.FloatingActionButton;
import com.maximum.fastride.utils.Globals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends BaseActivity {

    List<String> mCars;
    CarsAdapter mCarsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupUI(getString(R.string.title_activity_settings), "");

    }

    @Override
    protected void setupUI(String title, String subTitle) {
        super.setupUI(title, subTitle);

        mCars = new ArrayList<>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> carsSet = sharedPrefs.getStringSet(Globals.CARS_PREF, new HashSet<String>());
        if( carsSet != null ) {
            Iterator<String> iterator = carsSet.iterator();
            while (iterator.hasNext()) {
                String car = iterator.next();
                mCars.add(car);
            }
        }

        ListView listView = (ListView)findViewById(R.id.carsListView);
        mCarsAdapter = new CarsAdapter(this, R.layout.car_item_row, mCars);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    final View view,
                                    int position, long id) {

                MaterialDialog dialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title(R.string.edit_car_dialog_caption)
                        .customView(R.layout.dialog_add_car, true)
                        .positiveText(R.string.edit_car_button_save)
                        .negativeText(R.string.add_car_button_cancel)
                        .neutralText(R.string.edit_car_button_delete)
                        .autoDismiss(true)
                        .cancelable(true)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                String carNumber = mCarInput.getText().toString();
                                mCars.add(carNumber);
                                mCarsAdapter.add(carNumber);
                                mCarsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                String carNumber = mCarInput.getText().toString();
                                mCarsAdapter.remove(carNumber);
                                mCarsAdapter.notifyDataSetChanged();
                            }
                        })
                        .build();
                mCarInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNumber);
                String currentCarNumber =  mCarsAdapter.getItem(position);
                mCarInput.setText(currentCarNumber);

                dialog.show();

            }
        });
        listView.setAdapter(mCarsAdapter);

        View addButton = findViewById(R.id.add_car_button);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FloatingActionButton fab = (FloatingActionButton)addButton;
            fab.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_add));
            fab.setBackgroundColor(getResources().getColor(R.color.ColorAccent));
        } else {
            addButton.setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    int diameter = getResources().getDimensionPixelSize(R.dimen.diameter);
                    outline.setOval(0, 0, diameter, diameter);
                }
            });
            addButton.setClipToOutline(true);
        }

    }

    private EditText mCarInput;

    public void onAddCar(View view){

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.add_car_dialog_caption)
                .customView(R.layout.dialog_add_car, true)
                .positiveText(R.string.add_car_button_add)
                .negativeText(R.string.add_car_button_cancel)
                .autoDismiss(true)
                .cancelable(true)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        String carNumber = mCarInput.getText().toString();
                        mCarsAdapter.add(carNumber);
                        mCarsAdapter.notifyDataSetChanged();

                        Toast.makeText(SettingsActivity.this,
                                "Car number: " + mCarInput.getText().toString(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .build();

                final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
                mCarInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNumber);
                mCarInput.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void beforeTextChanged (CharSequence s,int start, int count, int after){

                        }

                        @Override
                        public void onTextChanged (CharSequence s,int start, int before, int count){
                            positiveAction.setEnabled(s.toString().trim().length() > 0);
                        }

                        @Override
                        public void afterTextChanged (Editable s){

                        }
                    });

                    dialog.show();
                    positiveAction.setEnabled(false); // disabled by default
                }

    }


