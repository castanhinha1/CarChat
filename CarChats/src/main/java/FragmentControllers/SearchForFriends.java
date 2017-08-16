package FragmentControllers;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchForFriends extends Fragment {

    User currentUser;
    SearchView searchView;
    //ListView
    TextView labeltv;
    ListView listView;
    SearchForFriendsAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String querySearch = "0";
    TextView emptylistview;

    OnUserSelected activityCallBack;

    public interface OnUserSelected {
        public void onUserSelected(String userId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        try {
            activityCallBack = (OnUserSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnUserSelected");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_search_for_friends, container, false);
        getActivity().invalidateOptionsMenu();
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        final TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Search for Friends");
        ImageButton backButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        backButton.setVisibility(View.INVISIBLE);
        Button cancelButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        cancelButton.setText("Cancel");
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(new CancelButtonListener());
        Button sendButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        sendButton.setVisibility(View.INVISIBLE);
        //Hide bottom navigation view
        BottomNavigationView bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.INVISIBLE);
        //SearchView
        searchView = (SearchView) rootview.findViewById(R.id.search_view_friends);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchTextChanged());
        //Listview
        emptylistview = (TextView) rootview.findViewById(R.id.emptyTextViewSearch);
        swipeRefreshLayout = (SwipeRefreshLayout) rootview.findViewById(R.id.swipeContainer);
        listView = (ListView) rootview.findViewById(R.id.search_friends_list_view);
        listView.setEmptyView(emptylistview);
        adapter = new SearchForFriendsAdapter(getActivity());
        listView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeToRefresh());
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light);

        return rootview;
    }

    private class SearchTextChanged implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            querySearch = query;
            adapter.loadObjects();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            querySearch = newText;
            adapter.loadObjects();
            return false;
        }
    }

    private class CancelButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            hideKeyboard(getContext());
            getFragmentManager().popBackStack();
        }
    }

    //ListView methods

    private class SwipeToRefresh implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            adapter.loadObjects();
        }
    }

    private class SearchForFriendsAdapter extends ParseAdapterCustomList implements ParseQueryAdapter.OnQueryLoadListener {
        private SearchForFriendsAdapter(final Context context){
            super(context, new ParseQueryAdapter.QueryFactory<User>(){
                public ParseQuery<User> create() {
                    ParseQuery<User> query = ParseQuery.getQuery("_User");
                    query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                    query.setLimit(20);
                    query.whereStartsWith("fullname", querySearch);
                    return query;
                }
            });
            addOnQueryLoadListener(this);
        }

        @Override
        public void onLoading() {
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoaded(List objects, Exception e) {
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public View getItemView(final User user, View v, ViewGroup parent){
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_search_friends, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            TextView nameTextView = (TextView) v.findViewById(R.id.search_friends_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the Location label
            TextView location = (TextView) v.findViewById(R.id.search_friends_object_id);
            location.setText(user.getLocation());

            //Add the distance label

            //Add the image
            CircleImageView imageView = (CircleImageView) v.findViewById(R.id.imageView4);
            imageView.setImageBitmap(user.getProfilePicture());

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onUserSelected(user.getObjectId());
                }
            });
            return v;
        }
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}

