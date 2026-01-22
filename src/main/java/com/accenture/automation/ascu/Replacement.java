package com.accenture.automation.ascu;

public class Replacement {

    String currentStep;
    String newStep;
    String relativeXpath;
    boolean scroll;
    boolean skip;

    //Created object and initialized values to it and will be used while loading CSV data and Script processing
    Replacement(String currentStep, String newStep, String relativeXpath, boolean scroll, boolean skip) {
        this.currentStep = currentStep;
        this.newStep = newStep;
        this.relativeXpath = relativeXpath;
        this.scroll = scroll;
        this.skip = skip;
    }
}