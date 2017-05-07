package com.google.firebase.codelab.friendlychat;

/**
 * Created by GaoBo on 5/7/2017.
 */

public class ImageInfo {
    public String name;
    public String text;
    public String imageUrl;
    public String photoUrl;
    public String timeStamp;
    public ImageInfo(String tName, String tText, String tImageUrl, String tPhotoUrl, String tTimeStamp) {
        this.name = tName;
        this.text = tText;
        this.imageUrl = tImageUrl;
        this.photoUrl = tPhotoUrl;
        this.timeStamp = tTimeStamp;
    }
}
