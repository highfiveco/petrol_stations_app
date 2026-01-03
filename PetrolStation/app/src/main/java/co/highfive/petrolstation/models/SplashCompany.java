package co.highfive.petrolstation.models;

public class SplashCompany {
    private String id;
    private String title;
    private String details;
    private String image;

    public SplashCompany(String details) {
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
