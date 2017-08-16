package ConfigClasses;

import android.content.Intent;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.parse.starter.ViewControllers.NavigationController;
import com.parse.starter.ViewControllers.WeightManagementMain;

import org.json.JSONObject;

/**
 * Created by Dylan Castanhinha on 8/9/2017.
 */

public class OneSignalNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;
        String fragmentToBeOpened;

        if (data != null) {
            fragmentToBeOpened = data.optString("intent", null);
            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
            if (fragmentToBeOpened != null && fragmentToBeOpened.equals("profile")) {
                Intent intent = new Intent(WeightManagementMain.getContext(), NavigationController.class);
                intent.putExtra("notification", "profilefragment");
                WeightManagementMain.getContext().startActivity(intent);
            } else{
                Intent intent = new Intent(WeightManagementMain.getContext(), NavigationController.class);
                WeightManagementMain.getContext().startActivity(intent);
            }
        } else {
            Intent intent = new Intent(WeightManagementMain.getContext(), NavigationController.class);
            WeightManagementMain.getContext().startActivity(intent);
        }
    }
}
