package com.mamunsproject.food_delevery.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.mamunsproject.food_delevery.Model.AddonModel;
import com.mamunsproject.food_delevery.Model.CategoryModel;
import com.mamunsproject.food_delevery.Model.FoodModel;
import com.mamunsproject.food_delevery.Model.SizeModel;
import com.mamunsproject.food_delevery.Model.TokenModel;
import com.mamunsproject.food_delevery.Model.UserModel;
import com.mamunsproject.food_delevery.R;
import com.mamunsproject.food_delevery.Services.MyFCMServieces;

import java.math.RoundingMode;
import java.sql.BatchUpdateException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {


    public static final String USER_REFERENCES = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEALS_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Orders";
    public static final String NOTIFIACTION_TITLE = "title";
    public static final String NOTIFICATION_CONTENT = "content";
    private static final String TOKEN_REF = "Tokens";
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static String currentToken="";
    public static String authorizeKey="";



    public static String formatPrice(double price) {

        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".", ",");
        } else
            return "0,00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {

        Double result = 0.0;
        if (userSelectedSize == null && userSelectedAddon == null)
            return 0.0;
        else if (userSelectedSize == null) {
            //if userselectedAddon !=null, we need sum price
            for (AddonModel addonModel : userSelectedAddon)
                result += addonModel.getPrice();
            return result;
        } else if (userSelectedAddon == null) {
            return userSelectedSize.getPrice() * 1.0;
        } else {

            //If both size and addon is selected
            result = userSelectedSize.getPrice() * 1.0;

            for (AddonModel addonModel : userSelectedAddon)
                result += addonModel.getPrice();
            return result;

        }

    }

    public static void setSpanString(String welcome, String name, TextView textView) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        builder.append(spannableStringBuilder);
        spannableStringBuilder.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String createOrderNumber() {

        return new StringBuilder()
                .append(System.currentTimeMillis()) //Get Current time millisecond
                .append(Math.abs(new Random().nextInt())) //Add random number to block same order ata same time
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i) {

            case 1:
                return "Monday";

            case 2:
                return "Thuseday";

            case 3:
                return "Wednesday";

            case 4:
                return "Thursday";

            case 5:
                return "Friday";

            case 6:
                return "Saturday";

            case 7:
                return "Sunday";

            default:
                return "Unk";

        }
    }


    public static String convertStatusToText(int orderStatus) {

        switch (orderStatus) {

            case 0:
                return "Placed";

            case 1:
                return "Shipping";

            case 2:
                return "Shipped";

            case -1:
                return "Cancelled";

            default:
                return "Unk";
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {

        PendingIntent pendingIntent=null;

        if (intent!=null){
            pendingIntent=PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT );

            String NOTIFICATION_CHANNEL_ID="mamuns_food_delivery_app";
            NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

                NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "Food Delivery ",NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription("Food Delivery");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);


                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder builder=new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_restaurant_menu_24));


            if (pendingIntent!=null)
                builder.setContentIntent(pendingIntent);

            Notification notification=builder.build();
            notificationManager.notify(id,notification);


        }



    }

    public static void updateToken(Context context,String newToken) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentUser.getUid())
                .setValue(new TokenModel(Common.currentUser.getPhone(),newToken))
                .addOnFailureListener(e ->
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
