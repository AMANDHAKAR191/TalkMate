package com.aman.talkmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.aman.talkmate.databinding.ActivityWelcomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomeActivity extends AppCompatActivity {
    ActivityWelcomeBinding binding;
    SharedPreferences sharedPreferences;
    public final String TALKMATE_SHARED_PREF = "TalkMateDB";
    public final String IS_NEW_USER = "newUser";
    public final String IS_LOGGED_IN = "isLoggedIn";
    private String REQUEST_CODE_NAME = "request_code";
    private String REQUEST_ID = "WelcomeActivity";
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(TALKMATE_SHARED_PREF, MODE_PRIVATE);
        reference = FirebaseDatabase.getInstance().getReference("myConversations");

        //Check if User is already login then go direct to HomeScreen
        Boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN,false);
        if (isLoggedIn){
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.loadingAnimationView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Already LoggedIn", Toast.LENGTH_SHORT).show();

            //load prev chat from DB
            reference.child("AMAN_DHAKAR").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    intent.putExtra(REQUEST_CODE_NAME, REQUEST_ID);
                    if (dataSnapshot.exists()){
                        String dataSnapshotInString = dataSnapshot.getValue(String.class);
                        System.out.println("Old User");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "loading chat...", Toast.LENGTH_SHORT).show();

                            }
                        });
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(IS_NEW_USER, false);
                        editor.apply();

                        intent.putExtra("dataSnapshotInString", dataSnapshotInString);
                    }else {
                        System.out.println("New User");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WelcomeActivity.this, "Creating Account...", Toast.LENGTH_SHORT).show();

                            }
                        });
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(IS_NEW_USER, true);
                        editor.apply();
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent, savedInstanceState);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.loadingAnimationView.setVisibility(View.INVISIBLE);
                }
            });
        }

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loadingAnimationView.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(IS_LOGGED_IN, true);
                editor.apply();

                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.putExtra(REQUEST_CODE_NAME, REQUEST_ID);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}