package com.softwareapp.group9.doctorpatientapp.consultdoctor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.softwareapp.group9.doctorpatientapp.R;
import com.softwareapp.group9.doctorpatientapp.doctorfeedback.DoctorFeedbackActivity;
import com.softwareapp.group9.doctorpatientapp.facilitiesnearme.FacilitiesNearMeActivity;
import com.softwareapp.group9.doctorpatientapp.medicalcondition.ViewMedicalConditionActivity;
import com.softwareapp.group9.doctorpatientapp.userprofile.PatientProfileActivity;

import java.io.ByteArrayOutputStream;

public class ConsultDoctorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView navigationView;

    private static final String TAG = "CurrentLocationApp";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private String reference;
    Button saveLocationToFirebase;
    String value_lat = null;
    String value_lng=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_patient_consult_doctor_home);
        mToolbar = (Toolbar) findViewById(R.id.appTb);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_patient);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.patientNv);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle("Consult Doctor");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        reference = "Users/Patients/" + userId + "/location";
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationDatabaseReference= mFirebaseDatabase.getReference(reference);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        saveLocationToFirebase=(Button)findViewById(R.id.getLocation);
        buildGoogleApiClient();


        final Button uploadImage = (Button) findViewById(R.id.button4);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePhotoDialog dialog = new ChangePhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.change_photo_dialog));
//                Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
//                startActivity(intent);
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }


    /**
     * Compress a bitmap by the @param "quality"
     * Quality can be anywhere from 1 -100 : 100 being the highest quality.
     * @param bitmap
     * @param quality
     * @return
     */
    public Bitmap compressBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return bitmap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        if(mToggle.onOptionsItemSelected(menuItem)){
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id  = item.getItemId();
        //Plan here is to have all the home screens of all the other features in one project so that our navigation bar can access them
        switch(id) {
            case R.id.nav_profile: Intent intent = new Intent(this, PatientProfileActivity.class); startActivity(intent); finish(); break;
            case R.id.nav_condition: Intent intent2 = new Intent(this, ViewMedicalConditionActivity.class); startActivity(intent2); finish(); break;
            case R.id.nav_facilities: Intent intent4 = new Intent(this, FacilitiesNearMeActivity.class); startActivity(intent4); finish(); break;
            case R.id.nav_feedback: Intent intent5 = new Intent(this, DoctorFeedbackActivity.class); startActivity(intent5); finish(); break;
            case R.id.nav_logout: System.exit(0);
        }
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout_patient);
        layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            value_lat= String.valueOf(mLastLocation.getLatitude());
            value_lng =String.valueOf(mLastLocation.getLongitude());
            mLatitudeText.setText(value_lat);
            mLongitudeText.setText(value_lng);



            saveLocationToFirebase.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    mLocationDatabaseReference.push().setValue("Latitude : "+value_lat +"  & Longitude : "+value_lng);
                    Toast.makeText(ConsultDoctorActivity.this ,"Location saved to the Firebasedatabase",Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}
