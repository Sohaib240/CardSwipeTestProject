package com.hts.poc.cardswipetestproject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ClockAPI {
    // This endpoint can also be https://127.0.0.1
    String ENDPOINT = "https://localhost";

    // --- Sample LED Examples ------------

    // The strings passed in as parameters here take the place of "{onOrOff}"

    @PUT("/led/green/{onOrOff}")
    Call<ResponseBody> switchGreenLED(@Path("onOrOff") String state);

    @PUT("/led/white/{onOrOff}")
    Call<ResponseBody> switchWhiteLED(@Path("onOrOff") String state);

    @PUT("/led/red/{onOrOff}")
    Call<ResponseBody> switchRedLED(@Path("onOrOff") String state);

    // Other examples:

    @PUT("/led/red/on")
    Call<ResponseBody> switchRedLEDOn();

    @PUT("/led/red/off")
    Call<ResponseBody> switchRedLEDOff();

    // ------------------------------

    // --- Biometric Examples -------

    // The strings passed in as parameters here take the place of the queries.

    @PUT("Biometric/CaptureTemplate")
    Call<ResponseBody> CaptureTemplate(@Query("Badge") String badgeNumberString);

    @PUT("/Biometric/VerifyAgainstTemplateList")
    Call<ResponseBody> VerifyAgainstTemplateList(@Query("Count") String templateCountString, @Query("Templates") String templateB64list);

    @PUT("Biometric/PushTemplates")
    Call<ResponseBody> PushTemplates(@Query("Count") String templateCountString, @Query("Templates") String templateB64list);

    @PUT("Biometric/SetSensorMode")
    Call<ResponseBody> SetSensorMode(@Query("Mode") String newMode);

    @PUT("Biometric/GetSensorMode")
    Call<ResponseBody> GetSensorMode();

    @PUT("Biometric/GetIdentifyEvent")
    Call<ResponseBody> GetIdentifyEvent();

    @PUT("Biometric/DeleteTemplates")
    Call<ResponseBody> DeleteTemplates(@Query("Count") String templateCountString, @Query("BadgeList") String badgeList);

    // Other examples:

    @PUT("Biometric/CaptureTemplate?Badge=1234")
    Call<ResponseBody> CaptureTemplate1234();

    // ------------------------------

    // --- Prox Examples ------------

    @GET("/prox?mode=oneshot")
    Call<ResponseBody> getProxOneshot();

    @GET("/prox?mode=wait")
    Call<ResponseBody> getProxwait();



    // ------------------------------

    // --- Keyboard Examples --------

    // Needs the streaming annotation to work
    @Streaming
    @GET("/keyboard?mode=stream")
    Call<ResponseBody> getKeyboardStream();

    // -------------------------------
}
