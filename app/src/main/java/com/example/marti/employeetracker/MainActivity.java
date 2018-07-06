package com.example.marti.employeetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
//TODO add a sort of mmovement animation tot he background so the homescreen doesnt look plain and cheap

    private ImageView mapImage;
    final Matrix matrix = new Matrix();
    private RectF mDisplayRect = new RectF();
    private ValueAnimator mAnimate;
    private float scaleFactor;
    private int direc = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This will set the title of the page
        setTitle("\t\t\t\tEmployee Tracker");

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.activity_main);

        //for signing out
        //FirebaseAuth.getInstance().signOut();

        //the two buttons that will be displayed on the main activity
        final Button loginB = (Button) findViewById(R.id.loginButton);
        final Button registerB = (Button) findViewById(R.id.registerButton);
        mapImage = findViewById(R.id.map);

        //to start the animated image
        mapImage.post(new Runnable() {
            @Override
            public void run() {
                scaleFactor = (float) mapImage.getHeight() / (float) mapImage.getDrawable().getIntrinsicHeight();
                matrix.postScale(scaleFactor,scaleFactor);
                mapImage.setImageMatrix(matrix);
                animate();

            }
        });

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras <= 0) {
            new AlertDialog.Builder(this).setTitle("Camera Not Found").setIcon(android.R.drawable.ic_dialog_alert).setTitle("Too Many Attempts")
                    .setMessage("\tThere is no camera available \n\tthe application cannot be used on this device\n\tand will now close").setPositiveButton("okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    finish();
                }
            }).show();
        }


        //This is the welcome toast message at the beginning of the
        //application
        Toast wel = Toast.makeText(getApplicationContext(), "Welcome !", Toast.LENGTH_SHORT);

        //here we will be displaying he toast message
        wel.show();

        //call the method in which we will make the requests
        //to use the camera and the GPS
        requestCameraLocation();


        //Listener for whe the user clicks on the Login button displayed
        //within the main activity
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create the login activty intent
                Intent intentL = new Intent(MainActivity.this, LoginActivity.class);

                //start the activity for that intent
                startActivityForResult(intentL, 1);
                //if we return from the login activity the application will end

               // FirebaseAuth.getInstance().signOut();

            }
        });


        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentL = new Intent(MainActivity.this, RegistrationActivity.class);

                startActivityForResult(intentL, 1);


            }
        });
    }

    //how the image will move
    private void updateDisplayRect() {
        mDisplayRect.set(0, 0, mapImage.getDrawable().getIntrinsicWidth(),mapImage.getDrawable().getIntrinsicHeight());
       matrix.mapRect(mDisplayRect);
    }
    private void animate() {
        updateDisplayRect();
        if(direc == 1) {
            imageMovement(mDisplayRect.left, mDisplayRect.left - (mDisplayRect.right - mapImage.getWidth()));
        } else {
            imageMovement(mDisplayRect.left, 0.0f);
        }
    }

    //for the animation of the image
    private void imageMovement(float from, float to) {
        mAnimate = ValueAnimator.ofFloat(from, to);
        mAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                matrix.reset();
                matrix.postScale(scaleFactor, scaleFactor);
                matrix.postTranslate(value, 0);

                mapImage.setImageMatrix(matrix);

            }
        });
        mAnimate.setDuration(10000);
        mAnimate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(direc == 1)
                    direc = 2;
                else
                    direc = 1;

                animate();
            }
        });
        mAnimate.start();


    }

    //This method will request for both the location and camera permissions needed
    //for the application
    private void requestCameraLocation()
    {

        if (ContextCompat.checkSelfPermission(this, CAMERA)
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {CAMERA, ACCESS_FINE_LOCATION }, 1);
    }

}
