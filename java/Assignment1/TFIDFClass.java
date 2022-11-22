package Assignment1;





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
    Node[] nodeTable;
    int count;
    HashTableClass hashTableClass;

    public TFIDFClass(HashTableClass h) {
        nodeTable = new Node[8];
        count = 0;
        hashTableClass = h;
    }

    public void inverseDocumentFrequencyComputation() {
        int textNumber = hashTableClass.count;
        for (int i = 0; i < hashTableClass.table.length; i++) {
            for (HashTableClass.Node e = hashTableClass.table[i]; e != null; e = e.next) {     // go through hash table
                String[] review = e.val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                for (int j = 0; j < review.length; j++) {     // get each word
                    String r = review[j];
                    if (newWord(r)) {
                        int documentFrequency = 1;
                        for (int k = 0; k < hashTableClass.table.length; k++) {
                            // see if other reviews contain the word
                            for (HashTableClass.Node n = hashTableClass.table[i]; n != null; n = n.next) {
                                if (n != e) {
                                    String[] anotherReview = n.val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                                    if (arrayContains(anotherReview, r)) {
                                        documentFrequency++;
                                    }
                                }
                            }
                        }
                        double idf = Math.log(textNumber / documentFrequency);
                        int h = r.hashCode();
                        int index = h & (nodeTable.length - 1);
                        nodeTable[index] = new Node(r, idf, nodeTable[index]);
                        count++;
                        if (((double) count / (double) nodeTable.length) > 0.75) {
                            resize();
                        }
                    }
                }
            }
        }
    }



    //new word method check if word is really a new word
    public boolean newWord(String key) {
        int h = key.hashCode();
        int i = h & (nodeTable.length - 1);
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (e.key.equalsIgnoreCase(key)) {
                return false;
            }
        }
        return true;
    }
    //checks if string is really inside the given array
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
        int i = h & (nodeTable.length - 1);
        for (Node e = nodeTable[i]; e != null; e = e.next) {
            if (e.key.equalsIgnoreCase(key)) {
                return e.val;
            }
        }
        return 0;
    }

    public void computeTFIDF() {
        for (int i = 0; i < hashTableClass.table.length; i++) {     // go through the hash table
            for (HashTableClass.Node e = hashTableClass.table[i]; e != null; e = e.next) {
                FrequencyTableClass frequencyTableClass = e.val;
                String[] reviewArray = frequencyTableClass.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                double[] vector = new double[count];
                int counter = 0;
                // loops through length of the node table
                for (int j = 0; j < nodeTable.length; j++) {
                    for (Node n = nodeTable[j]; n != null; n = n.next) {
                        String word = n.key;
                        boolean found = false;
                        // loops through review length to find word
                        for (int k = 0; k < reviewArray.length; k++) {
                            if (word.equalsIgnoreCase(reviewArray[k])) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            double tfidf = (frequencyTableClass.getTF(word)) * n.val;
                            // TF * IDF
                            vector[counter] = tfidf;
                            counter++;
                        } else {
                            vector[counter] = 0;
                            // not found -> tf = 0, tfidf must be 0
                            counter++;
                        }
                    }
                }
                frequencyTableClass.vector = vector;     // associate vector with entry
            }
        }
    } private void resize() {
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