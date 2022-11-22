/*
 * My Java class for my BNodes
 *
 */
package Assignment2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BNODEClass implements Serializable {

    static class Node {

        BusinessCLASS key;
        FrequencyTableClass val;

        Node(BusinessCLASS k, FrequencyTableClass v) {
            key = k;
            val = v;
        }
    }

    List<Node> entries;
    List<BNODEClass> children;     // position of children
    private int order;     // if reached, split
    int position = -1;     // position in the file
    private FileOperatorClass fileManager;

    public BNODEClass(int o, FileOperatorClass fm) {
        order = o;
        entries = new ArrayList<>(order - 1);
        children = new ArrayList<>(order);
        fileManager = fm;
    }

    public int getCount() {
        int count = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (!children.isEmpty()) {
                count = count + children.get(i).getCount();
            }
            count++;
        }
        if (!children.isEmpty()) {
            count = count + children.get(entries.size()).getCount();
        }
        return count;
    }

    public void insertionFunction(BusinessCLASS b, FrequencyTableClass ft) throws IOException {
        // splitting of root is checked in BTree
        int indexIfAdded = getIndexOf(b);
        if (indexIfAdded != -1) {     // if business is added before
            entries.get(indexIfAdded).val.putNewReview(ft.reviewText);
            return;
        }
        int entryIndex = 0;
        for (Node n : entries) {
            if (b.compareTo(n.key) > 0) {     // if inserting entry is greater than keys
                entryIndex++;
            }
        }
        if (children.isEmpty()) {     // leaf node -> add directly
            Node n = new Node(b, ft);
            entries.add(entryIndex, n);
            return;
        }
        // add from bottom
        BNODEClass child = children.get(entryIndex);
        if (child.entries.size() > order) {     // child is full
            bNodeSplitChildFunction(entryIndex);
            if (b.compareTo(entries.get(entryIndex).key) < 0) {
                child = children.get(entryIndex);     // less than -> go left
            } else if (b.compareTo(entries.get(entryIndex).key) > 0) {
                child = children.get(entryIndex + 1);     // more than -> go right
            } else {
                entries.get(entryIndex).val.putNewReview(ft.reviewText);
                return;
            }
        }
        child.insertionFunction(b, ft);     // child is not full -> add directly
        fileManager.writeToRandomAccessFile(child);
    }

    private int getIndexOf(BusinessCLASS b) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).key.equals(b)) {
                return i;
            }
        }
        return -1;
    }

    private void bNodeSplitChildFunction(int entryIndex) throws IOException {
        BNODEClass parent = children.get(entryIndex);     // BNode to be split
        BNODEClass child1 = new BNODEClass(order, fileManager);     // split left
        child1.position = fileManager.getNextAvailablePosition();
        BNODEClass child2 = new BNODEClass(order, fileManager);     // split right
        child2.position = fileManager.getNextAvailablePosition();
        // System.out.println("Parent split: child1.position: " + child1.position + ", child2.position: " + child2.position);

        int midPosition = (order / 2) - 1;
        Node entry = parent.entries.remove(midPosition);     // entry left for split node, add back later
        for (int j = 0; j < midPosition; j++) {
            child1.entries.add(parent.entries.get(j));     // left hand side of the middle
        }
        for (int j = midPosition; j < parent.entries.size(); j++) {
            child2.entries.add(parent.entries.get(j));     // right hand side of the middle
        }
        if (!parent.children.isEmpty()) {     // if the splitting one is not leaf
            for (int j = 0; j < midPosition + 1; j++) {
                child1.children.add(parent.children.get(j));     // first half of the children
            }
            for (int j = midPosition + 1; j < parent.children.size(); j++) {
                child2.children.add(parent.children.get(j));     // second half of the children
            }
        }
        fileManager.writeToRandomAccessFile(child1);
        fileManager.writeToRandomAccessFile(child2);
        entries.add(entryIndex, entry);     // add the new split node
        children.set(entryIndex, child1);     // replace original child with child1
        children.add(entryIndex + 1, child2);     // add child2
        fileManager.eraseFromRandomAccessFile(parent);
    }

    public void termFrequencyComputation() throws IOException {
        if (fileManager.frequencyTableFileNotReady()) {
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).val.computeTF();
            }
            fileManager.writeFT(this, position);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).termFrequencyComputation();
            }
        } else {
            ArrayList<FrequencyTableClass> fList = fileManager.loadFrequencyTable(position);
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).val.nodeTable = fList.get(i).nodeTable;
            }
            for (int i = 0; i < children.size(); i++) {
                children.get(i).termFrequencyComputation();
            }
        }
    }

    // TFIDF
    static class idfNode {

        String word;
        double idf;

        private idfNode(String w, double i) {
            word = w;
            idf = i;
        }

    }

    ArrayList<idfNode> idf = new ArrayList<>();

    public ArrayList<idfNode> getIDFList() {
        return idf;
    }

    public void inverseDocumentFrequencyComputation(int nrOfText) {
        ArrayList<idfNode> temp = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            if (!children.isEmpty()) {
                children.get(i).inverseDocumentFrequencyComputation(nrOfText);
                this.idf.addAll(children.get(i).getIDFList());
            }
            if (i < entries.size()) {
                String[] review = entries.get(i).val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                for (int j = 0; j < review.length; j++) {     // get each word
                    String r = review[j];
                    if (newWord(r)) {
                        int docFrequency = countDF(r);
                        double idf = Math.log(nrOfText / docFrequency);
                        idfNode node = new idfNode(r, idf);
                        this.idf.add(node);
                    }
                }
            }
        }
    }

    public boolean newWord(String key) {
        for (idfNode e : idf) {
            if (e.word.equalsIgnoreCase(key)) {
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

    private int countDF(String r) {
        int df = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (!children.isEmpty()) {
                df = df + children.get(i).countDF(r);
            }
            Node n = entries.get(i);
            String[] anotherReview = n.val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
            if (arrayContains(anotherReview, r)) {
                df++;
            }

        }
        if (!children.isEmpty()) {
            df = df + children.get(entries.size()).countDF(r);
        }
        return df;
    }

    public void computeTFIDF(TFIDFCLASS.Node[] table) throws IOException {
        if (fileManager.tfidfFileNotReady()) {
            for (int i = 0; i < entries.size(); i++) {
                FrequencyTableClass ft = entries.get(i).val;
                String[] reviewArray = ft.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                double[] vector = new double[table.length];
                int counter = 0;
                for (int j = 0; j < table.length; j++) {     // go through the word list
                    for (TFIDFCLASS.Node n = table[j]; n != null; n = n.next) {
                        String word = n.key;
                        boolean found = false;
                        for (int k = 0; k < reviewArray.length; k++) {     // if the review contains a word
                            if (word.matches("[a-zA-Z]*") && word.equalsIgnoreCase(reviewArray[k])) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            double tfidf = (ft.getTF(word)) * n.val;     // TF * IDF
                            vector[counter] = tfidf;
                            counter++;
                        } else {
                            vector[counter] = 0;     // not found -> tf = 0, tfidf must be 0
                            counter++;
                        }
                    }
                }
                ft.vector = vector;     // associate vector with entry
            }
            fileManager.writeTFIDF(this, position);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).computeTFIDF(table);
            }
        } else {
            loadVectors();
        }
    }

    public void loadVectors() throws FileNotFoundException, IOException {
        ArrayList<double[]> vectors = fileManager.loadTFIDF(position);
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).val.vector = vectors.get(i);
        }
        for (int i = 0; i < children.size(); i++) {
            children.get(i).loadVectors();
        }
    }

    public String idByName(String name) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).key.name.equalsIgnoreCase(name)) {
                return entries.get(i).key.business_id;
            }
        }
        for (int i = 0; i < children.size(); i++) {
            String id = children.get(i).idByName(name);
            if (!id.equals("")) {
                return id;
            }
        }
        return "";
    }

    public FrequencyTableClass search(String id) {
        int entryIndex = 0;
        for (Node n : entries) {
            if (id.compareTo(n.key.business_id) > 0) {     // if id is greater than keys
                entryIndex++;
            } else if (id.compareTo(n.key.business_id) == 0) {     // found
                return n.val;
            }
        }
        System.out.println("Entry Index:");
        System.out.println(entryIndex);
        System.out.println("children size:");
        System.out.println(children.size());
        BNODEClass child = children.get(entryIndex);
        return child.search(id);
    }

}