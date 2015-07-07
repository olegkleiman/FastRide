package com.maximum.fastride;

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
import android.widget.TextView;

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
    TextView carNumber;
    TextView created;
    TextView nameDriver;
    LinearLayout rawLayout;

    List<User> lstPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        //setupUI("Ride details", "");
        Ride ride = (Ride) getIntent().getSerializableExtra("ride");

        DriverImage = (ImageView) findViewById(R.id.imageDriver);
        carNumber = (TextView) findViewById(R.id.txtCarNumber);
        nameDriver = (TextView) findViewById(R.id.txtNameDriver);
        created = (TextView) findViewById(R.id.txtCreated);
        rawLayout = (LinearLayout) findViewById(R.id.myRideDetail);

        DriverImage.setImageResource(R.drawable.driver64);

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        carNumber.setText(ride.getCarNumber());
        nameDriver.setText(ride.getNameDriver());
        created.setText(df.format(ride.getCreated()));
        carNumber.setText(ride.getCarNumber());




        RecyclerView recycler = (RecyclerView)findViewById(R.id.recyclerPassengers);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setItemAnimator(new DefaultItemAnimator());

        getPassenger();

        PassengerListAdapter adapter = new PassengerListAdapter(this,lstPassenger);
        recycler.setAdapter(adapter);
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
}
