package Assignment1;


/*
 * FrequencyTable class for review text
 */


public class FrequencyTableClass {

    static class Node {

        final String key;
        int val;
        double tf;
        Node next;

        Node(String k, int v, Node n) {
            key = k;
            val = v;
            next = n;
        }
    }
    Node[] nodeTable;
    int count;
    String reviewText;
    double[] vector;

    public FrequencyTableClass(String r) {
        nodeTable = new Node[8];
        count = 0;
        reviewText = r;
    }

    public void putNewReview(String r) {
        reviewText = reviewText + r;
    }

    public int get(String key) {
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                return e.val;
            }
        }
        return 0;
    }

    public void put(String[] array, String key) {
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (key.equalsIgnoreCase(e.key)) {
                e.val = e.val+1;
                e.tf = (double) e.val / (double) array.length;
                return;
            }
        }
        nodeTable[i] = new Node(key, 1, nodeTable[i]);
        ++count;
        nodeTable[i].tf = (double) 1 / (double) array.length;
        if (((double) count / (double) nodeTable.length) > 0.75) {
            resize();
        }
    }

    public void computeTF() {
        // format and replace new lines and punctuation marks on the reviews
        String formatted = reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "");
        String[] reviewArray = formatted.split(" ");
        // loop through length of formatted reviews
        for (int i = 0; i < reviewArray.length; i++) {
            String review = reviewArray[i];
            put(reviewArray, review);
        }
    }

    public double getTF(String key) {
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (key.equalsIgnoreCase(e.key)) {
                return e.tf;
            }
        }
        return 0;
    }

    private void resize() {
        Node[] oldTable = nodeTable;
        Node[] newTable = new Node[oldTable.length * 2];
        // resize new table to be double of old table
        //loop through old table, hash the element key, make it fit
        // then shove it into the new table
        for (int i = 0; i < oldTable.length; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
                int h = e.key.hashCode();
                int j = h & (newTable.length - 1);
                newTable[j] = new Node(e.key, e.val, newTable[i]);
            }
        }
        nodeTable = newTable;
    }

    public void remove(String key) {
        //hash the key
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);
        Node pred = null;
        Node e = nodeTable[i];
        while (e != null) {
            if (key.equals(e.key)) {
                if (pred == null) {
                    nodeTable[i] = e.next;
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
}
