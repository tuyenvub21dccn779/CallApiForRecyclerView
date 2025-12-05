package com.callapi.api;

import com.callapi.FileResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    public static final String DOMAIN = "https://upload.imagekit.io/api/v1/";

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy MM dd HH:mm:ss")
            .create();

    ApiService apiService = new Retrofit.Builder()
            .baseUrl(DOMAIN)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);



    @Multipart
    @POST("files/upload")
    Call<FileResponse> uploadImage(
            @Part (Const.KEY_FILENAME) RequestBody fileName,
            @Part MultipartBody.Part file,
            @Header(Const.KEY_AUTHORIZATION) String authorization
            );
}
