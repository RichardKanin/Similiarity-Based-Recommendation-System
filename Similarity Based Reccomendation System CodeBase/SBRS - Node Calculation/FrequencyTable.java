/*
Frequency table class
 */
package Assignment3;



public class FrequencyTable {

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
    Node[] table;
    int count;
    String reviewText;
    double[] vector;

    public FrequencyTable(String r) {
        table = new Node[8];
        count = 0;
        reviewText = r;
    }

    public void putNewReview(String r) {
        reviewText = reviewText + r;
    }

    public int get(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);     // int i = Math.abs(h)%table.size;
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                return e.val;
            }
        }
        return 0;
    }

    public void put(String[] array, String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        // int i = Math.abs(h)%table.size;
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equalsIgnoreCase(e.key)) {
                e.val = e.val + 1;
                e.tf = (double) e.val / (double) array.length;
                return;
            }
        }
        table[i] = new Node(key, 1, table[i]);
        ++count;
        table[i].tf = (double) 1 / (double) array.length;
        if (((double) count / (double) table.length) > 0.75) {
            resize();
        }
    }

    public void load(String word, int frequency, double termFrequency) {
        int h = word.hashCode();
        int i = h & (table.length - 1);
        table[i] = new Node(word, frequency, table[i]);
        ++count;
        table[i].tf = termFrequency;
        if (((double) count / (double) table.length) > 0.75) {
            resize();
        }
    }

    public void computeTF() {
        String formatted = reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "");
        String[] reviewArray = formatted.split(" ");
        for (int i = 0; i < reviewArray.length; i++) {
            String review = reviewArray[i];
            if (!review.isEmpty() && review.matches("[a-zA-Z]*")) {
                put(reviewArray, review);
            }
        }
    }

    public double getTF(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equalsIgnoreCase(e.key)) {
                return e.tf;
            }
        }
        return 0;
    }

    private void resize() {
        Node[] oldTable = table;
        Node[] newTable = new Node[oldTable.length * 2];
        for (int i = 0; i < oldTable.length; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
                int h = e.key.hashCode();
                int j = h & (newTable.length - 1);
                newTable[j] = new Node(e.key, e.val, newTable[i]);
            }
        }
        table = newTable;
    }

    public void remove(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        Node pred = null;
        Node e = table[i];
        while (e != null) {
            if (key.equals(e.key)) {
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
}