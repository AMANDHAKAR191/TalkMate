package com.aman.talkmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.aman.talkmate.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private final int REQUEST_CODE_SPEECH_INPUT = 1000;
    SharedPreferences sharedPreferences;
    WelcomeActivity welcomeActivity = new WelcomeActivity();
    private DatabaseReference reference;
    private JSONArray jsonArray, messages = new JSONArray();
    JSONObject initialPrompt = new JSONObject();
    private MessageAdaptor messageAdapter;
    private ArrayList<messageModelClass> messageList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(welcomeActivity.TALKMATE_SHARED_PREF, MODE_PRIVATE);
        reference = FirebaseDatabase.getInstance().getReference("myConversations");
        //set recyclerView
        setRecyclerview();

        Intent intent = getIntent();
        boolean isNewUser = sharedPreferences.getBoolean(welcomeActivity.IS_NEW_USER, false);
        if (isNewUser) {
            binding.welcomeText.setVisibility(View.VISIBLE);
            try {
                initialPrompt.put("role", "system");
                initialPrompt.put("content", "You are a Advance AI, You have act like BestFriend of AMAN");
                messages.put(initialPrompt);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        } else {
            //hide welcome text and logo
            binding.welcomeText.setVisibility(View.INVISIBLE);
            binding.imageTalkMateLogo.setVisibility(View.INVISIBLE);
            String dataSnapshotInString = intent.getStringExtra("dataSnapshotInString");
            System.out.println("dataSnapshotInString: " + dataSnapshotInString);
            try {
                jsonArray = new JSONArray(dataSnapshotInString);
                System.out.println("JSONArray: " + jsonArray);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String role = jsonObject.getString("role");
                    String content = jsonObject.getString("content");
                    messages.put(jsonObject);
                    messageModelClass myData = new messageModelClass(role, content);
                    messageList.add(myData);
                    messageAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        binding.imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = binding.editTextMessage.getText().toString().trim();
                addToChat(question, messageModelClass.SENT_BY_ME);
//                updateMessagesArray(question,messageModelClass.SENT_BY_ME);
                APICall(question);
//                callAPI(question);
                binding.editTextMessage.setText("");
                binding.welcomeText.setVisibility(View.GONE);
                binding.imageTalkMateLogo.setVisibility(View.GONE);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);

    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        private final Color replyBackgroundColor = Color.valueOf(Color.parseColor("#FFC107"));
        private final Color replyTextColor = Color.valueOf(Color.parseColor("#000000"));

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                int position = viewHolder.getAdapterPosition();
                binding.editTextMessage.setText(messageList.get(position).getContent());
                messageAdapter.notifyDataSetChanged();
            } else {
                int position = viewHolder.getAdapterPosition();
                messageList.remove(position);
                messageAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(getResources().getColor(R.color.teal_700));
                paint.setTextSize(getResources().getDimension(R.dimen.font_size));
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);

                float replyTextSize = getResources().getDimension(R.dimen.font_size);

                if (dX > 0) {
                    // Swiping right to reply
                    System.out.println("dX: " + dX);
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);
                    paint.setColor(getResources().getColor(R.color.black));
                    paint.setTextSize(replyTextSize);
                    c.drawText("Reply", (float) dX - 200, (float) itemView.getTop() + (itemView.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2), paint);
                }
                if (dX < 0) {
                    // Swiping left to delete
                    System.out.println("dX: " + dX);
                    System.out.println("dX: " + (itemView.getWidth() + dX));
                    System.out.println("itemweidth: " + itemView.getWidth());
                    c.drawRect((float) itemView.getWidth() + dX, (float) itemView.getTop(), itemView.getRight(), (float) itemView.getBottom(), paint);
                    paint.setColor(getResources().getColor(R.color.black));
                    paint.setTextSize(replyTextSize);
                    c.drawText("Delete", (float) itemView.getWidth() + dX + 100, (float) itemView.getTop() + (itemView.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2), paint);
                }
            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(MainActivity.this, "Storing Chat...", Toast.LENGTH_SHORT).show();
        storeConversationToDatabase(messages);
    }


    void storeConversationToDatabase(JSONArray messages) {
        String messagesInString = messages.toString();
        reference.child("AMAN_DHAKAR").setValue(messagesInString)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error adding documents." + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setRecyclerview() {
        //setup recycler view
        messageAdapter = new MessageAdaptor(messageList, MainActivity.this, MainActivity.this) {
            @Override
            void openOptionMenu(View view) {
                super.openOptionMenu(view);
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_context, popupMenu.getMenu());
                popupMenu.show();
            }
        };
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void updateMessagesArray(String message, String sentBy){
        JSONObject messageObj3 = new JSONObject();
        try {
            messageObj3.put("role", sentBy);
            messageObj3.put("content", message);
            messages.put(messageObj3);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println("messageJSONArray: " + messages);
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new messageModelClass(sentBy, message));
                messageAdapter.notifyDataSetChanged();
                binding.recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void APICall(String question) {
        //okhttp
        messageList.add(new messageModelClass(messageModelClass.SENT_BY_BOT, "Typing... "));
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONObject messageObj3 = new JSONObject();
            messageObj3.put("role", "user");
            messageObj3.put("content", question);
            messages.put(messageObj3);

            jsonBody.put("messages", messages);
            System.out.println("jsonBody: " + jsonBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        System.out.println("messages ==>> " + messages);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to response due to " + e.getMessage());
//                isErrorOccurred = true;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("choices");
                        String result = jsonArray1.getJSONObject(0).getJSONObject("message").getString("content");
                        System.out.println("result: " + result);
                        addResponse(result.trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().toString());
//                    isErrorOccurred = true;
                }
            }
        });
    }

//    void callAPI(String question){
//        //okhttp
//        messageList.add(new messageModelClass("Typing... ",messageModelClass.SENT_BY_BOT));
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("model","text-davinci-003");
//            jsonBody.put("prompt",question);
//            jsonBody.put("max_tokens",4000);
//            jsonBody.put("temperature",0);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
//        Request request = new Request.Builder()
//                .url("https://api.openai.com/v1/completions")
//                .header("Authorization","Bearer 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")
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                addResponse("Failed to load response due to "+e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if(response.isSuccessful()){
//                    JSONObject  jsonObject = null;
//                    try {
//                        jsonObject = new JSONObject(response.body().string());
//                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
//                        String result = jsonArray.getJSONObject(0).getString("text");
//                        addResponse(result.trim());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }else{
//                    addResponse("Failed to load response due to "+response.body().toString());
//                }
//            }
//        });
//
//
//
//
//
//    }

    void addResponse(String response) {
        JSONObject messageObj3 = new JSONObject();
        try {
            messageObj3.put("role", messageModelClass.SENT_BY_BOT);
            messageObj3.put("content", response);
            messages.put(messageObj3);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println("messages => " + messages);
        messageList.remove(messageList.size() - 1);
        addToChat(response, messageModelClass.SENT_BY_BOT);
    }
    void speechToText() {
        // intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi Speak Something");

        //start intent
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addToChat(result.toString(), messageModelClass.SENT_BY_ME);
//                    messageEditText.setText("");
//                    welcomeTextView.setVisibility(View.GONE);
                }
        }
    }

    // Call this method to check for internet connectivity
    private void checkInternetSpeed() {
        new CheckInternetTask().execute();
    }

    private class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("https://www.google.com");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Android");
                connection.setRequestProperty("Connection", "close");
                connection.setConnectTimeout(1500);
                connection.connect();
                return (connection.getResponseCode() == 200);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // Internet is available
                Toast.makeText(MainActivity.this, "Internet is ok", Toast.LENGTH_SHORT).show();
            } else {
                // Internet is not available
                Toast.makeText(MainActivity.this, "Internet is slow", Toast.LENGTH_SHORT).show();
            }
        }
    }
}