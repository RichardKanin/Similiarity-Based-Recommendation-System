
//BUSINESS CLASS ASSIGNMENT 2

package Assignment2;

public class BusinessCLASS implements Comparable{

    String business_id;
    String name;

    public BusinessCLASS(String id){
        business_id = id;
    }

    public void putName(String name){
        this.name = name;
    }

    public String getID(){
        return business_id;
    }

    public String getName(){
        return name;
    }

    public boolean equals(BusinessCLASS b){
        return business_id.equals(b.getID());
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
