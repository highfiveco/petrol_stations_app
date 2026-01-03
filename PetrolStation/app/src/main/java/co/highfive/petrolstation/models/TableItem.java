package co.highfive.petrolstation.models;

public class TableItem {
    private String[] text;
    private int[] width;
    private int[] align;

    public TableItem( String[] text,int[] width,int[] align) {
//        text = new String[]{"test","test","test"};
//        width = new int[]{1,1,1};
//        align = new int[]{0,0,0};
        this.text=text;
        this.width=width;
        this.align=align;
    }

    public String[] getText() {
        return text;
    }

    public void setText(String[] text) {
        this.text = text;
    }

    public int[] getWidth() {
        return width;
    }

    public void setWidth(int[] width) {
        this.width = width;
    }

    public int[] getAlign() {
        return align;
    }

    public void setAlign(int[] align) {
        this.align = align;
    }
}
