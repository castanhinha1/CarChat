package FragmentControllers;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;


import Models.User;

public class EditProfileFragment extends Fragment {

    User currentUser;
    int positionFromProfile;
    EditText details;
    EditText licensePlate;
    Spinner stateSpinner;
    String[] statesArray;
    ArrayAdapter<String> dataAdapter;
    LinearLayout stateLinearLayout;
    String item;
    ImageButton button;
    ImageButton clearTextButton;

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private Button toolbarButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarButton = (Button) toolbar.findViewById(R.id.toolbar_right_button_text);
        toolbarButton.setText("Done");
        toolbarButton.setVisibility(View.VISIBLE);
        toolbarButton.setOnClickListener(new DoneButtonClickListener());
        positionFromProfile = getArguments().getInt("position");
        currentUser = (User) ParseUser.getCurrentUser();
        Log.i("AppInfo", "Position: "+positionFromProfile);

        //Edit Details
        details = (EditText) rootView.findViewById(R.id.profile_details_edit_text);
        stateLinearLayout = (LinearLayout) rootView.findViewById(R.id.license_plate_linear_layout);
        stateSpinner = (Spinner) rootView.findViewById(R.id.statespinner);
        licensePlate = (EditText) rootView.findViewById(R.id.license_plate_edit_text);
        licensePlate.setOnFocusChangeListener(new EditTextEditing());
        licensePlate.setOnEditorActionListener(new ListenForDonePressed());
        stateSpinner.setOnItemSelectedListener(new StateSelected());
        button = (ImageButton) rootView.findViewById(R.id.profile_details_image_button);
        details.setOnFocusChangeListener(new EditTextEditing());
        details.setOnEditorActionListener(new ListenForDonePressed());
        clearTextButton = (ImageButton) rootView.findViewById(R.id.clear_text_button);
        clearTextButton.setOnClickListener(new ClearTextListener());
        getStatesData();
        getSelectedDetail();

        //Change navigation of back button to popback stack
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    private class StateSelected implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            item = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class ListenForDonePressed implements EditText.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                toolbarButton.performClick();
                return true;
            }
            return false;
        }
    }

    private class ClearTextListener implements ImageButton.OnClickListener {

        @Override
        public void onClick(View v) {
            details.setText("");
            licensePlate.setText("");
        }
    }

    private class EditTextEditing implements EditText.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            clearTextButton.setVisibility(View.VISIBLE);
        }
    }

    public void getStatesData() {

        statesArray = getResources().getStringArray(R.array.states);

        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, statesArray);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        stateSpinner.setAdapter(dataAdapter);
    }

    public void getSelectedDetail(){
        switch(positionFromProfile){
            case 0: {
                stateLinearLayout.setVisibility(View.INVISIBLE);
                details.setText(currentUser.getFullName());
                toolbarTitle.setText("Edit Name");
                button.setImageResource(R.drawable.ic_user_buttpn);
                break;
            }
            case 1: {
                stateLinearLayout.setVisibility(View.INVISIBLE);
                details.setText(currentUser.getLocation());
                toolbarTitle.setText("Edit Location");
                button.setImageResource(R.drawable.ic_user_location);
                break;
            }
            case 2: {
                stateLinearLayout.setVisibility(View.INVISIBLE);
                details.setText(currentUser.getEmail());
                toolbarTitle.setText("Edit Email");
                button.setImageResource(R.drawable.ic_email_icon);
                break;
            }
            case 3: {
                stateLinearLayout.setVisibility(View.INVISIBLE);
                details.setText(currentUser.getPhoneNumber());
                details.setInputType(InputType.TYPE_CLASS_NUMBER);
                toolbarTitle.setText("Edit Number");
                button.setImageResource(R.drawable.ic_phone_icon);
                break;
            }
            case 4: {
                stateLinearLayout.setVisibility(View.VISIBLE);
                licensePlate.setText(currentUser.getLicensePlateNumber());
                if (currentUser.getLicensePlateState() != null) {
                    if (!currentUser.getLicensePlateState().equals("")) {
                        int spinnerpostion = dataAdapter.getPosition(currentUser.getLicensePlateState());
                        stateSpinner.setSelection(spinnerpostion);
                    }
                } else {
                    Log.i("AppInfo", "Called!");
                }
                toolbarTitle.setText("Edit License Plate");
                button.setImageResource(R.drawable.ic_car_button);
                break;
            }
        }
    }


    public class DoneButtonClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (positionFromProfile) {
                case 0: {
                    currentUser.setFullName(details.getText().toString());
                    break;
                }
                case 1: {
                    currentUser.setLocation(details.getText().toString());
                    break;
                }
                case 2: {
                    currentUser.setEmail(details.getText().toString());
                    break;
                }
                case 3: {
                    currentUser.setPhoneNumber(details.getText().toString());
                    break;
                }
                case 4: {
                    currentUser.setLicensePlateNumber(licensePlate.getText().toString());
                    currentUser.setLicensePlateState(item);
                }
            }
            saveUser();
        }
    }
    public void saveUser(){

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    toolbarButton.setVisibility(View.INVISIBLE);
                    getFragmentManager().popBackStack();
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });

    }
}
