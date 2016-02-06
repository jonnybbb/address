package hello;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by johannes on 29/01/16.
 */
public class Address implements Serializable {
    private String street;
    private  String plz;
    private String number;

  private String place;

    public Address() {
    }

    public Address(String street, String plz, String place) {


        this.street = street;
        this.plz = plz;
        this.place = place;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return street + " " + plz + " " + place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        if (plz != null ? !plz.equals(address.plz) : address.plz != null) return false;
        return place != null ? place.equals(address.place) : address.place == null;

    }

    @Override
    public int hashCode() {
        int result = street != null ? street.hashCode() : 0;
        result = 31 * result + (plz != null ? plz.hashCode() : 0);
        result = 31 * result + (place != null ? place.hashCode() : 0);
        return result;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
