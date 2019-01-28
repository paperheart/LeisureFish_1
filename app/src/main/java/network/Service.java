package network;

import android.support.v4.provider.FontsContractCompat;

import beans.FeedResponse;
import beans.PostVideoResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface Service {
    @POST("minidouyin/video/")
    @Multipart
    Call<PostVideoResponse> creatVideo(
            @Query("student_id") String id,
            @Query("user_name")  String name,
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2
    );


    @GET("minidouyin/feed/")
    Call<FeedResponse> getFeed();
}
