package com.smartpost.entities;

public class PostManClientMap {

    private String emailId;
    private String uuid;
    private String address;
    private String consignmentId;
    private String phone;

    private ReceiverDetails details;



    public PostManClientMap(){

    }



    public PostManClientMap(String emailId, String uuid) {
        this.emailId = emailId;
        this.uuid = uuid;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public boolean isBelongToClient(String email){
        return this.emailId.equalsIgnoreCase(email);
    }

    public String getConsignmentId() {
        return consignmentId;
    }

    public void setConsignmentId(String consignmentId) {
        this.consignmentId = consignmentId;
    }

    @Override
    public String toString() {
        return "PostManClientMap{" +
                "emailId='" + emailId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", address='" + address + '\'' +
                ", consignmentId='" + consignmentId + '\'' +
                '}';
    }

    public boolean isBelongToPostman(String uuid){
        return  this.uuid.equalsIgnoreCase(uuid);
    }

    public ReceiverDetails getDetails() {
        return details;
    }

    public void setDetails(ReceiverDetails details) {
        this.details = details;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
