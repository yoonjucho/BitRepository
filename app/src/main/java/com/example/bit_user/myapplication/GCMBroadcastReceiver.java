package com.example.bit_user.myapplication;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.net.URLDecoder;

/**
 * 푸시 메시지를 받는 Receiver 정의
 * @author Mike
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {
	private static final String TAG = "GCMBroadcastReceiver";
	private static PowerManager.WakeLock sCpuWakeLock;
	private static KeyguardManager.KeyguardLock mKeyguardLock;
	private static boolean isScreenLock;

	public static void acquireCpuWakeLock(Context context) {
		Log.e("PushWakeLock", "Acquiring cpu wake lock");
		Log.e("PushWakeLock", "wake sCpuWakeLock = " + sCpuWakeLock);
		if (sCpuWakeLock != null) {
			return;
		}
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP |
						PowerManager.ON_AFTER_RELEASE, "hello");
		sCpuWakeLock.acquire();
	}

	public static void releaseCpuLock() {
		Log.e("PushWakeLock", "Releasing cpu wake lock");
		Log.e("PushWakeLock", "relase sCpuWakeLock = " + sCpuWakeLock);
		if (sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {		//상대방이 메시지 보낼때  intent의 부가적인 정보로 사용

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

		String action = intent.getAction();
		Log.d(TAG, "action : " + action);
		
		if (action != null) {
			if (action.equals("com.google.android.c2dm.intent.RECEIVE")) { // 푸시 메시지 수신 시
				String from = intent.getStringExtra("from");
				String command = intent.getStringExtra("command");		// 서버에서 보낸 command 라는 키의 value 값 
				String type = intent.getStringExtra("type");		// 서버에서 보낸 type 라는 키의 value 값
				String rawData = intent.getStringExtra("data");		// 서버에서 보낸 data 라는 키의 value 값
				String thisClass = intent.getStringExtra("class");
                String data = "";
                try {
					data = URLDecoder.decode(rawData, "UTF-8");
				} catch(Exception ex) {
					ex.printStackTrace();
				}

				Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data + ", class : "+thisClass);

				if(thisClass.contains("gcm")) {
					Log.d(TAG, "GCMBoard gcm");
					sendToGCMActivity(context, from, command, type, data);
				}
				else if(thisClass.contains("qna")){
					Log.d(TAG, "GCMBoard qna");
					//sendToQNAActivity(context, from, command, type, data);
				}
				else if(thisClass.contains("notice")){
					Log.d(TAG, "GCMBoard notice");
					sendToNOTICEActivity(context, from, command, type, data);
				}
				else if(thisClass == null){
					Log.d(TAG, "GCMBoard null");
				}
				else{
					Log.d(TAG, "GCMBoard error");
				}
			} else {
				Log.d(TAG, "Unknown action : " + action);
			}
		} else {
			Log.d(TAG, "action is null.");
		}
		
	}

	/**
	 * @param context
	 * @param command
	 * @param type
	 * @param data
	 */

	private void sendToGCMActivity(Context context, String from, String command, String type, String data) {
            Intent intent = new Intent(context, GCMPush.class);
			intent.putExtra("from", from);
			intent.putExtra("command", command);
			intent.putExtra("type", type);
			intent.putExtra("data", data);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
			vibrator.vibrate(1000);
			GCMPush.acquire(context, 10000);
			context.startActivity(intent);
	}

	private void sendToNOTICEActivity(Context context, String from, String command, String type, String data) {
		/*
		Intent intent = new Intent(context, GCMPush.class);
		intent.putExtra("from", from);
		intent.putExtra("command", command);
		intent.putExtra("type", type);
		intent.putExtra("data", data);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		vibrator.vibrate(1000);
		GCMPush.acquire(context, 10000);
		context.startActivity(intent);
		*/
	}

}