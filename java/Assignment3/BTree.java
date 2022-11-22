/*
 * B-Tree classes from assignment 2
 */
package Assignment3;

import java.io.IOException;
import java.util.ArrayList;


public class  BTree {

    private BNode root;
    private int order;
    private FileOperator fileManager;

    public BTree(int order, String path, String ftFile, String tfidf, String clusters, String clusterVectors, String graph) throws IOException {
        this.order = order;
        fileManager = new FileOperator(order, path, ftFile, tfidf, clusters, clusterVectors, graph);
        if (fileManager.peek(0) == -1) {     // the BTree file is empty
            root = new BNode(order, fileManager);
            root.position = fileManager.getNextAvailablePosition();
            fileManager.write(root);
        } else {
            root = fileManager.read(0);     // BTree file has content, read from it
        }
    }

    public BNode getRoot() {
        return root;
    }

    public FileOperator getFileManager() {
        return fileManager;
    }

    public void insert(Business b, FrequencyTable ft) throws IOException {
        if (root.entries.size() >= (order - 1)) {
            splitSelf();
            fileManager.write(root);
        }
        root.insert(b, ft);
        fileManager.write(root);
    }

    private void splitSelf() throws IOException {
        BNode child1 = new BNode(order, fileManager);
        child1.position = fileManager.getNextAvailablePosition();
        BNode child2 = new BNode(order, fileManager);
        child2.position = fileManager.getNextAvailablePosition();

        int midPos = (order / 2) - 1;

        BNode.Node entry = root.entries.remove(midPos);
        for (int j = 0; j < midPos; j++) {
            child1.entries.add(root.entries.get(j));     // left hand side of the middle
        }
        for (int j = midPos; j < root.entries.size(); j++) {
            child2.entries.add(root.entries.get(j));     // right hand side of the middle
        }
        if (!root.children.isEmpty()) {     // if the splitting one is not leaf
            for (int j = 0; j < midPos + 1; j++) {
                child1.children.add(root.children.get(j));     // first half of the children
            }
            for (int j = midPos + 1; j < root.children.size(); j++) {
                child2.children.add(root.children.get(j));     // second half of the children
            }
        }

        root.entries.clear();
        root.children.clear();
        root.entries.add(entry);
        root.children.add(child1);
        root.children.add(child2);

        fileManager.write(child1);
        fileManager.write(child2);
        fileManager.erase(root);
        root.position = fileManager.getNextAvailablePosition();
    }

    public void computeTF() throws IOException {
        root.computeTF();
    }

    public int count() {
        return root.getCount();
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

    public void computeNodeIDF(BNode n, int nrOfText) {
        for (int i = 0; i < n.children.size(); i++) {
            if (!n.children.isEmpty()) {
                computeNodeIDF(n.children.get(i),nrOfText);
            }
            if (i < n.entries.size()) {
                String[] review = n.entries.get(i).val.reviewText.replace("\n", " ").replaceAll("\\p{Punct}", "").split(" ");
                for (int j = 0; j < review.length; j++) {     // get each word
                    String r = review[j];
                    if (newWord(r)) {
                        int docFrequency = root.countDF(r);
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

    // TFIDF
    public void computeIDF() throws IOException {
        if (fileManager.tfidfFileNotReady()) {
            int nrOfText = count();
            computeNodeIDF(root, nrOfText);
        }
    }

    public void computeTFIDF(TFIDFClass.Node[] table) throws IOException {
        root.computeTFIDF(table);
    }

    public String idByName(String name) {
        return root.idByName(name);
    }

    public FrequencyTable search(String id) {
        return root.search(id);
    }

}