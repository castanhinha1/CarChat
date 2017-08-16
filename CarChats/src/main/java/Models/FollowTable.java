package Models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

@ParseClassName("FollowTable")
public class FollowTable extends ParseObject {

    public FollowTable() {
        super();
    }

    public User getFollowing() {
        return (User) getParseUser("following");
    }
    public void setFollowing(User value) {
        put("following", value);
    }

    public User getIsFollowed() {
        return (User) getParseUser("isFollowed");
    }
    public void setIsFollowed(User value) {
        put("isFollowed", value);
    }

    public Date getExpirationDate(){
        return (Date) get("expirationDate");
    }
    public void setExpirationDate(Date value){
        put("expirationDate", value);
    }

    public boolean getRequestConfirmed(){return (boolean) get("requestConfirmed");}
    public void setRequestConfirmed(boolean value) { put("requestConfirmed", value);}

}
