package com.example.marti.employeetracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A login screen that offers login via user/password.
 */

public class LoginActivity extends AppCompatActivity {
//TODO needs to check if a user and passwkrd is within a databse
    //TODO meeds tl check if an employee or admin and send them to the right activity
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_CAMERA = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };


    //log in variables
    private String username;
    private String password;

    private Worker worker;
    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private int numAttempts = 4;


    private boolean cancel = false;
    //databasse
    private FirebaseAuth myAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String token = new String();
    private static final String TAG = "LoginActivity";

    private FirebaseUser fUser;

    //database data
    private ChangingString isAdmin = new ChangingString();

    private ProgressDialog bar;

    private String userN;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Login");
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_login);

        isAdmin.setActivityContext(this);

        bar = new ProgressDialog(this);
        bar.setMessage("Loading...");
        bar.setCancelable(false);
        bar.show();
        // Check if user is signed in (non-null) and update UI accordingly.

        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.user);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    //attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        bar.cancel();
    }

    private void attemptLogin() {

        mUserView.setError(null);
        mPasswordView.setError(null);


        username = mUserView.getText().toString();
        password = mPasswordView.getText().toString();


        if(!isValid(username, password))
            return;

        signIn(username, password);

        bar.setMessage("Signing in....");
        bar.show();

        //needed delay for sign in attempt
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                signIn(username, password);

                getAdminBit();
                bar.cancel();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkUser();
                    }
                }, 2000);


            }
        }, 5000);
    }

    private void checkUser()
    {



        if (isAdmin.getBit() != null) {
            mUserView.setError(null);
            mPasswordView.setError(null);


            startActivity();

        } else {
            mUserView.setError("");
            mPasswordView.setError("Incorrect login. Please try again");

            cancel = true;
            if (numAttempts <= 0) {

                tooManyAttempts();
            } else
                numAttempts--;
        }
    }


    private void tooManyAttempts()
    {
        new AlertDialog.Builder(this).setTitle("Too many Attempts").setIcon(android.R.drawable.ic_dialog_alert).setTitle("Too Many Attempts")
                .setMessage("\tYou exceeded the allowed attempts \n\tapplication will now close").setPositiveButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();
            }
        }).show();





    }


//We'll use this to get the admin bit from the current user

    private void getAdminBit()
    {

        userN = fUser.getEmail();
        userN = userN.substring(0,userN.indexOf('@'));

        setLoginTime();

        mDatabase.child("worker").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isAdmin.setBit(dataSnapshot.child(userN).child("adminBit").getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        setLoginTime();

    }

    private void setLoginTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        mDatabase.child("worker").child(userN).child("lastLog").setValue(dateFormat.format(date));
    }



    private void startActivity() {

        setLoginTime();


        if(isAdmin.getBit().equals("0"))
        {
            Intent intentEmpPage = new Intent(LoginActivity.this, EmployeePage.class);
            this.startActivity(intentEmpPage);



        } else {

            //instance of the intent to start the admin page
            Intent intentEmpPage = new Intent(LoginActivity.this, AdminPage.class);

            //finshed with the login page so we will finish the activity.
            //finish();
            this.startActivity(intentEmpPage);

        }
    }

    private class ChangingString implements ChangeListener {
        private String bit = null;
        private Context activityContext;

        @Override
        public void onChange(ChangeEvent changeEvent) {
            startActivity();
        }

        public String getBit() {
            return bit;
        }

        public void setBit(String bit) {
            this.bit = bit;
        }

        public Context getActivityContext() {
            return activityContext;
        }

        public void setActivityContext(Context activityContext) {
            this.activityContext = activityContext;
        }
    }


    private void signIn(String user, String password)
    {

        //Signing into the user with the given username and password
        //@email is added in order to log into account because firebase only allows emails
        myAuth.signInWithEmailAndPassword(user + "@employeetracker.com", password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {

                    fUser = myAuth.getCurrentUser();
                    mDatabase = FirebaseDatabase.getInstance().getReference();

                }
            }
        });

        fUser = myAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //To check if the strings given are valid
    private boolean isValid(String user, String password)
    {
        boolean flag = true;

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

          return flag;
    }
}

