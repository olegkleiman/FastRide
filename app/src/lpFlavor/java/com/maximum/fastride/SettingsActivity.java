package com.maximum.fastride;

import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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

                final String currentCarNumber =  mCarsAdapter.getItem(position);

                MaterialDialog dialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title(R.string.edit_car_dialog_caption)
                        .customView(R.layout.dialog_add_car, true)
                        .positiveText(R.string.edit_car_button_save)
                        .negativeText(R.string.add_car_button_cancel)
                        .neutralText(R.string.edit_car_button_delete)
                        .autoDismiss(false)
                        .cancelable(true)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {

                                String carNumber = mCarInput.getText().toString();
                                if( carNumber.length() < 7 ) {
                                    mCarInput.setError(getString(R.string.car_number_validation_error));
                                    return;
                                }

                                if( !carNumber.equals(currentCarNumber) ) {

                                    mCars.remove(currentCarNumber);
                                    mCars.add(carNumber);

                                    // Adapter's items will be updated since underlaying list changes
                                    mCarsAdapter.notifyDataSetChanged();

                                    saveCars();
                                }

                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }


                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                String carNumber = mCarInput.getText().toString();
                                mCarsAdapter.remove(carNumber);
                                mCarsAdapter.notifyDataSetChanged();

                                saveCars();
                                dialog.dismiss();
                            }
                        })
                        .build();
                mCarInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNumber);

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

                        saveCars();
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

    private void saveCars() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Set<String> carsSet = new HashSet<String>();
        for (String _s : mCars) {
            carsSet.add(_s);
        }
        editor.putStringSet(Globals.CARS_PREF, carsSet);
        editor.apply();
    }
}


