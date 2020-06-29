package find.staff.findstaff.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import find.staff.findstaff.MyLocationService;
import find.staff.findstaff.R;
import find.staff.findstaff.data.StaffModel;

public class StaffHomeActivity extends AppCompatActivity {

    static StaffHomeActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static StaffHomeActivity getInstance() {
        return instance;
    }

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView logout;

    ImageView memberImage, mapImage;
    EditText memberName, memberEmail, memberPhone;
    String imageUrl, name, email, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        instance = this;

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            updateLocation();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(StaffHomeActivity.this, "you must accept this location", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        memberImage = findViewById(R.id.profile_image);
        memberEmail = findViewById(R.id.email_field);
        memberName = findViewById(R.id.name_field);
        memberPhone = findViewById(R.id.phone_field);
        mapImage = findViewById(R.id.map_image);

        logout = findViewById(R.id.staff_logout);

        logout.setOnClickListener(view -> new AlertDialog.Builder(StaffHomeActivity.this)
                .setTitle("Sign Out !!")
                .setMessage("Are You Sure To Sign Out ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(StaffHomeActivity.this, LoginActivity.class);
                    startActivity(i);
                })
                .setNegativeButton("No", null)
                .show());

        getData();
    }

    private String getUID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }

    private void getData() {
        databaseReference.child("Staff").child(getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StaffModel staffModel = dataSnapshot.getValue(StaffModel.class);

                name = staffModel.getMemberName();
                phone = staffModel.getMemberPhone();
                email = staffModel.getMemberEmail();
                imageUrl = staffModel.getMemberImgUrl();

                memberName.setText(name);
                memberEmail.setText(email);
                memberPhone.setText(phone);

                if (!StaffHomeActivity.this.isFinishing()) {
                    Glide.with(StaffHomeActivity.this)
                            .load(imageUrl)
                            .into(memberImage);
                }

                Glide.with(getApplicationContext())
                        .load(imageUrl)
                        .into(memberImage);

                mapImage.setOnClickListener(view -> {
                    double longitude = staffModel.getLongitude();
                    double latitude = staffModel.getLatitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latitude + "," + longitude));
                    startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            }
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    public void updateTextView(double latitude, double longitude) {
        StaffHomeActivity.this.runOnUiThread(() -> {
            StaffModel staffModel = new StaffModel(getUID(), name, email, phone, imageUrl, latitude, longitude);
            databaseReference.child("Staff").child(getUID()).setValue(staffModel);
            //txtLocation.setText(value);
        });
    }
}