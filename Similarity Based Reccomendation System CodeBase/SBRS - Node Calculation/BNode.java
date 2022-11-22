/*
Java class for my bnodes
 */
package Assignment3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BNode implements Serializable {

    static class Node {

        Business key;
        FrequencyTable val;

        Node(Business k, FrequencyTable v) {
            key = k;
            val = v;
        }
    }

    List<Node> entries;
    List<BNode> children;     // position of children
    private int order;     // if reach, split
    int position = -1;     // position in the file
    private FileOperator fileManager;

    public BNode(int o, FileOperator fm) {
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

    public void insert(Business b, FrequencyTable ft) throws IOException {
        // spliting of root is checked in BTree
        int indexIfAdded = indexOf(b);
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
        if (children.isEmpty()) {     // leaf node -> add dirrectly
            Node n = new Node(b, ft);
            entries.add(entryIndex, n);
            return;
        }
        // add from bottom
        BNode child = children.get(entryIndex);
        if (child.entries.size() > order) {     // child is full
            splitChild(entryIndex);
            if (b.compareTo(entries.get(entryIndex).key) < 0) {
                child = children.get(entryIndex);     // less than -> go left
            } else if (b.compareTo(entries.get(entryIndex).key) > 0) {
                child = children.get(entryIndex + 1);     // more than -> go right
            } else {
                entries.get(entryIndex).val.putNewReview(ft.reviewText);
                return;
            }
        }
        child.insert(b, ft);     // child is not full -> add directly
        fileManager.write(child);
    }

    private int indexOf(Business b) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).key.equals(b)) {
                return i;
            }
        }
        return -1;
    }

    private void splitChild(int entryIndex) throws IOException {
        BNode parent = children.get(entryIndex);     // BNode to be split
        BNode child1 = new BNode(order, fileManager);     // split left
        child1.position = fileManager.getNextAvailablePosition();
        BNode child2 = new BNode(order, fileManager);     // split right
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
        fileManager.write(child1);
        fileManager.write(child2);
        entries.add(entryIndex, entry);     // add the new split node
        children.set(entryIndex, child1);     // replace original child with child1
        children.add(entryIndex + 1, child2);     // add child2
        fileManager.erase(parent);
    }

    public void computeTF() throws IOException {
        if (fileManager.ftFileNotReady()) {
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).val.computeTF();
            }
            fileManager.frequencyTableWRITER(this, position);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).computeTF();
            }
        } else {
            ArrayList<FrequencyTable> fList = fileManager.loadFT(position);
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).val.table = fList.get(i).table;
            }
            for (int i = 0; i < children.size(); i++) {
                children.get(i).computeTF();
            }
        }
    }

    public int countDF(String r) {
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

    public boolean arrayContains(String[] array, String s) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public void computeTFIDF(TFIDFClass.Node[] table) throws IOException {
        if (fileManager.tfidfFileNotReady()) {
            for (int i = 0; i < entries.size(); i++) {
                FrequencyTable ft = entries.get(i).val;
                String[] reviewArray = ft.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                double[] vector = new double[table.length];
                int counter = 0;
                for (int j = 0; j < table.length; j++) {     // go through the word list
                    for (TFIDFClass.Node n = table[j]; n != null; n = n.next) {
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
            fileManager.write_tfidf(this, position);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).computeTFIDF(table);
            }
        } else {
            loadVectors();
        }
    }

    public void loadVectors() throws FileNotFoundException, IOException {
        ArrayList<double[]> vectors = fileManager.TFIDF_LOADER(position);
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

    public FrequencyTable search(String id) {
        int entryIndex = 0;
        for (Node n : entries) {
            if (id.compareTo(n.key.business_id) > 0) {     // if id is greater than keys
                entryIndex++;
            } else if (id.compareTo(n.key.business_id) == 0) {     // found
                return n.val;
            }
        }
        BNode child = children.get(entryIndex);
        return child.search(id);
    }

}