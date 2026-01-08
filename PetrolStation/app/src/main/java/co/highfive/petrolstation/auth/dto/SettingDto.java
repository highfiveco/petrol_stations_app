package co.highfive.petrolstation.auth.dto;

import com.google.gson.annotations.SerializedName;

public class SettingDto {
    @SerializedName("id")
    public int id;

    @SerializedName("financial_safe")
    public Integer financial_safe;

    @SerializedName("name")
    public String name;

    @SerializedName("url_app")
    public String url_app;

    @SerializedName("logo_size")
    public Integer logo_size;

    @SerializedName("version_app")
    public String version_app;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("image")
    public String image;

    @SerializedName("code")
    public String code;

    @SerializedName("no_print_copies")
    public Integer no_print_copies;
}
