/*
 * Java class in package assignment 2 called
 * FrequencyTable class for review text
 */
package Assignment2;

import Assignment1.*;
import java.util.ArrayList;
import java.util.List;


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
        int i = h & (nodeTable.length - 1);     // int i = Math.abs(h)%table.size;
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                return e.val;
            }
        }
        return 0;
    }

    public void put(String[] array, String key) {
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);     // int i = Math.abs(h)%table.size;
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (key.equalsIgnoreCase(e.key)) {
                e.val = e.val + 1;
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

    public void load(String word, int frequency, double termFrequency) {
        int h = word.hashCode();
        int i = h & (nodeTable.length - 1);     // int i = Math.abs(h)%table.size;
        nodeTable[i] = new Node(word, frequency, nodeTable[i]);
        ++count;
        nodeTable[i].tf = termFrequency;
        if (((double) count / (double) nodeTable.length) > 0.75) {
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
        int i = h & (nodeTable.length - 1);     // int i = Math.abs(h)%table.size;
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
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);     // int i = Math.abs(h)%table.size;
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