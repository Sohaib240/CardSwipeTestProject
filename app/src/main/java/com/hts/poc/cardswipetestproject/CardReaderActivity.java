package com.hts.poc.cardswipetestproject;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CardReaderActivity extends AppCompatActivity {


    private volatile boolean running;
    private static final String TAG = CardReaderActivity.class.getSimpleName();
    public static ClockAPI clockAPI;
    List<String> list = new ArrayList<>();
    String id;


    private TextView tvInstructions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);

        /*TextView tvInstructions = (TextView) findViewById(R.id.textView);
        tvInstructions.setText("Scan your card!");*/
        createClockAPIObject();

        Button enrollment = (Button) findViewById(R.id.Enrollment);
        enrollment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = true;
                startThreadEnrollment();
                //getStringWait();
            }
        });


        Button verification = (Button) findViewById(R.id.verification);
        enrollment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = true;
                startThreadVerification();
                //getStringWait();
            }
        });
    }

    private void startThreadVerification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearBuffer();
                while(running)
                    readFromReader();
            }
        }).start();
    }

    private void createClockAPIObject() {
        Gson gson = new GsonBuilder().create();

        OkHttpClient okHttpClient = getOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ClockAPI.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        clockAPI = retrofit.create(ClockAPI.class);
    }

    /**
     * Create an OkHTTPClient for retrofit to use
     * Add certificates to allow for HTTPS
     */
    private OkHttpClient getOkHttpClient() {

        try {
            /*
             * Create a trust manager that does not validate certificate chains.
             */
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            } };

            /*
             * Install the trust manager
             */
            final SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());

            /*
             * Create an ssl socket factory with our manager
             */
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();

            return new OkHttpClient()
                    .newBuilder()
                    .readTimeout(20000, TimeUnit.MILLISECONDS)
                    .writeTimeout(20000, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .build();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void startThreadEnrollment() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearBuffer();
                while(running)
                readFromReader();
            }
        }).start();

}
    private void clearBuffer() {
        do {
            getStringWait();
        } while (getStringWait() != null);
    }

    /**
     * Check if there's something to read. If so, process it.
     */
    private void readFromReader() {
        String response = getStringWait();
        if(response == null)
            response = getTemplate();
        if (response != null && !response.isEmpty()) {
            processResponse(response);
        }
    }
    private String getStringWait() {

        Call<ResponseBody> call;
        try {
            TextView badgeNumber = (TextView) findViewById(R.id.badgeNumber);
            id = badgeNumber.getText().toString();
            call = clockAPI.CaptureTemplate(id);

            Response<ResponseBody> response = call.execute();
            if(response.body() != null)
                return response.body().string();
            else
                return null;
        } catch (IOException e) {
            return null;
        }
    }
    private String getTemplate() {
        Call<ResponseBody> call;
        try {
            /*if((clockAPI.CaptureTemplate("1234").isExecuted())) {*/
            call = clockAPI.CaptureTemplate(id);
            /*} else {*/
            //call = clockAPI.getProxOneshot();
            /*}*/

            Response<ResponseBody> response = call.execute();
            if(response.body() != null)
                return response.body().string();
            else
                return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Got a json response from REST. Process that response for the read value.
     */
    private void processResponse(final String proxEvent) {
        Log.d(TAG, "Processing : " + proxEvent);
        list.add(proxEvent);

        try {
            JSONObject jsonObject = new JSONObject(proxEvent);
            final String readValue = jsonObject.getString("readValue");

            if (!readValue.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String result = "Read Value: " + readValue;
                        //TextView displayTextView = (TextView) findViewById(R.id.textView2);
                        //displayTextView.setText(result);

                        flickerLED("green");
                    }
                });
            }
            playSound();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void flickerLED(final String color) {
        switchLED(color, "on");

        // After a delay, switch led off
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                switchLED(color, "off");
            }
        }, 1000);
    }

    /**
     * Define a call to switch the given LED to its desired state.
     * Use the static clockAPI defined in MainActivity.
     */
    public void switchLED(final String color, final String state) {

        Call<ResponseBody> call = null;
        switch (color) {
            case "white":
                call = clockAPI.switchWhiteLED(state);
                break;

            case "green":
                call = clockAPI.switchGreenLED(state);
                break;

            case "red":
                call = clockAPI.switchRedLED(state);
                break;
        }

        if (call != null) {
            execute(call);
        }
    }

    /**
     * Execute the call defined above.
     * Must be done on a separate thread.
     */
    private void execute(final Call<ResponseBody> call) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    public int add(int a, int b) {
        return a+b;
    }

    private void playSound() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);
        mp.start();
    }

}
