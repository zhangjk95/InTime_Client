package com.example.kevin.mapapplication.ui.userinfo;

import android.app.LauncherActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kevin.mapapplication.R;
import com.example.kevin.mapapplication.model.ConnectionManager;
import com.example.kevin.mapapplication.utils.AsyncJSONHttpResponseHandler;
import com.example.kevin.mapapplication.utils.CustomListItem;
import com.example.kevin.mapapplication.utils.CustomListViewAdapter;
import com.example.kevin.mapapplication.utils.RefreshableView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class FriendsActivity extends AppCompatActivity {

    public static final int RESULT_CODE = 233;

    private ProgressBar loading;
    private SharedPreferences userinfo;

    private ListView pendingListView, waitingListView, acceptedListView;
    private Map<String, String> usernametoid;
    private TextView pendingdivider, waitingdivider, accepteddivider;
    private ImageButton addfriends;
    RelativeLayout no_friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        ImageButton back = (ImageButton) findViewById(R.id.btn_back);
        assert back != null;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });


       /* final RefreshableView refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);

        assert refreshableView != null;
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshableView.finishRefreshing();
            }
        }, 0);*/

        loading = (ProgressBar) findViewById(R.id.friends_loading);
        userinfo = getSharedPreferences("User_info", MODE_PRIVATE);
        addfriends = (ImageButton) findViewById(R.id.friends_add_btn);

        pendingListView = (ListView) findViewById(R.id.friends_pending_list);
        waitingListView = (ListView) findViewById(R.id.friends_waiting_list);
        acceptedListView = (ListView) findViewById(R.id.friends_accepted_list);

        pendingdivider = (TextView) findViewById(R.id.friends_pending_divider);
        waitingdivider = (TextView) findViewById(R.id.friends_waiting_divider);
        accepteddivider = (TextView) findViewById(R.id.friends_accepted_divider);
        no_friend = (RelativeLayout) findViewById(R.id.friend_no_friend_layout);

        addfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, AddFriendsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void GetFriendsList() {
        loading.setVisibility(View.VISIBLE);
        usernametoid = new HashMap<>();
        no_friend.setVisibility(View.INVISIBLE);

        AsyncJSONHttpResponseHandler handler = new AsyncJSONHttpResponseHandler() {
            @Override
            public void onSuccessWithJSON(int statusCode, Header[] headers,  JSONObject res) throws JSONException {
                loading.setVisibility(View.INVISIBLE);
                JSONArray friends_list = res.getJSONArray("friends");

                final List<CustomListItem> pendinglist = new ArrayList<>(), waitinglist = new ArrayList<>(), acceptedlist = new ArrayList<>();
                for(int i = 0; i <friends_list.length(); i++) {
                    JSONObject friends_item = friends_list.optJSONObject(i);
                    Log.i("FRIENDS::", friends_item.optString("username"));
                    switch (friends_item.optString("status")) {
                        case "waiting":
                            waitinglist.add(new CustomListItem(friends_item.optString("username")));
                            usernametoid.put(friends_item.optString("username"), friends_item.optString("uid"));
                            break;
                        case "pending":
                            pendinglist.add(new CustomListItem(friends_item.optString("username")));
                            usernametoid.put(friends_item.optString("username"), friends_item.optString("uid"));
                            break;
                        case "accepted":
                            acceptedlist.add(new CustomListItem(friends_item.optString("username")));
                            usernametoid.put(friends_item.optString("username"), friends_item.optString("uid"));
                            break;
                    }
                }

                Comparator<CustomListItem> comparator = new Comparator<CustomListItem>() {
                    @Override
                    public int compare(CustomListItem lhs, CustomListItem rhs) {
                        return lhs.text.compareTo(rhs.text);
                    }
                };
                Collections.sort(pendinglist, comparator);
                Collections.sort(waitinglist, comparator);
                Collections.sort(acceptedlist, comparator);

                CustomListViewAdapter waitinglistAdapter = new CustomListViewAdapter(FriendsActivity.this, R.layout.listview_item_waiting_friends, waitinglist) {
                    @Override
                    public View setViewDetail(int position, View convertView, ViewGroup parent) {
                        View v = convertView;
                        if (v == null) {
                            LayoutInflater vi;
                            vi = LayoutInflater.from(FriendsActivity.this);
                            v = vi.inflate(R.layout.listview_item_waiting_friends, parent, false);
                        }
                        CustomListItem item = getItem(position);

                        if(item!=null) {
                            TextView username = (TextView) v.findViewById(R.id.friends_waiting_username);
                            username.setText(item.text);
                        }
                        return v;
                    }
                };

                CustomListViewAdapter pendinglistAdapter = new CustomListViewAdapter(FriendsActivity.this, R.layout.listview_item_pending_friends, pendinglist) {
                    @Override
                    public View setViewDetail(int position, View convertView, ViewGroup parent) {
                        View v = convertView;
                        if (v == null) {
                            LayoutInflater vi;
                            vi = LayoutInflater.from(FriendsActivity.this);
                            v = vi.inflate(R.layout.listview_item_pending_friends, parent, false);
                        }
                        final CustomListItem item = getItem(position);
                        if(item!=null) {
                            TextView username = (TextView) v.findViewById(R.id.friends_pending_username);
                            Button accept = (Button) v.findViewById(R.id.friends_pending_accept_btn);
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AcceptFriendRequest(usernametoid.get(item.text));
                                    FriendsActivity.this.onResume();
                                }
                            });
                            username.setText(item.text);
                        }
                        return v;
                    }
                };

                final CustomListViewAdapter acceptedlistAdapter = new CustomListViewAdapter(FriendsActivity.this, R.layout.listview_item_accepted_friends, acceptedlist) {
                    @Override
                    public View setViewDetail(int position, View convertView, ViewGroup parent) {
                        View v = convertView;
                        if (v == null) {
                            LayoutInflater vi;
                            vi = LayoutInflater.from(FriendsActivity.this);
                            v = vi.inflate(R.layout.listview_item_accepted_friends, parent, false);
                        }

                        CustomListItem item = getItem(position);

                        if(item!=null) {
                            TextView username = (TextView) v.findViewById(R.id.friends_accepted_username);
                            username.setText(item.text);
                        }
                        return v;
                    }
                };
                waitingListView.setAdapter(waitinglistAdapter);
                pendingListView.setAdapter(pendinglistAdapter);
                acceptedListView.setAdapter(acceptedlistAdapter);

                boolean flag1 = true, flag2 = true, flag3 = true;

                if(waitinglist.isEmpty()) {
                    flag1 = false;
                    waitingdivider.setVisibility(View.GONE);
                }
                else {
                    waitingdivider.setVisibility(View.VISIBLE);
                }

                if(pendinglist.isEmpty()) {
                    flag2 = false;
                    pendingdivider.setVisibility(View.GONE);
                }
                else {
                    pendingdivider.setVisibility(View.VISIBLE);
                }

                if(acceptedlist.isEmpty()) {
                    flag3 = false;
                    accepteddivider.setVisibility(View.GONE);
                }
                else {
                    accepteddivider.setVisibility(View.VISIBLE);
                }

                if(!flag1&&!flag2&&!flag3) {

                    no_friend.setVisibility(View.VISIBLE);
                }

                acceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
                        intent.putExtra("uid", usernametoid.get(acceptedlist.get(position).text));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                });

                waitingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
                        intent.putExtra("uid", usernametoid.get(waitinglist.get(position).text));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                });

                pendingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(FriendsActivity.this, FriendsDetailActivity.class);
                        intent.putExtra("uid", usernametoid.get(pendinglist.get(position).text));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                });

            }

            @Override
            public void onFailureWithJSON(int statusCode, Header[] headers, JSONObject res, String error) throws JSONException {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(FriendsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        };
        ConnectionManager.getInstance().GetFriendsList(userinfo.getString("uid", null), userinfo.getString("token", null), handler);

    }

    public void AcceptFriendRequest(String uid) {
        loading.setVisibility(View.VISIBLE);
        AsyncJSONHttpResponseHandler handler = new AsyncJSONHttpResponseHandler() {
            @Override
            public void onSuccessWithJSON(int statusCode, Header[] headers, JSONObject res) throws JSONException {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(FriendsActivity.this, "Friend Request Accepted.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailureWithJSON(int statusCode, Header[] headers, JSONObject res, String error) throws JSONException {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(FriendsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        };
        ConnectionManager.getInstance().AddOrAcceptFriendRequest(userinfo.getString("uid", null), uid, userinfo.getString("token", null), handler);
    }

    @Override
    protected void onResume() {
        GetFriendsList();
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        //intent.putExtra("Choosed","Item id");
        setResult(RESULT_CODE, intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

}
