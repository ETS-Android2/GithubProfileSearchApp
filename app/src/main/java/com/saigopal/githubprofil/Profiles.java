package com.saigopal.githubprofil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Profiles extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private com.saigopal.githubprofil.recycleViewAdapter recycleViewAdapter;
    private int PageNumber = 1;
    private ArrayList<ProfileModel> dataList = new ArrayList<>();
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        final String Name = getIntent().getStringExtra("SearchName");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.loading);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toolbar.setTitle("Profiles");

        assert Name != null;
        if (!Name.isEmpty())
        {
            if (Functions.isNetworkAvailable(Profiles.this)) {
                Profile_details profile = new Profile_details(Name, PageNumber);
                profile.execute();
            }
            else {
                Error("No Internet");
            }
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1))
                {
                    if (Functions.isNetworkAvailable(Profiles.this)) {
                        progressBar.setVisibility(View.VISIBLE);
                        PageNumber = PageNumber + 1;
                        Profile_details profile = new Profile_details(Name, PageNumber);
                        profile.execute();
                    }
                    else {
                            Error("No Internet");
                    }

                }
            }
        });

        recycleViewAdapter = new recycleViewAdapter(dataList);
        recyclerView.setAdapter(recycleViewAdapter);

    }


    @SuppressLint("StaticFieldLeak")
    class Profile_details extends AsyncTask<String, Void, String> {

        static final String KEY_USER_ID = "login";
        static final String KEY_AVATAR_URL = "avatar_url";

        String Name;
        public Profile_details(String name , int pageNumber) {
            Name = name;
            PageNumber = pageNumber;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            String xml = Functions.Get("https://api.github.com/search/users?q="+Name+"&page="+PageNumber);
            Log.d("URL ", Objects.requireNonNull(xml));
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            try {
                JSONObject jsonResponse = new JSONObject(xml);
                JSONArray jsonArray = jsonResponse.optJSONArray("items");
                if(jsonArray == null || jsonArray.length() == 0)
                {
                    Error("Profile Not found");
                } else {
                    toolbar.setTitle("Total Profiles : "+jsonResponse.optString("total_count"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ProfileModel model = new ProfileModel(jsonObject.optString(KEY_USER_ID),jsonObject.optString(KEY_AVATAR_URL));
                        dataList.add(model);
                        recycleViewAdapter.notifyDataSetChanged();
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                }
                assert jsonArray != null;
                progressBar.setVisibility(View.GONE);

            } catch (JSONException e) {
                Log.d("Json Exception : ", e + "");
                Error("Json Exception");
            }


        }

    }

    private void Error(String ErrorType)
    {
        Snackbar.make(progressBar,ErrorType,Snackbar.LENGTH_SHORT).show();
    }

}