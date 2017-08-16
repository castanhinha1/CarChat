package ConfigClasses;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.parse.ParseUser;
import com.parse.starter.ViewControllers.LoginController;
import com.parse.starter.ViewControllers.NavigationController;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Dylan Castanhinha on 8/11/2017.
 */

public class LogoutButtonListener implements ImageButton.OnClickListener {

    private Context context;

    public LogoutButtonListener(Context context){
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setCancelText("Cancel")
                .setConfirmText("Logout")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        ParseUser.getCurrentUser().logOut();
                        sweetAlertDialog.cancel();
                        Intent intent = new Intent(getApplicationContext(), LoginController.class);
                        context.startActivity(intent);
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                    }
                })
                .show();
    }

}
