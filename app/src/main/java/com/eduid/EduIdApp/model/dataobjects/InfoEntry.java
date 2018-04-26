package com.eduid.EduIdApp.model.dataobjects;

import android.icu.text.IDNA;

public class InfoEntry {

    private String titleText;
    private String contentText;

    public InfoEntry(String titleText, String contentText){
        this.titleText = titleText;
        this.contentText = contentText;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
}
