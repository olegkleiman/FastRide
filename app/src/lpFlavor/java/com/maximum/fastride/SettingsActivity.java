package com.maximum.fastride;

import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.adapters.CarsAdapter;
import com.maximum.fastride.model.GFence;
import com.maximum.fastride.model.RegisteredCar;
import com.maximum.fastride.utils.FloatingActionButton;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.wamsUtils;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends BaseActivity {

    List<RegisteredCar> mCars;
    CarsAdapter mCarsAdapter;

    private static final String LOG_TAG = "FR.Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupUI(getString(R.string.title_activity_settings), "");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh_settings) {
            onRefreshGeofences();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onRefreshGeofences() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    MobileServiceClient wamsClient =
                            new MobileServiceClient(
                                    Globals.WAMS_URL,
                                    Globals.WAMS_API_KEY,
                                    getApplicationContext());

                    MobileServiceSyncTable<GFence> gFencesSyncTable = wamsClient.getSyncTable("gfences",
                            GFence.class);
                    MobileServiceTable<GFence> gFencesTbl = wamsClient.getTable(GFence.class);

                    wamsUtils.sync(wamsClient, "gfences");

                    Query pullQuery = gFencesTbl.where();
                    gFencesSyncTable.purge(pullQuery);
                    gFencesSyncTable.pull(pullQuery).get();

                    // TEST
                    MobileServiceList<GFence> gFences
                            = gFencesSyncTable.read(pullQuery).get();
                    for (GFence _gFence : gFences) {
                        double lat = _gFence.getLat();
                        double lon = _gFence.getLon();
                        Log.i(LOG_TAG, "GFence: " + lat + " " + lon);
                    }

                } catch(MalformedURLException | InterruptedException | ExecutionException ex ) {
                    Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
                }

                return null;
            }
        }.execute();


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
                String strCar = iterator.next();

                String[] tokens = strCar.split("~");
                RegisteredCar car = new RegisteredCar();
                car.setCarNumber(tokens[0]);
                car.setCarNick(tokens[1]);
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

                final RegisteredCar currentCar =  mCarsAdapter.getItem(position);

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

                                String strCarNumber = mCarInput.getText().toString();
                                if( strCarNumber.length() < 7 ) {
                                    mCarInput.setError(getString(R.string.car_number_validation_error));
                                    return;
                                }

                                mCars.remove(currentCar);

                                String carNick = mCarNick.getText().toString();

                                RegisteredCar registeredCar = new RegisteredCar();
                                registeredCar.setCarNumber(strCarNumber);
                                registeredCar.setCarNick(carNick);

                                mCars.add(registeredCar);

                                // Adapter's items will be updated since underlaying list changes
                                mCarsAdapter.notifyDataSetChanged();

                                saveCars();


                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }


                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                String carNumber = mCarInput.getText().toString();

                                RegisteredCar carToRemove = null;
                                for(RegisteredCar car : mCars) {
                                    if( car.getCarNumber().equals(carNumber) ) {
                                        carToRemove = car;
                                    }
                                }

                                if( carToRemove!= null ) {

                                    mCarsAdapter.remove(carToRemove);
                                    mCarsAdapter.notifyDataSetChanged();

                                    saveCars();
                                }
                                dialog.dismiss();
                            }
                        })
                        .build();
                mCarInput = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNumber);
                mCarInput.setText(currentCar.getCarNumber());
                mCarNick = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNick);
                mCarNick.setText(currentCar.getCarNick());

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
    private EditText mCarNick;

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
                        String carNick = mCarNick.getText().toString();

                        RegisteredCar car = new RegisteredCar();
                        car.setCarNumber(carNumber);
                        car.setCarNick(carNick);

                        mCarsAdapter.add(car);
                        mCarsAdapter.notifyDataSetChanged();

                        saveCars();
                    }
                })
                .build();

        final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        mCarNick = (EditText) dialog.getCustomView().findViewById(R.id.txtCarNick);
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
        for (RegisteredCar car : mCars) {

            String _s = car.getCarNumber() + "~" + car.getCarNick();
            carsSet.add(_s);

        }
        editor.putStringSet(Globals.CARS_PREF, carsSet);
        editor.apply();
    }
}


