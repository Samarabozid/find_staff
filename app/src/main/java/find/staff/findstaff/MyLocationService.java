package find.staff.findstaff;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import find.staff.findstaff.activity.StaffHomeActivity;

public class MyLocationService extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATE = "find.staff.findstaff.UPDATE_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATE.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location location = result.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    try {
                        StaffHomeActivity.getInstance().updateTextView(latitude,longitude);
                    }catch (Exception e){
                        Toast.makeText(context, latitude +" / " + longitude , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
