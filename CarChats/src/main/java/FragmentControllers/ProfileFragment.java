package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ConfigClasses.DeleteParseUser;
import ConfigClasses.MyProfilePictureView;
import Models.User;

import static android.app.Activity.RESULT_OK;


/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

public class ProfileFragment extends Fragment  {

    User currentUser;
    OnRowSelected activityCallBack;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    //ProfileView
    ListView listview;
    MyProfilePictureView profilepicture;
    CurrentDetailsAdapter adapter;
    Button deleteAccountButton;
    boolean buttonState;
    Button changePhotoButton;
    private List<String> mPath;


    public interface OnRowSelected{
        void onRowSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCallBack = (OnRowSelected) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("AppInfo", "Created");
        currentUser = (User) ParseUser.getCurrentUser();
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Profile");
        //Disable keyboard
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        //ProfileView
        currentUser = (User) ParseUser.getCurrentUser();
        profilepicture = (MyProfilePictureView) rootView.findViewById(R.id.profile_picture);
        deleteAccountButton = (Button) rootView.findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(new DeleteParseUser(getActivity()));
        buttonState = true;
        profilepicture.setImageBitmap(profilepicture.getRoundedBitmap(currentUser.getProfilePicture()));
        changePhotoButton = (Button) rootView.findViewById(R.id.change_photo_button);
        changePhotoButton.setOnClickListener(new ChangePhotoButtonListener());
        //User Details List View
        ArrayList<User> users = new ArrayList<User>();
        listview = (ListView) rootView.findViewById(R.id.profile_details_list_view);
        adapter = new CurrentDetailsAdapter(getActivity().getApplicationContext(), users);
        listview.setAdapter(adapter);
        for (int i = 0; i < 5; i++){
            adapter.add(currentUser);
        }

        return rootView;
    }

    public class ChangePhotoButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            new ImagePicker.Builder(getActivity())
                    .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                    .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                    .directory(ImagePicker.Directory.DEFAULT)
                    .extension(ImagePicker.Extension.PNG)
                    .scale(600, 600)
                    .allowMultipleImages(false)
                    .enableDebuggingMode(true)
                    .build();
        }
    }

    public class CurrentDetailsAdapter extends ArrayAdapter<User>{
        public CurrentDetailsAdapter(Context context, ArrayList<User> users){
            super(context,0, users);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            User user = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout_profile_details, parent, false);
            }
            final TextView details = (TextView) convertView.findViewById(R.id.profile_details_text_view);
            ImageButton button = (ImageButton) convertView.findViewById(R.id.profile_details_image_button);
            switch(position){
                case 0: {
                    details.setText(user.getFullName());
                    button.setImageResource(R.drawable.ic_user_buttpn);
                    break;
                }
                case 1: {
                    details.setText(user.getLocation());
                    button.setImageResource(R.drawable.ic_user_location);
                    break;
                }
                case 2: {
                    details.setText(user.getEmail());
                    button.setImageResource(R.drawable.ic_email_icon);
                    break;
                }
                case 3: {
                    details.setText(user.getPhoneNumber());
                    button.setImageResource(R.drawable.ic_phone_icon);
                    break;
                }
                case 4: {
                    details.setText(user.getLicensePlateState()+" #"+user.getLicensePlateNumber());
                    button.setImageResource(R.drawable.ic_car_button);
                    break;
                }
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onRowSelected(position);
                }
            });
            return convertView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mPath = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            setImage();
        }
    }

    public void setImage(){
        Bitmap bitmap = BitmapFactory.decodeFile(mPath.get(0));
        profilepicture.setImageBitmap(profilepicture.getRoundedBitmap(bitmap));
        //Save profile picture by converting it to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        currentUser.setProfilePicture(byteArray);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.i("AppInfo", "saved!");
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });
    }

}
