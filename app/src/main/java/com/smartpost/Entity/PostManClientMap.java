package com.smartpost.Entity;

public class PostManClientMap {

    private String emailId;
    private String uuid;
    private String address;
    private String consignmentId;



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
}
