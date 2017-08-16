package ConfigClasses;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.starter.R;

import org.w3c.dom.Text;

import Models.User;

/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public abstract class ParseAdapterCustomList extends ParseQueryAdapter<User> {

    public ParseAdapterCustomList(Context context, Class<? extends ParseObject> clazz) {
        super(context, clazz);
    }

    public ParseAdapterCustomList(Context context, String className) {
        super(context, className);
    }

    public ParseAdapterCustomList(Context context, Class<? extends ParseObject> clazz, int itemViewResource) {
        super(context, clazz, itemViewResource);
    }

    public ParseAdapterCustomList(Context context, String className, int itemViewResource) {
        super(context, className, itemViewResource);
    }

    public ParseAdapterCustomList(Context context, QueryFactory<User> queryFactory) {
        super(context, queryFactory);
    }

    public ParseAdapterCustomList(Context context, QueryFactory<User> queryFactory, int itemViewResource) {
        super(context, queryFactory, itemViewResource);
    }
}
