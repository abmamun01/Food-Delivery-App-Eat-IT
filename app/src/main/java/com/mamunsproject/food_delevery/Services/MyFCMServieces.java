package com.mamunsproject.food_delevery.Services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mamunsproject.food_delevery.Common.Common;

import java.util.Map;
import java.util.Random;

public class MyFCMServieces extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Map<String, String> dataRecieve = remoteMessage.getData();
        if (dataRecieve != null) {
            Common.showNotification(this, new Random().nextInt(),
                    dataRecieve.get(Common.NOTIFIACTION_TITLE),
                    dataRecieve.get(Common.NOTIFICATION_CONTENT),
                    null);
        }
    }


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(getApplicationContext(),s);
    }
}
