package com.example.marti.employeetracker;


import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.identity.intents.AddressConstants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.media.MediaRecorder.VideoSource.CAMERA;


public class AdminPage extends AppCompatActivity implements OnMapReadyCallback{
    //TODO add a back button listener to log off or add a button for that
    //TODO send location to admin

    private byte[] imageB;
    private ImageView selfie;
    private String image;
    private String userN;



    private Intent takePictureIntent;

    //
    private Bitmap bmImage;

    //variables for databasse
    private DatabaseReference mDatabase;
    private FirebaseAuth myAuth;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private FirebaseUser curUser;

    private Criteria criteria;

    private ArrayList<Worker> employeeList = new ArrayList<>();

    private int positionA = 0;

    //dialog bar for loading
    private ProgressDialog bar;

    //Objects we can take form database
    private Worker curWorker = new Worker();

    //variables for other employees
    private String userE;
    private String fNameE;
    private String lNameE;
    private String groupIE;
    private String adminIE;
    private String adminBitE;
    private Double latiE;
    private Double longiE;
    private String photoSE;

    private byte[] photoBE;

    //string array for database search
    static String[] entries;

    // SimpleCursorAdapter adapter;
    //These are the variables for the
    //Google Maps API
    private GoogleMap mMap;
    private Button ping;
    private Button list;
    private double longi;


    private double lati;
    private LocationManager lm;
    private Location loc;
    private MarkerOptions mark;

    private Context context = this;

    //used to store the class of the selected user from the displaylist
    private Worker selectedWorker = new Worker();



    int [] toViews;

    //flags used for methods
    private boolean clickedDisplayL = false;
    private boolean flagStart = true;

    //xml variables
    TextView firstName;
    TextView lastName;
    ListView displayList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_admin_page);



        setTitle("Admin Page");
        //TODO 1 send gps location
        //TODO 2 set picture taken and make it a thumbnail instead of a pin

        bar = new ProgressDialog(this);


        //making all the needed variables for firebase
        myAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        user = myAuth.getCurrentUser();

        //setting all the needed variables for the display list button
        displayList = findViewById(R.id.listE);
        toViews = new int[]{R.id.listOptions};


        //This holds all the strings that will be used to access certain sections of each user within the database
        entries = getResources().getStringArray(R.array.databaseEntries);

        //used to place marker on the map
        mark = new MarkerOptions();


        //Buttons that will be displayed
        ping = (Button) findViewById(R.id.ping);
        list = (Button) findViewById(R.id.list);

        //her we are getting the current users username to be used to send the data to the database
        userN = user.getEmail();
        userN = userN.substring(0,userN.indexOf('@'));

//        //here we are getting the rest of the current users data
//        getCurrentUserData();
//
//        //here we will be setting up the gps
//        gpsSetup();

       // startCamera();

        //loading progress bar is shown
        //we need this because if the user tries to click anything without
        //everything loaded the app will crash
        bar.setMessage("Loading please wait...");
        bar.setCancelable(false);
        bar.show();

        buttonAction();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        onMapReady(mMap);

        //here we are getting the rest of the current users data
        getCurrentUserData();

        //here we will be setting up the gps
        gpsSetup();

        //to clear the display list
        employeeList.clear();


        //get the instance of the current user to retrieve thier info from firebase
        curUser = FirebaseAuth.getInstance().getCurrentUser();

        //Here we are setting up the list for the display list button
        displayList = findViewById(R.id.listE);
        CustomAdapter customA = new CustomAdapter();
        displayList.setAdapter(customA);
        displayList.setVisibility(View.GONE);
        displayList.invalidateViews();

        bar.cancel();

        bar.setMessage("Getting location please wait...");

        bar.show();

    }

    private void startCamera() {


        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA);

        }

    }


    //thsi will get the image from the camera intent and create it into a bitmap
    //so it can be used for whatever
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent takePictureIntent) {
        super.onActivityResult(requestCode, resultCode, takePictureIntent);

        //here we are getting the bitmap from the camera
        bmImage = (Bitmap) takePictureIntent.getExtras().get("data");

        //here we preparing the bitmap to be converted into a Byte array to further the converting process
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmImage.compress(Bitmap.CompressFormat.PNG, 100, stream);

        //Converting the image into a byte array
        imageB = stream.toByteArray();

        //here we are converting the byte array into a string
        image = Base64.encodeToString(imageB,Base64.DEFAULT);

        //her we are getting the current users username to be used to send the data to the database
        userN = user.getEmail();
        userN = userN.substring(0,userN.indexOf('@'));

        //here we are send the image in string form to the database
        mDatabase.child("worker").child(userN).child("photo").setValue(image);

        //here we are getting the rest of the current users data
        getCurrentUserData();

        //here we will be setting up the gps
        gpsSetup();
    }
    //here we will set up the gps to be used by the user
    protected void gpsSetup() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        lm.getBestProvider(criteria, true);

        loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, locationListener);

    }

    //ths will check if there is a change in location from the current user
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longi = location.getLongitude();
            lati = location.getLatitude();

            curWorker.setLatitude(lati);
            curWorker.setLongitude(longi);

            mDatabase.child("worker").child(userN).child("longitude").setValue(longi);
            mDatabase.child("worker").child(userN).child("latitude").setValue(lati);

            if(flagStart)
            {
                selectedWorker.setLongitude(longi);
                selectedWorker.setLatitude(lati);
                jumpMapTo(lati, longi);
                flagStart = false;
            }

            //set the current location of the user
            setCurrentPosition(mMap);

            // this will cancel the map loading dialog
            bar.cancel();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //this enables the google map api
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMap(googleMap);
    }

    //set the current position of the current user
    private void setCurrentPosition(GoogleMap map) {

        //This will clear  the markers present in the map window
        mMap.clear();

        //this will set the marker postion within the map window
        mark.position(new LatLng(lati, longi));

        //sets the title of the marker when the marker is clicked
        mark.title(curWorker.getFirstName() + " " + curWorker.getLastName());

        mMap.addMarker(mark);

        getOtherEmployees();
    }

    //listeners for the buttons displayed at the bottom of the activity
    private void buttonAction() {

        //thisis the listener for the ping employee button
        ping.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }


                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, locationListener);


                jumpMapTo(selectedWorker.getLatitude(),selectedWorker.getLongitude());


            }
        });


//this is the listener for the display list button
        list.setOnClickListener(new View.OnClickListener() {
            //   boolean clicked = false;


            @Override
            public void onClick(View v) {


                //if the button hasnt been clicked we will display the list of employees
                if(!clickedDisplayL)
                {
                    displayList.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    displayList.setVisibility(View.VISIBLE);

                    displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            selectedWorker = (Worker) displayList.getAdapter().getItem(position);
                        }
                    });
                    clickedDisplayL = true;

                }
                //if the button was already clicked and it is clicked again the list will disappear
                else {
                    displayList.setBackgroundColor(Color.parseColor("#00ff0000"));
                    displayList.setVisibility(View.GONE);

                    clickedDisplayL = false;
                }
            }
        });
    }

    private void enableMap(GoogleMap map) {
        map = mMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Why is this here?... I'm not sure myself im being superstitious...if I delete this I assume everything will break...
            return;
        }
    }

    //here we are getting the first and last name of the current user
    private void getCurrentUserData()
    {
        DatabaseReference data = mDatabase.child("worker").child(userN);

        //this will pull the group id of the current user
        data.child("groupId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curWorker.setGroupId(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    //this method will get the information of the other employees from the database and
    //update their classes within the phone running the application
    private void getOtherEmployees()
    {
        DatabaseReference childList = mDatabase.child("worker");

        //this will wait for a change in the worker branch of the database in order to run this section of the code
        childList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot uniqueUserSnapshot : dataSnapshot.getChildren())
                {

                    addOtherEmployeeLocations(uniqueUserSnapshot);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //here we will be pulling the other users data from the database and actually
    //creating wroker class versions of them and addin the them to the arrayList
    private void addOtherEmployeeLocations(DataSnapshot data) {


        Worker l = data.getValue(Worker.class);


        if(l.getUsername() == null)
            return;

        userE = data.child(entries[0]).getValue(String.class);
        fNameE = data.child(entries[1]).getValue(String.class);
        lNameE = data.child(entries[2]).getValue(String.class);
        groupIE = data.child(entries[3]).getValue(String.class);
        adminIE = data.child(entries[4]).getValue(String.class);
        adminBitE = data.child(entries[5]).getValue(String.class);
        latiE =  data.child("latitude").getValue(double.class);
        longiE = data.child("longitude").getValue(double.class);
        photoSE = data.child(entries[8]).getValue(String.class);

        photoBE = Base64.decode(photoSE,Base64.DEFAULT);
        Bitmap bmE = BitmapFactory.decodeByteArray(photoBE,0,photoBE.length);

        if(fNameE == null || lNameE == null)
            return;

        Worker x = new Worker(userE, null, fNameE, lNameE, adminBitE, adminIE, groupIE, null, 0, bmE );

        positionA = 0;


        //the logic if the user is alrady within the arrayList
        if(containsUser(x, positionA))
        {
            employeeList.get(positionA).setLongitude(longiE);
            employeeList.get(positionA).setLatitude(latiE);
        }
        else
        {

            x.setLatitude(latiE);
            x.setLongitude(longiE);

            employeeList.add(x);
        }





        if(userN.equals(x.getUsername()))
            return;

        //here we are adding the marker of the current employee
        //but we are checking to make sure it is not the current employee so we wont have
        //two markers on the map
        if(!userE.equals(curWorker.getUsername()) && adminBitE.equals("0") && groupIE.equals(curWorker.getGroupId()) && fNameE != null && lNameE != null) {
            if(userN.equals(x.getUsername()))
                setMark(latiE, longiE, employeeList.get(positionA).getFirstName() + " " + employeeList.get(positionA).getLastName(), null, true);
            else
                setMark(latiE, longiE, fNameE + " " + lNameE, bmE, false);
        }
    }

    //method for checking if the user is currently within the arrayList
    private boolean containsUser(Worker x, int position) {

        boolean flag = false;

        for(int i =0; i< employeeList.size();i++)
        {
            if(employeeList.get(i).getUsername().equals(x.getUsername()) )
            {
                flag = true;
                positionA = i;
            }
        }

        return flag;
    }

    //this method will acatually set the marker
    private void setMark(double latim, double longim, String title, Bitmap bm, boolean flag)
    {
        mark.position(new LatLng(latim, longim));

        //sets the title of the marker when the marker is clicked
        mark.title(title);

        if(!flag)
            mMap.addMarker(mark.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm,100,100,false))));
        else
            mMap.addMarker(mark);
    }

    //this method will jump to the location given
    //within the program this will jump to the location of the selected employee after pressing button
    private void jumpMapTo(double lati, double longi)
    {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lati, longi), 16));
    }

    //this is the inner  class created for creating the display list
    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return employeeList.size();
        }

        //gets the value of the selected item within the list
        @Override
        public Worker getItem(int position) {
            displayList.requestLayout();
            displayList.setBackgroundColor(Color.parseColor("#00ff0000"));
            displayList.setVisibility(View.GONE);
            clickedDisplayL = false;
            return employeeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        //this will set the layout of each list entry
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.list_layout,null);

            TextView textVF = convertView.findViewById(R.id.textListFirst);
            ImageView imageview = convertView.findViewById(R.id.imageList);
            TextView textVL = convertView.findViewById(R.id.textListLast);

            textVF.setText(employeeList.get(position).getFirstName());
            textVL.setText(employeeList.get(position).getLastName());
            imageview.setImageDrawable(new BitmapDrawable(employeeList.get(position).getBitmapImage()));


            return convertView;
        }
    }



}
