package com.kisaan.kisaan_backend.dto;

public class FarmProfileRequest {
    private String soilType;
    private Double farmSize;
    private String cropsGrown;

    // Getters and Setters
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }

    public String getCropsGrown() { return cropsGrown; }
    public void setCropsGrown(String cropsGrown) { this.cropsGrown = cropsGrown; }
}