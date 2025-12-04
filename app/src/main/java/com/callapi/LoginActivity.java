package com.callapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.callapi.api.ApiService;
import com.callapi.model.User;
import com.callapi.model.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editPassword;
    private Button btnLogin;

    private List<User> mListUser;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editUserName = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);

        mListUser = new ArrayList<>();
        getListUser();

        btnLogin.setOnClickListener(this::clickLogin);
    }

    private void clickLogin(View view) {
        String strUsername = editUserName.getText().toString().trim();
        String strPassword = editPassword.getText().toString().trim();

        if (mListUser == null || mListUser.isEmpty()) {
            return;
        }
        boolean isHasUser = false;
        for (User user : mListUser) {
            if (strUsername.equals(user.getUsername()) && strPassword.equals(user.getPassword())) {
                isHasUser = true;
                mUser = user;
                break;


            }
        }

        if (isHasUser) {
            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("object_user", mUser);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Username or password invalid", Toast.LENGTH_SHORT).show();
        }
    }

    private void getListUser() {
        ApiService.apiService.getListUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                mListUser = response.body().getUsers();
                Log.e("List User", mListUser.size() + "");
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Call api error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}