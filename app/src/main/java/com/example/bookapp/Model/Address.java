package com.example.bookapp.Model;

/**
 * Model tương ứng subcollection "users/{userId}/addresses"
 * Người dùng tự chọn địa chỉ khi đặt hàng, không có địa chỉ mặc định.
 */
public class Address {

    private String addressId;
    private String name;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;

    public Address() {
    }

    public Address(String addressId, String name, String phone, String province,
                    String district, String ward, String detailAddress) {
        this.addressId = addressId;
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = detailAddress;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    /**
     * Ghép thành 1 chuỗi địa chỉ đầy đủ để hiển thị UI.
     */
    public String getFullAddress() {
        return detailAddress + ", " + ward + ", " + district + ", " + province;
    }
}
