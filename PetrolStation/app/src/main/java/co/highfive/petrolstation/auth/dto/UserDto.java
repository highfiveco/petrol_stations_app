package co.highfive.petrolstation.auth.dto;


import com.google.gson.annotations.SerializedName;

public class UserDto {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("no_print_copies")
    public Integer no_print_copies;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("email")
    public String email;

    @SerializedName("company_name")
    public String company_name;

    @SerializedName("platform")
    public String platform;

    @SerializedName("fcm_token")
    public String fcm_token;
}
