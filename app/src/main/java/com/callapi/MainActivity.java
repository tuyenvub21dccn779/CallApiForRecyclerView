package com.callapi;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.callapi.api.ApiService;
import com.callapi.api.Const;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    private static final int MY_REQUEST_CODE = 10;
    private EditText editPrivateKey;
    private ImageView imgFromGallery, imgFromApi;
    private Button btnSelectImage, btnUploadImage;
    private TextView tvFileId, tvFileName;
    private Uri mUsi;

    private ProgressDialog mProgressDialog;

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUsi = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imgFromGallery.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait ...");

        btnSelectImage.setOnClickListener(view -> {
            onClickRequestPermission();
        });

        btnUploadImage.setOnClickListener(view -> {
            if (mUsi != null) {
                callApiUploadImage();
            }
        });
    }



    private void initUi() {
        editPrivateKey = findViewById(R.id.edit_private_key);
        imgFromGallery = findViewById(R.id.img_from_gallery);
        imgFromApi = findViewById(R.id.img_from_api);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        tvFileId = findViewById(R.id.tv_file_id);
        tvFileName = findViewById(R.id.tv_file_name);
    }

    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission, MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    private void callApiUploadImage() {
        mProgressDialog.show();

        String strPrivateKey = editPrivateKey.getText().toString().trim();



        String strRealPath = RealPathUtil.getRealPath(this, mUsi);
        Log.e("Tincoder", strRealPath);
        File file = new File(strRealPath);
        RequestBody requestBodyAvt = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        MultipartBody.Part multipartBodyAvt = MultipartBody.Part.createFormData(Const.KEY_FILE, file.getName(), requestBodyAvt);

        RequestBody requestBodyFileName = RequestBody.create(file.getName(), MediaType.parse("multipart/form-data"));

        ApiService.apiService.uploadImage(
                        requestBodyFileName,
                        multipartBodyAvt,
                getBasicAutherization(strPrivateKey, "")
                ).enqueue(new Callback<FileResponse>() {
                    @Override
                    public void onResponse(Call<FileResponse> call, Response<FileResponse> response) {
                        mProgressDialog.dismiss();
                        FileResponse fileResponse = response.body();
                        if (fileResponse != null) {
                            tvFileId.setText(fileResponse.getFileId());
                            tvFileName.setText(fileResponse.getName());
                            Glide.with(MainActivity.this)
                                    .load(fileResponse.getUrl())
                                    .into(imgFromApi);
                        }
                    }

                    @Override
                    public void onFailure(Call<FileResponse> call, Throwable t) {
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Call api fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getBasicAuthericd zation(String username, String password) {
        String autherizationStr = Const.BASIC_AUTHORIZATION + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
        Log.e(MainActivity.class.getName(), autherizationStr);
        return autherizationStr;
    }
}