package co.highfive.petrolstation.pos.dto;

public class PosAddItemDto {
    public int itemId;
    public double price;
    public double count;

    public PosAddItemDto(int itemId, double price, double count) {
        this.itemId = itemId;
        this.price = price;
        this.count = count;
    }
}
