package find.staff.findstaff.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

import find.staff.findstaff.R;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.getUid().equals("TbwyB7OEsOQrHaQnlsxvYJSFYCD2")) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        // go to the main activity
                        Intent i = new Intent(getApplicationContext(), AdminHomeActivity.class);
                        startActivity(i);
                    }
                };
                // Show splash screen for 3 seconds
                new Timer().schedule(task, 3000);
            } else {
                databaseReference.child("Staff").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user.getUid())) {
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    // go to the main activity
                                    Intent i = new Intent(getApplicationContext(), StaffHomeActivity.class);
                                    startActivity(i);
                                    // kill current activity
                                }
                            };
                            // Show splash screen for 3 seconds
                            new Timer().schedule(task, 3000);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}