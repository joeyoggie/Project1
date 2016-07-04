package com.example.android.project1;

/**
 * Created by Joey on 7/1/2016.
 */
public class ImageInfo {
    private long ImageID;
    private String senderUserName;
    private String recepientUserName;
    private String timestamp;
    private String imageFilePath;
    /*Bitmap imageContent;
    byte[] imageData;*/

    public void setImageID(long imageID) {
        ImageID = imageID;
    }
    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }
    public void setRecepientUserName(String recepientUserName) {
        this.recepientUserName = recepientUserName;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public void setimageFilePath(String imageFileName) {
        this.imageFilePath = imageFileName;
    }
    /*public void setImageContent(Bitmap imageContent) {
        this.imageContent = imageContent;
    }
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }*/

    public long getImageID() {
        return ImageID;
    }
    public String getSenderUserName() {
        return senderUserName;
    }
    public String getRecepientUserName() {
        return recepientUserName;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getimageFilePath() {
        return imageFilePath;
    }
    /*public Bitmap getImageContent() {
        return imageContent;
    }
    public byte[] getImageData() {
        return imageData;
    }*/
}
