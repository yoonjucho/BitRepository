package com.example.bit_user.myapplication;

import java.net.URLDecoder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.PowerManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.app.Notification;
import android.util.Log;

/**
 * 푸시 메시지를 받는 Receiver 정의
 * 
 * @author Mike
 *
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {
	private static final String TAG = "GCMBroadcastReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {		//상대방이 메시지 보낼때  intent의 부가적인 정보로 사용
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
					sendToQNAActivity(context, from, command, type, data);
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

	private void sendToQNAActivity(Context context, String from, String command, String type, String data) {
		/*
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
			vibrator.vibrate(1000);
			QnAActivity.acquire(context, 10000);
			context.startActivity(intent);
			*/

		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);

		NotificationManager mNotificationManager  =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(context, QnAActivity.class);
		intent.putExtra("from", from);
		intent.putExtra("command", command);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //contentIntent?

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, QnAActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);//required
		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));
		mBuilder.setContentTitle(from);//required
		mBuilder.setContentText(data);//required
		mBuilder.setTicker("tickerText");//optional
		mBuilder.setNumber(10);//optional
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(1, mBuilder.build());
		vibrator.vibrate(1000); //1초 동안 진동
	}
}
