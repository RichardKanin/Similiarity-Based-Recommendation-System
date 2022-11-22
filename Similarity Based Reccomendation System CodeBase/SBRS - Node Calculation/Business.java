/*
business class
 */
package Assignment3;


public class Business implements Comparable {

    String business_id;
    String name;
    double latitude;
    double longitude;

    public Business(String id) {
        business_id = id;
    }

    public void putName(String name) {
        this.name = name;
    }

    public String getID() {
        return business_id;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Business b) {
        return business_id.equals(b.getID());
    }

    @Override
    public int compareTo(Object o) {
        Business another = (Business) o;
        return business_id.compareTo(another.business_id);
    }

    public void putLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}