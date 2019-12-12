package com.daurislittle.googlerecaptcha;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

//volley
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;


import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = MainActivity.class.getSimpleName();
    Button btn;
    TextView txtV;
    String Site_Key = "6LcFP8cUAAAAALPrBpvuSPileb7vd_OlhDn7MeI1"; //consider making global variable
    String Site_Secret_Key = "6LcFP8cUAAAAAJyKpv8FKRkd1bSnR-WuKeLc4nve"; //consider making global variable
    RequestQueue requestQueue;

    //application space controls
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.reCaptcha);
        txtV = findViewById(R.id.verifyText);
        btn.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onClick(View view){
        SafetyNet.getClient(this).verifyWithRecaptcha(Site_Key)
                .addOnSuccessListener(this, new OnSuccessListener <SafetyNetApi.RecaptchaTokenResponse>(){
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response){
                        if (!response.getTokenResult().isEmpty()){
                            handleCaptchaResult(response.getTokenResult());

                        }
                    }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof  ApiException){
                            ApiException apiException = (ApiException)e;
                            Log.d(TAG, "Error Message: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                        } else {
                            Log.d(TAG, "Unknown error type or error" + e.getMessage());
                        }
                    }
                });
    }

    void handleCaptchaResult(final String responseToken){
        String url = "https://www.google.com/recaptcha/api/siteverify"; //consider using global variable here
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        txtV.setTextSize(35);
                        txtV.setText(R.string.successful_response);
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "Error message: " + ex.getMessage());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error message: " + error.getMessage());
                    }
                })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("secret", Site_Secret_Key);
                params.put("response", responseToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(50000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
