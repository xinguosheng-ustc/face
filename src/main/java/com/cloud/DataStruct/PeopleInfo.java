package com.cloud.DataStruct;

public class PeopleInfo {
    private String personName;
    private String personWorkunit;
    private String personSex;
    private String personOccupation;
    private float[] personLocation;
    public String getPersonName() {
        return personName;
    }

    public float[] getPersonLocation() {
        return personLocation;
    }

    public void setPersonLocation(float[] personLocation) {
        this.personLocation = personLocation;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonWorkunit() {
        return personWorkunit;
    }

    public void setPersonWorkunit(String personWorkunit) {
        this.personWorkunit = personWorkunit;
    }

    public String getPersonSex() {
        return personSex;
    }

    public void setPersonSex(String personSex) {
        this.personSex = personSex;
    }

    public String getPersonOccupation() {
        return personOccupation;
    }

    public void setPersonOccupation(String personOccupation) {
        this.personOccupation = personOccupation;
    }
}
