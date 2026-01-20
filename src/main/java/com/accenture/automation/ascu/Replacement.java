package com.accenture.automation.ascu;

public class Replacement {

    String currentStep;
    String newStep;
    String relativeXpath;

    //Created object and initialized values to it and will be used while loading CSV data and Script processing
    Replacement(String currentStep, String newStep, String relativeXpath) {
        this.currentStep = currentStep;
        this.newStep = newStep;
        this.relativeXpath = relativeXpath;
    }
}