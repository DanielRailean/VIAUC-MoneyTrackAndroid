package com.ddlele.moneytrack.Model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ddlele.moneytrack.Networking.ServiceGenerator;
import com.ddlele.moneytrack.Networking.UserAPI;
import com.ddlele.moneytrack.Util.LocalStorage;
import com.ddlele.moneytrack.Wrappers.ApiResponses.JWT;
import com.ddlele.moneytrack.Wrappers.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private static UserRepository instance;
    private UserAPI userAPI;

    private MutableLiveData<JWT> accessToken;
    private MutableLiveData<JWT> refreshToken;

    private String email;
    private String pass;

    private UserRepository(){
        userAPI = ServiceGenerator.getUserAPI();
        refreshToken = new MutableLiveData<>();
        accessToken = new MutableLiveData<>();

        accessToken.setValue(new JWT("loading"));
//        refreshToken.setValue(new JWT(LocalStorage.getInstance().get("refreshToken")));
//        refresh();
        email = LocalStorage.getInstance().get("email");
        pass = LocalStorage.getInstance().get("pass");
        login(new User(email,pass));
    }

    public static UserRepository getInstance(){
        if(instance==null){
            instance = new UserRepository();
        }
        return instance;
    }

    public LiveData<JWT> getToken(){
        if (accessToken == null)
            refresh();
        return accessToken;
    }

    public void login(User user){
        LocalStorage.getInstance().set("email",user.getEmail());
        LocalStorage.getInstance().set("pass",user.getPassword());

        Call<JWT> call = userAPI.login(user);
        call.enqueue(new Callback<JWT>() {
            @Override
            public void onResponse(Call<JWT> call, Response<JWT> response) {
                if(response.code() == 200){
                    LocalStorage.getInstance().set("access_token", response.body().getToken());
                    accessToken.setValue(response.body());
                }else{
                    accessToken.setValue(new JWT("empty"));
                }
            }

            @Override
            public void onFailure(Call<JWT> call, Throwable t) {
                accessToken.setValue(new JWT("empty"));
            }
        });

    }

    public void logout(){
        accessToken.setValue(new JWT("empty"));
        LocalStorage.getInstance().set("access_token", "empty");
        LocalStorage.getInstance().set("email","clear");
        LocalStorage.getInstance().set("pass","clear");
    }

    public void register(User user){
        Call<User> call = userAPI.register(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    login(user);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.i("api call", "Error");
            }
        });

    }

    private void refresh(){
        accessToken.setValue(new JWT("empty"));
    }
}
