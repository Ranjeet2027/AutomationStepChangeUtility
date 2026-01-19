package com.accenture.automation.ascu;

public class Replacement {

    String oldStep;
    String newStep;
    String xpath;

    //Created object and initialized values to it and will be used while loading CSV data and Script processing
    Replacement(String oldStep, String newStep, String xpath) {
        this.oldStep = oldStep;
        this.newStep = newStep;
        this.xpath = xpath;
    }
}