package co.highfive.petrolstation.pos.data;

import java.util.List;

import co.highfive.petrolstation.pos.dto.PosItemDto;

public class PosItemsPaging {
    public int current_page;
    public List<PosItemDto> data;
    public int last_page;
    public int per_page;
    public int total;
}