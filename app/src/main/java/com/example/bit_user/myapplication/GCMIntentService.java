package com.example.bit_user.myapplication;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URLDecoder;

public class GCMIntentService extends GCMBaseIntentService {

	public String phoneId;
	public String bundleId;
	String status;
	private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
	private static final String TAG = "GCMIntentService";
	public static final String SEND_ID = GCMInfo.PROJECT_ID;
	private static PowerManager.WakeLock sCpuWakeLock;

	public GCMIntentService() {
		this(GCMInfo.PROJECT_ID);
		Log.d("ggggg", SEND_ID.toString() + "/////GCMIntentService() called.");}

	public GCMIntentService(String project_id) { super(project_id); }

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
	protected void onMessage(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle b = intent.getExtras();

		Log.d(TAG,"?????????????SENDID????????????????????????"+SEND_ID);
		String from = intent.getStringExtra("from");
		String command = intent.getStringExtra("command");		// 서버에서 보낸 command 라는 키의 value 값
		String type = intent.getStringExtra("type");		// 서버에서 보낸 type 라는 키의 value 값
		String rawData = intent.getStringExtra("data");		// 서버에서 보낸 data 라는 키의 value 값
		String thisClass = intent.getStringExtra("class");
		String bundleId = intent.getStringExtra("bundleId");
		String data = "";
		try {
			data = URLDecoder.decode(rawData, "UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		if(thisClass.contains("qna")){
			Log.d(TAG, "GCMBoard qna");
			sendToQNAActivity(context, from, command, type, data, bundleId);
		}
		else if(thisClass.contains("notice")){
			Log.d(TAG, "GCMBoard notice");
			sendToNOTICEActivity(context, from, command, type, data, bundleId);
		}
		else if(thisClass.contains("vote")){
			Log.d(TAG, "GCMBoard notice");
			sendToVOTEActivity(context, from, command, type, data, bundleId);
		}
	}

	private void sendToVOTEActivity(Context context, String from, String command, String type, String data, String bundleId) {
		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		NotificationManager mNotificationManager  =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(context, VoteListStudent.class);
		intent.putExtra("from", from);
		intent.putExtra("command", command);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
		intent.putExtra("bundleId", bundleId);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //contentIntent?

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);//required
		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));
		mBuilder.setContentTitle("투표");//required
		mBuilder.setContentText(data);//required
		mBuilder.setNumber(10);//optional
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		// 잠든 단말을 깨워라.
		acquireCpuWakeLock(context);
		// WakeLock 해제.
		releaseCpuLock();

		mNotificationManager.notify(1, mBuilder.build());
		vibrator.vibrate(1000); //1초 동안 진동
	}

	private void sendToNOTICEActivity(Context context, String from, String command, String type, String data, String bundleId) {
		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		NotificationManager mNotificationManager  =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(context, NoticeStudentActivity.class);
		intent.putExtra("from", from);
		intent.putExtra("command", command);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
		intent.putExtra("bundleId", bundleId);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //contentIntent?

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);//required
		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));
		mBuilder.setContentTitle("공지사항");//required
		mBuilder.setContentText(data);//required
		mBuilder.setNumber(10);//optional
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		// 잠든 단말을 깨워라.
		acquireCpuWakeLock(context);
		// WakeLock 해제.
		releaseCpuLock();

		mNotificationManager.notify(1, mBuilder.build());
		vibrator.vibrate(1000); //1초 동안 진동
	}

	private void sendToQNAActivity(Context context, String from, String command, String type, String data, String bundleId) {
		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		NotificationManager mNotificationManager  =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(context, MessageTeacher.class);
		intent.putExtra("from", from);
		intent.putExtra("command", command);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
		intent.putExtra("bundleId", bundleId);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //contentIntent?

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);//required
		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));
		mBuilder.setContentTitle("질문");//required
		mBuilder.setContentText(data);//required
		mBuilder.setNumber(10);//optional
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		// 잠든 단말을 깨워라.
		acquireCpuWakeLock(context);
		// WakeLock 해제.
		releaseCpuLock();

		mNotificationManager.notify(1, mBuilder.build());
		vibrator.vibrate(1000); //1초 동안 진동
	}

	/**에러 발생시*/
	@Override
	protected void onError(Context context, String errorId) {
		Log.d(TAG, "onError. errorId : " + errorId);
	}
	/**단말에서 GCM 서비스 등록 했을 때 등록 id를 받는다*/
	@Override
	protected void onRegistered(Context context, String regId) {
		Log.d(TAG, "onRegistered. regId : " + regId);
		//GCMRegistrar.register(context, regId);
	}
	/**단말에서 GCM 서비스 등록 해지를 하면 해지된 등록 id를 받는다*/
	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d(TAG, "onUnregistered. regId : "+regId);
	}
}