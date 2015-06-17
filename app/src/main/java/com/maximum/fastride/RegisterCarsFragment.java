package com.maximum.fastride;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.adapters.CarsAdapter;
import com.maximum.fastride.model.RegisteredCar;
import com.maximum.fastride.utils.Globals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegisterCarsFragment extends Fragment {

    private EditText mCarInput;
    private EditText mCarNickInput;
    private CarsAdapter mCarsAdapter;
    List<RegisteredCar> mCars;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register_cars, container, false);

        try {

            mCars = new ArrayList<>();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Set<String> carsSet = sharedPrefs.getStringSet(Globals.CARS_PREF, new HashSet<String>());
            if( carsSet != null ) {
                Iterator<String> iterator = carsSet.iterator();

                while (iterator.hasNext()) {
                    String strCar= iterator.next();
                    String[] tokens = strCar.split("~");
                    RegisteredCar car = new RegisteredCar();
                    car.setCarNumber(tokens[0]);
                    car.setCarNick(tokens[1]);
                    mCars.add(car);
                }
            }

            ListView listView = (ListView)v.findViewById(R.id.carsListView);
            mCarsAdapter = new CarsAdapter(getActivity(), R.layout.car_item_row, mCars);
            listView.setAdapter(mCarsAdapter);

            View addButton = v.findViewById(R.id.add_car_button);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                com.maximum.fastride.utils.FloatingActionButton fab = (com.maximum.fastride.utils.FloatingActionButton)addButton;
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

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
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
                                    String carNick =  mCarNickInput.getText().toString();

                                    RegisteredCar car = new RegisteredCar();
                                    car.setCarNumber(carNumber);
                                    car.setCarNick(carNick);

                                    mCarsAdapter.add(car);
                                    mCarsAdapter.notifyDataSetChanged();

                                    saveCars();
                                }
                            })
                            .build();
                    mCarInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNumber);
                    mCarNickInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNick);
                    dialog.show();

                    //Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
                }
            });
        } catch(Exception ex) {
            Log.e("FR", ex.getMessage());
        }
        return v;
   }

    private void saveCars() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Set<String> carsSet = new HashSet<String>();
        for (RegisteredCar car : mCars) {
            String s = car.getCarNumber() + "~" + car.getCarNick();
            carsSet.add(s);
        }
        editor.putStringSet(Globals.CARS_PREF, carsSet);
        editor.apply();
    }

}
