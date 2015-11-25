package com.example.bit_user.myapplication;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends IntentService {

	private static final String TAG = "GCMIntentService";

	/**
	 * 생성자
	 */
    public GCMIntentService() {
        super(TAG);

        Log.d(TAG, "GCMIntentService() called.");
    }
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		//here call your methods to send the registrationId to your webserver
 		GCMRegistrar.register(context, registrationId); }

    /*
     * 전달받은 인텐트 처리
     */
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		
		Log.d(TAG, "action : " + action);
		
	}

}