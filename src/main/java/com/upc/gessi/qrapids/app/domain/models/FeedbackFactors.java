package com.upc.gessi.qrapids.app.domain.models;

import java.util.List;

public class FeedbackFactors {
    private Long siId;
    private String siName;
    private String date;
    private List<String> fact;
    private List<Float> factVal;
    private String author;
    private float oldvalue;
    private String oldCategory;
    private String oldCategoryColor;
    private float newvalue;
    private String newCategory;
    private String newCategoryColor;

    public FeedbackFactors(Long siId, String siName, String date, List<String> fact, List<Float> factVal, String author, float oldvalue, String oldCategory, String oldCategoryColor, float newvalue, String newCategory, String newCategoryColor) {
        this.siId = siId;
        this.siName = siName;
        this.date = date;
        this.fact = fact;
        this.factVal = factVal;
        this.author = author;
        this.oldvalue = oldvalue;
        this.oldCategory = oldCategory;
        this.oldCategoryColor = oldCategoryColor;
        this.newvalue = newvalue;
        this.newCategory = newCategory;
        this.newCategoryColor = newCategoryColor;
    }

    public Long getSiId() {
        return siId;
    }

    public void setSiId(Long siId) {
        this.siId = siId;
    }

    public String getSiName() {
        return siName;
    }

    public void setSiName(String siName) {
        this.siName = siName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getFact() {
        return fact;
    }

    public void setFact(List<String> fact) {
        this.fact = fact;
    }

    public List<Float> getFactVal() {
        return factVal;
    }

    public void setFactVal(List<Float> factVal) {
        this.factVal = factVal;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public float getOldvalue() {
        return oldvalue;
    }

    public void setOldvalue(float oldvalue) {
        this.oldvalue = oldvalue;
    }

    public String getOldCategory() {
        return oldCategory;
    }

    public void setOldCategory(String oldCategory) {
        this.oldCategory = oldCategory;
    }

    public String getOldCategoryColor() {
        return oldCategoryColor;
    }

    public void setOldCategoryColor(String oldCategoryColor) {
        this.oldCategoryColor = oldCategoryColor;
    }

    public float getNewvalue() {
        return newvalue;
    }

    public void setNewvalue(float newvalue) {
        this.newvalue = newvalue;
    }

    public String getNewCategory() {
        return newCategory;
    }

    public void setNewCategory(String newCategory) {
        this.newCategory = newCategory;
    }

    public String getNewCategoryColor() {
        return newCategoryColor;
    }

    public void setNewCategoryColor(String newCategoryColor) {
        this.newCategoryColor = newCategoryColor;
    }
}
