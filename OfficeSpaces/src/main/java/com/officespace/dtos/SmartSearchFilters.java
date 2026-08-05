package com.officespace.dtos;

import lombok.Data;

@Data
public class SmartSearchFilters {
    private String city;
    private String propertyType;
    private String listingType;
    private Double maxPrice;
    private Double minArea;
}