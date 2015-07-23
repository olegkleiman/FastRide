package com.maximum.fastride;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.R;
import com.maximum.fastride.adapters.PassengerListAdapter;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.model.User;
import com.maximum.fastride.utils.IRecyclerClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RideDetailsActivity extends BaseActivity
        implements IRecyclerClickListener {

    ImageView DriverImage;
    ImageView SelfiImage;
    TextView carNumber;
    TextView created;
    TextView nameDriver;
    RecyclerView recyclerViewPass;
    RelativeLayout rawLayout;

    Boolean boolPassengersList;

    List<User> lstPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        //setupUI("Ride details", "");
        Ride ride = (Ride) getIntent().getSerializableExtra("ride");

        DriverImage = (ImageView) findViewById(R.id.imageDriver);
        SelfiImage = (ImageView) findViewById(R.id.selfi);
        carNumber = (TextView) findViewById(R.id.txtCarNumber);
        nameDriver = (TextView) findViewById(R.id.txtNameDriver);
        created = (TextView) findViewById(R.id.txtCreated);
        rawLayout = (RelativeLayout) findViewById(R.id.myRideDetail);

        DriverImage.setImageResource(R.drawable.driver50);

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        carNumber.setText(ride.getCarNumber());
        nameDriver.setText(ride.getNameDriver());
        created.setText(df.format(ride.getCreated()));
        carNumber.setText(ride.getCarNumber());

        //TODO:  for test
        boolPassengersList = true;
        //------

        if(boolPassengersList == true) {

            SelfiImage.setVisibility(View.GONE);


            RecyclerView recycler = (RecyclerView) findViewById(R.id.recyclerPassengers);
            recycler.setHasFixedSize(true);
            recycler.setLayoutManager(new LinearLayoutManager(this));
            recycler.setItemAnimator(new DefaultItemAnimator());

            getPassenger();

            PassengerListAdapter adapter = new PassengerListAdapter(this, lstPassenger);
            recycler.setAdapter(adapter);
        }
        else {
            findViewById(R.id.recyclerPassengers).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.textViewListPass)).setText(R.string.ride_photo);
            //TODO:  need implementation
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ride_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getPassenger  (){
        lstPassenger = new ArrayList<User>();

        User pass1 = new User();
        pass1.setFirstName("aaaa");
        pass1.setLastName("AAA");
        //pass1.setPictureURL(R.drawable.passenger64);
        lstPassenger.add(pass1);

        User pass2 = new User();
        pass2.setFirstName("bbb");
        pass2.setLastName("BBB");
        //pass2.setPictureURL(R.drawable.passenger64);
        lstPassenger.add(pass2);

        User pass3 = new User();
        pass3.setFirstName("ccc");
        pass3.setLastName("CCC");
        //pass2.setPictureURL(R.drawable.passenger64);
        lstPassenger.add(pass3);
    }


    @Override
    public void clicked(View view, int position) {

    }

    public void onClickSendAppeal(View view) {

        new MaterialDialog.Builder(this)
                .title("")
                .content("")
                .customView(R.layout.dialog_write_appeal, true)
                .autoDismiss(true)
                .cancelable(true)
                .positiveText(R.string.add_car_button_add)
                .negativeText(R.string.add_car_button_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(getApplicationContext(),
                                SettingsActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
