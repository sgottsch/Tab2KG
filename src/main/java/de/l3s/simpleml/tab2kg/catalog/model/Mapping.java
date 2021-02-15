package de.l3s.simpleml.tab2kg.catalog.model;

/**
 * Created by iosifidis on 29.03.19.
 */
public class Mapping {
    private String representsProperty;
    private String representsClass;
    private String representsPartOfProperty;
    private String representsPropertyOfSubject;

    public Mapping() {
    }

    public Mapping(String representsProperty, String representsClass, String representsPartOfProperty, String representsPropertyOfSubject) {
        this.representsProperty = representsProperty;
        this.representsClass = representsClass;
        this.representsPartOfProperty = representsPartOfProperty;
        this.representsPropertyOfSubject = representsPropertyOfSubject;
    }

    public String getRepresentsProperty() {
        return representsProperty;
    }

    public void setRepresentsProperty(String representsProperty) {
        this.representsProperty = representsProperty;
    }

    public String getRepresentsClass() {
        return representsClass;
    }

    public void setRepresentsClass(String representsClass) {
        this.representsClass = representsClass;
    }

    public String getRepresentsPartOfProperty() {
        return representsPartOfProperty;
    }

    public void setRepresentsPartOfProperty(String representsPartOfProperty) {
        this.representsPartOfProperty = representsPartOfProperty;
    }

    public String getRepresentsPropertyOfSubject() {
        return representsPropertyOfSubject;
    }

    public void setRepresentsPropertyOfSubject(String representsPropertyOfSubject) {
        this.representsPropertyOfSubject = representsPropertyOfSubject;
    }
}
