package beans;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class FeedResponse {

    @SerializedName("feeds")
    private List<Feed> feedList = new ArrayList<Feed>();

    @SerializedName("success")
    boolean success;


    public void setSuccest()    {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }


    public void setFeedList(List<Feed> feedList) {
        this.feedList = feedList;
    }

    public List<Feed> getFeedList() {
        return feedList;
    }
}
