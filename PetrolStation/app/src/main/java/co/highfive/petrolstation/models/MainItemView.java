package co.highfive.petrolstation.models;

public class MainItemView {
    private int id;
    private int name;
    private int bg_color;
    private int icon;

    public MainItemView(int id, int name, int bg_color, int icon) {
        this.id = id;
        this.name = name;
        this.bg_color = bg_color;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getBg_color() {
        return bg_color;
    }

    public void setBg_color(int bg_color) {
        this.bg_color = bg_color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
