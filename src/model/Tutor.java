package model;

import java.math.BigDecimal;

public class Tutor {
    private int tutorId;
    private int userId;
    private BigDecimal pricePerHour;
    private String experience;
    private String status;
    private Integer provinceId;
    private String provinceName;
    private boolean isApproved;
    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
    public Tutor() {
    }

    public Tutor(int userId, BigDecimal pricePerHour, String experience) {
        this.userId = userId;
        this.pricePerHour = pricePerHour;
        this.experience = experience;
        this.status = "AVAILABLE";
    }
    public int getTutorId() {
        return tutorId;
    }
    public void setTutorId(int tutorId) {
        this.tutorId = tutorId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }
    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
    public String getExperience() {
        return experience;
    }
    public void setExperience(String experience) {
        this.experience = experience;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Integer getProvinceId() {
        return provinceId;
    }
    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }
    public String getProvinceName() {
        return provinceName;
    }
    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
    @Override
    public String toString() {
        return "Tutor{" +
                "tutorId=" + tutorId +
                ", userId=" + userId +
                ", pricePerHour=" + pricePerHour +
                ", experience='" + experience + '\'' +
                ", status='" + status + '\'' +
                ", provinceId=" + provinceId +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }
}
