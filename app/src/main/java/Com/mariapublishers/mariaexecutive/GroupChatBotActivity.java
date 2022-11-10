package Com.mariapublishers.mariaexecutive;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupChatBotActivity extends AppCompatActivity {
    String TAG = "GroupChatBotActivity";
    Toolbar toolbar;
    ActionBar actionBar = null;
    Utilis utilis;
    static SharedPreferences mPrefs;
    UserInfo obj;

    RecyclerView recyclerView;

    EditText etMsgBox;
    ImageView sendMsg;

    String senderId = "", receiverId = "", str_message = "", str_result = "";
    ArrayList<ChatMessage> messageList = new ArrayList<>();

    ChatAdapter adapter;
    int msgCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_bot);

        utilis = new Utilis(GroupChatBotActivity.this);
        mPrefs = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("MyObject", "");
        obj = gson.fromJson(json, UserInfo.class);

        senderId = obj.getIndexId();

        Intent intent = getIntent();
        String exeName = intent.getStringExtra("name");
        receiverId = intent.getStringExtra("receiverId");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(exeName);
        }
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        msgCount = 0;
        messageList = new ArrayList<>();

        etMsgBox = findViewById(R.id.et_msg_box);
        sendMsg = findViewById(R.id.send_msg);

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chatMsg = etMsgBox.getText().toString().trim();
                if (!chatMsg.isEmpty()) {

                    if (Utilis.isInternetOn()) {
                        sendMsgToServer(chatMsg);
                    } else {
                        Toast.makeText(GroupChatBotActivity.this, GroupChatBotActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        recyclerView = findViewById(R.id.chat_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(GroupChatBotActivity.this, messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(messageList.size());

        getChatMsg();
    }

    private void getChatMsg() {
        if (Utilis.isInternetOn()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.getgroupmsg, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        System.out.println(TAG + " getChatMsg response - " + response);

                        str_result = obj.getString("errorCode");
                        System.out.print(TAG + " getChatMsg result " + str_result);

                        if (Integer.parseInt(str_result) == 0) {

                            str_message = obj.getString("message");
                            msgCount = Integer.parseInt(obj.getString("overAllCount"));

                            JSONArray json = obj.getJSONArray("result");
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject = json.getJSONObject(i);
                                ChatMessage chatMessage = new ChatMessage(
                                        jsonObject.getString("messageText"),
                                        jsonObject.getString("date"),
                                        jsonObject.getString("time"),
                                        jsonObject.getString("dateTime"),
                                        jsonObject.getString("senderId"),
                                        "");

                                messageList.add(chatMessage);
                            }

                            adapter.notifyDataSetChanged();

                            recyclerView.smoothScrollToPosition(messageList.size());

                        }
                        if (Integer.parseInt(str_result) == 2) {

                            str_message = obj.getString("message");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(GroupChatBotActivity.this, GroupChatBotActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

                    if (error instanceof NoConnectionError) {
                        System.out.println("NoConnectionError");
                    } else if (error instanceof TimeoutError) {
                        System.out.println("TimeoutError");

                    } else if (error instanceof ServerError) {
                        System.out.println("ServerError");

                    } else if (error instanceof AuthFailureError) {
                        System.out.println("AuthFailureError");

                    } else if (error instanceof NetworkError) {
                        System.out.println("NetworkError");
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("senderId", obj.getIndexId());
                    params.put("groupId", receiverId);
                    params.put("count", String.valueOf(msgCount));
                    System.out.println(TAG + " getChatMsg inputs " + params);
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(GroupChatBotActivity.this, GroupChatBotActivity.this.getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMsgToServer(final String chatMsg) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utilis.Api + Utilis.sendgroupmsg, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(response);

                    System.out.println(TAG + " sendMsgToServer response - " + response);

                    str_result = obj.getString("errorCode");
                    System.out.print(TAG + " sendMsgToServer result " + str_result);

                    if (Integer.parseInt(str_result) == 0) {

                        etMsgBox.setText("");

                        str_message = obj.getString("message");

                        msgCount = msgCount + 1;
                        ChatMessage chatMessage = new ChatMessage(
                                chatMsg,
                                "",
                                "",
                                obj.getString("datetime"),
                                senderId,
                                receiverId);

                        messageList.add(messageList.size(), chatMessage);
                        adapter.notifyDataSetChanged();

                        recyclerView.smoothScrollToPosition(messageList.size());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(GroupChatBotActivity.this, GroupChatBotActivity.this.getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_SHORT).show();

                if (error instanceof NoConnectionError) {
                    System.out.println("NoConnectionError");
                } else if (error instanceof TimeoutError) {
                    System.out.println("TimeoutError");

                } else if (error instanceof ServerError) {
                    System.out.println("ServerError");

                } else if (error instanceof AuthFailureError) {
                    System.out.println("AuthFailureError");

                } else if (error instanceof NetworkError) {
                    System.out.println("NetworkError");
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("senderId", obj.getIndexId());
                params.put("groupId", receiverId);
                params.put("text", chatMsg);
                System.out.println(TAG + " sendMsgToServer inputs " + params);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void back() {
        Intent iback = new Intent(GroupChatBotActivity.this, ChatListActivity.class);
        iback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(iback);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ViewHolder> {
        Activity mActivity;
        private ArrayList<ChatMessage> arrayList;

        private static final int MSG_TYPE_LEFT = 0;
        private static final int MSG_TYPR_RIGHT = 1;

        public ChatAdapter(Activity con, ArrayList<ChatMessage> messageList) {
            mActivity = con;
            arrayList = messageList;
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == MSG_TYPE_LEFT) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_left, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_right, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvMsg.setText(arrayList.get(position).getMessageText());
            holder.tvTime.setText(arrayList.get(position).getDateTime());
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (arrayList.get(position).getSenderId().equals(obj.getIndexId())) {
                return MSG_TYPR_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvMsg;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvTime = view.findViewById(R.id.tv_timestamp);
            tvMsg = view.findViewById(R.id.tv_msg);
        }
    }
}