package Assignment1;

//business class used to get business id
// followed by getters and setters

public class BusinessClass {

    String business_id;
    String business_name;

    public BusinessClass(String id) {
        business_id = id;
    }


    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getBusinessID() {
        return business_id;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public boolean equals(BusinessClass b) {
        return business_id.equals(b.getBusinessID());
    }

}
