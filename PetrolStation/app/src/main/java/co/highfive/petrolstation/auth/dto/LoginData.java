package co.highfive.petrolstation.auth.dto;


import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("user")
    public UserDto user;

    @SerializedName("token")
    public String token; // مثال: "Bearer 7|...."

    @SerializedName("setting")
    public SettingDto setting;
}
