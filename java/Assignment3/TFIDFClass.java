/*
 * Class for TFIDF computation
 */
package Assignment3;

import Assignment3.BTree.idfNode;
import java.io.IOException;
import java.util.ArrayList;

public class TFIDFClass {

    static class Node {

        final String key;
        double val;
        Node next;

        private Node(String s, double i, Node n) {
            key = s;
            val = i;
            next = n;
        }

        public String toString() {
            return key + ": " + val;
        }

    }
    Node[] table;
    int count;
    BTree bt;

    public TFIDFClass(BTree t) {
        table = new Node[8];
        count = 0;
        bt = t;
    }

    public Node[] getTFIDFtable() {
        return table;
    }

    public void computeIDF() throws IOException {
        bt.computeIDF();
        ArrayList<idfNode> list = bt.getIDFList();
        for (int i = 0; i < list.size(); i++) {
            String r = list.get(i).word;
            double idf = list.get(i).idf;
            int h = r.hashCode();
            int index = h & (table.length - 1);
            table[index] = new Node(r, idf, table[index]);
            count++;
            if (((double) count / (double) table.length) > 0.75) {
                resize();
            }
        }
    }

    public boolean newWord(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);     // int i = Math.abs(h)%table.size;
        for (Node e = table[i]; e != null; e = e.next) {
            if (e.key.equalsIgnoreCase(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean arrayContains(String[] array, String s) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public double getIDF(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);     // int i = Math.abs(h)%table.size;
        for (Node e = table[i]; e != null; e = e.next) {
            if (e.key.equalsIgnoreCase(key)) {
                return e.val;
            }
        }
        return 0;
    }

    public void computeTFIDF() throws IOException {
        bt.computeTFIDF(table);
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
        int i = h & (table.length - 1);     // int i = Math.abs(h)%table.size;
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