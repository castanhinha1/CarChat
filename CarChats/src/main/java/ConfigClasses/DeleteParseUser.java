package ConfigClasses;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.starter.ViewControllers.LoginController;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Dylan Castanhinha on 8/15/2017.
 */

public class DeleteParseUser implements ImageButton.OnClickListener {

    private Context context;

    public DeleteParseUser(Context context){
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setCancelText("Cancel")
                .setConfirmText("Delete Account")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    ParseUser.getCurrentUser().logOut();
                                    Intent intent = new Intent(getApplicationContext(), LoginController.class);
                                    context.startActivity(intent);
                                }
                            }
                        });
                        sweetAlertDialog.cancel();
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
