package Assignment1;




import java.util.ArrayList;
import java.util.List;



public class HashTableClass {

    static class Node {

        final BusinessClass key;
        FrequencyTableClass val;
        Node next;

        Node(BusinessClass k, FrequencyTableClass v, Node n) {
            key = k;
            val = v;
            next = n;
        }
    }
    Node[] table;
    int count;

    public HashTableClass() {
        table = new Node[8];
        count = 0;
    }
    public void put(BusinessClass key, FrequencyTableClass val) {
        int h = key.business_id.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.val.putNewReview(val.reviewText);
                return;
            }
        }
        table[i] = new Node(key, val, table[i]);
        ++count;
        if (((double) count / (double) table.length) > 0.75) {
            resize();
        }
    }
    public void remove(BusinessClass key) {
        int h = key.business_id.hashCode();
        int i = h & (table.length - 1);
        Node pred = null;
        Node e = table[i];
        while (e != null) {
            if (key.business_id.equals(e.key.business_id)) {
                if (pred == null) {
                    table[i] = e.next;
                } else {
                    pred.next = e.next;
                }
                --count;
                return;
            }
            pred = e;
            e = e.next;
        }
    }
    public FrequencyTableClass get(BusinessClass key) {
        int h = key.business_id.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.business_id.equals(e.key.business_id)) {
                return e.val;
            }
        }
        return null;
    }



    private void resize() {
        Node[] oldTable = table;
        Node[] newTable = new Node[oldTable.length * 2];
        for (int i = 0; i < oldTable.length; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
                int h = e.key.business_id.hashCode();
                int j = h & (newTable.length - 1);
                newTable[j] = new Node(e.key, e.val, newTable[j]);
            }
        }
        table = newTable;
    }



    public int findBusiness(String id, String name, int b) {
        int h = id.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (id.equals(e.key.business_id)) {
                b = b + 1;
                e.key.setBusiness_name(name);    // associating business names with entries by ids
                return b;
            }
        }
        return b;
    }
    public String getIDByName(String name) {
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                if (e.key.getBusiness_name().equalsIgnoreCase(name)) {
                    return e.key.getBusinessID();
                }
            }
        }
        return "";
    }
    public void computeTF() {
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                e.val.computeTF();
            }
        }
    }



    public List<String> obtainBusinessReviews() {
        List<String> businessReviews = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                businessReviews.add(e.val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", ""));
            }
        }
        return businessReviews;
    }
}
