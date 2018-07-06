package com.example.marti.employeetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RegistrationActivity extends AppCompatActivity {
//TODO add a user and password tona database

    //xml variables
    private AutoCompleteTextView mUserView;
    private AutoCompleteTextView mGroupView;
    private AutoCompleteTextView mAdminView;
    private EditText mPasswordView;
    private EditText mFirstView;
    private EditText mLastView;
    private EditText mBirthView;
    private View mProgressView;
    private View mLoginFormView;

    //register button
    private Button registerB;

//Variables for the Worker class
    private String user;
    private String password;
    private String fName;
    private String lName;
    private String groupI;
    private String adminI;
    private String admin;
    private String birth;

    //needed firebase variables
    private FirebaseAuth myAuth;
    private DatabaseReference mDatabase;

    //dialog bar for loading
    ProgressDialog bar;

    //for the list selection if the user is an manager or not
    ArrayList<String> adminList;
    Spinner adminCh;
    ArrayAdapter<String> dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_registration);

        adminCh = findViewById(R.id.adminL);

        bar = new ProgressDialog(this);

        buildArray();

        //setting up database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myAuth = FirebaseAuth.getInstance();

        mUserView = findViewById(R.id.user);
        mGroupView = findViewById(R.id.gID);
        mAdminView = findViewById(R.id.aID);

        mPasswordView = findViewById(R.id.password);
        mFirstView = findViewById(R.id.fName);
        mLastView = findViewById(R.id.lName);
        mBirthView = findViewById(R.id.dateOfBirth);

        registerB = findViewById(R.id.user_register_button);

//waiting for the user to click the register button
        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });


    }

    //building array for the list selections
    private void buildArray()
    {
        adminList = new ArrayList<String>();

        adminList.add("Yes");
        adminList.add("No");

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, adminList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminCh.setAdapter(dataAdapter);

    }

    //attempting to register
    private void attemptRegister() {

        mUserView.setError(null);
        mPasswordView.setError(null);
        mGroupView.setError(null);
        mAdminView.setError(null);
        mFirstView.setError(null);
        mLastView.setError(null);
        mBirthView.setError(null);

        boolean flag = false;

        // Store values at the time of the login attempt.
        user = mUserView.getText().toString().toLowerCase();
        password = mPasswordView.getText().toString();
        fName = mFirstView.getText().toString();
        lName = mLastView.getText().toString();
        groupI = mGroupView.getText().toString();
        adminI = mAdminView.getText().toString();
        birth = mBirthView.getText().toString();

        if(!isValid(user, password))
            return;

        //loading progress bar is shown
        bar.setMessage("Registering User...");
        bar.setCancelable(false);
        bar.show();

        //here the user is created
        myAuth.createUserWithEmailAndPassword(user+"@employeetracker.com", password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){

            @Override
            public void onComplete(@NonNull Task<AuthResult> task){
                if(task.isSuccessful()){
                    //user is made
                    Toast.makeText(RegistrationActivity.this, "Account created", Toast.LENGTH_LONG).show();
                    bar.cancel();

                    writeNewUser();

                    //myAuth.signOut();

                    finish();
                }
                else
                {

                        Toast.makeText(RegistrationActivity.this, "Failed Registration: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    bar.cancel();
                }
            }
        });


 }

 //class for settignt he admin bit of new user within firebase
 private void writeNewUser()
 {

    if(String.valueOf(adminCh.getSelectedItem()).equals("Yes"))
        admin = "1";
    else
        admin = "0";

    Worker worker = new Worker(user,password, fName, lName,admin, groupI, adminI, birth, 1, null);

     mDatabase.child("worker").child(user).setValue(worker);
 }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    //Will be used to check if the strings entered are valid
    private boolean isValid(String user, String password)
    {
        boolean flag = true;

        DateValidator date = new DateValidator();

        if(user.isEmpty()){
            mUserView.setError("Please enter your username");
            flag =  false;
        }

        if(password.isEmpty()){
            mPasswordView.setError("Please enter your password");
            flag = false;
        }

        if(!password.isEmpty() && password.contains("`") || password.contains("~") || password.contains("!") || password.contains("@") || password.contains("#") ||
                password.contains("$") || password.contains("%") || password.contains("^") || password.contains("&") || password.contains("*") ||
                password.contains("(") ||  password.contains(")") || password.contains("_") || password.contains("-") || password.contains("+") ||
                password.contains("=")){
            mPasswordView.setError("Password can not contain ` ~ ! @ # $ % ^ & * ( ) _ - + =");
            flag = false;
        }

        if(mGroupView.getText().toString().isEmpty())
        {
            mGroupView.setError("Please enter your group ID");
            flag = false;
        }

        if(mAdminView.getText().toString().isEmpty())
        {
            mGroupView.setError("Please enter your admin ID");
            flag = false;
        }

        if(mFirstView.getText().toString().isEmpty())
        {
            mFirstView.setError("Please enter your first name");
            flag = false;
        }

        if(mLastView.getText().toString().isEmpty())
        {
            mLastView.setError("Please enter your last name");
            flag = false;
        }

        if(mBirthView.getText().toString().isEmpty())
        {
            mBirthView.setError("Please enter your date of birth");
            flag = false;
        }

        if(!mBirthView.getText().toString().isEmpty() && !date.isThisDateValid(mBirthView.getText().toString(),"MM/dd/yyyy"))
        {
            mBirthView.setError("Please enter a correct date of birth format of mm/dd/yyyy");
            flag = false;
        }


        return flag;
    }

    public class DateValidator {

        public boolean isThisDateValid(String dateToValidate, String dateFromat){

            if(dateToValidate == null){
                return false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
            sdf.setLenient(false);

            try {

                //if not valid, it will throw ParseException
                Date date = sdf.parse(dateToValidate);
                System.out.println(date);

            } catch (ParseException e) {

                e.printStackTrace();
                return false;
            }

            return true;
        }

    }
}
