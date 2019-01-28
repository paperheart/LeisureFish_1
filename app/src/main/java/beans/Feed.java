package beans;

import com.google.gson.annotations.SerializedName;


public class Feed {

    @SerializedName("student_id")   String id;
    @SerializedName("user_name")    String name;
    @SerializedName("image_url")    String iurl;
    @SerializedName("video_url")    String vurl;


    public String getId()   { return id; }
    public void   setId()   {this.id = id;}

    public String getName() {return name;}
    public void   setName() {this.name = name;}

    public String getIurl() {return iurl;}
    public void setIurl(String iurl) { this.iurl = iurl; }

    public String getVurl() {return  vurl;}
    public void setVurl(String vurl) { this.vurl = vurl; }
}
