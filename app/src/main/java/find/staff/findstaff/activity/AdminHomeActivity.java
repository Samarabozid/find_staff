package find.staff.findstaff.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import find.staff.findstaff.R;
import find.staff.findstaff.data.StaffModel;

public class AdminHomeActivity extends AppCompatActivity {

    FloatingActionButton addMember;
    TextView logout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DividerItemDecoration dividerItemDecoration;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String key;
    List<StaffModel> list;
    StaffAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        addMember = findViewById(R.id.add_member_fab);
        logout = findViewById(R.id.admin_logout);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        list = new ArrayList<>();

        logout.setOnClickListener(view -> new AlertDialog.Builder(AdminHomeActivity.this)
                .setTitle("Sign Out !!")
                .setMessage("Are You Sure To Sign Out ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(AdminHomeActivity.this,LoginActivity.class);
                    startActivity(i);
                })
                .setNegativeButton("No", null)
                .show());

        addMember.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), AddStaffMemberActivity.class);
            startActivity(i);
        });

        databaseReference.child("Staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    StaffModel staffModel = snapshot.getValue(StaffModel.class);
                    list.add(staffModel);
                    key = snapshot.getKey();
                }

                adapter = new StaffAdapter(list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffVH> {
        List<StaffModel> staffModels;

        StaffAdapter(List<StaffModel> staffModels) {
            this.staffModels = staffModels;
        }

        @NonNull
        @Override
        public StaffAdapter.StaffVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_staff, parent, false);
            return new StaffAdapter.StaffVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StaffVH holder, final int position) {
            final StaffModel staffModel = staffModels.get(position);

            String staffName = staffModel.getMemberName();
            final String staffNumber = staffModel.getMemberPhone();
            final String staffID = staffModel.getId();

            holder.name.setText(staffName);

            isTelephonyEnabled();

            Glide.with(AdminHomeActivity.this)
                    .load(staffModel.getMemberImgUrl())
                    .into(holder.circleImageView);

            holder.call.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+staffNumber.trim()));
                startActivity(intent);
            });

            holder.location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double longitude = staffModel.getLongitude();
                    double latitude = staffModel.getLatitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+latitude+","+longitude));
                    startActivity(intent);
                }
            });

            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(AdminHomeActivity.this)
                            .setTitle("Delete Staff Member")
                            .setMessage("Are you want to delete this member?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseReference.child("Staff").child(staffID).removeValue();
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return staffModels.size();
        }

        class StaffVH extends RecyclerView.ViewHolder {
            CircleImageView circleImageView;
            ImageView call, location;
            TextView name;
            LinearLayout linearLayout;

            StaffVH(@NonNull View itemView) {
                super(itemView);

                circleImageView = itemView.findViewById(R.id.image);
                call = itemView.findViewById(R.id.call);
                location = itemView.findViewById(R.id.location);
                name = itemView.findViewById(R.id.staff_name);
                linearLayout = itemView.findViewById(R.id.linearlayout);
            }
        }
    }

    private boolean isTelephonyEnabled(){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}