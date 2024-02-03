package com.example.productservice.dto;

import com.example.productservice.enums.*;

import java.util.List;

public record FilterProductDTO(Integer limit, Integer offset,
                               List<String> brand, List<Connectivity> connectivity, List<Switches> keySwitches,
                               List<KeyCaps> keyCaps, List<Layout> layout, List<Category> category,
                               Float minPrice, Float maxPrice, SortType sortType
                               ) {
}
