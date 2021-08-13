package Com.mariapublishers.mariaexecutive;

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;

        if (data != null) {
            try {
                if(LoginSharedPreference.getLoggedStatus(App.getContext())) {
                    Intent intent = new Intent(App.getContext(), MenuDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    App.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(App.getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    App.getContext().startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //If we send notification with action buttons we need to specidy the button id's and retrieve it to
        //do the necessary operation.
        if (actionType == OSNotificationAction.ActionType.ActionTaken) {

        }
    }
}
