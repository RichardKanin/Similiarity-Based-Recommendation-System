/*
 * My BTree class for Assignment 2 below
 */
package Assignment2;

import java.io.IOException;


public class BTREEClass {

    private BNODEClass bNodeRoot;
    private int orderOF;
    private FileOperatorClass fileManagement;

    public BTREEClass(int order, String filePATH, String ftFile, String tfidf, String clusters, String clusterVectors) throws IOException {
        this.orderOF = order;
        fileManagement = new FileOperatorClass(order, filePATH, ftFile, tfidf, clusters, clusterVectors);
        if (fileManagement.randomAccessFilePeak(0) == -1) {     // the BTree file is empty
            bNodeRoot = new BNODEClass(order, fileManagement);
            bNodeRoot.position = fileManagement.getNextAvailablePosition();
            fileManagement.writeToRandomAccessFile(bNodeRoot);
        } else {
            bNodeRoot = fileManagement.randomAccessFileRead(0);     // BTree file has content, read from it
        }
    }

    public BNODEClass getRoot() {
        return bNodeRoot;
    }

    public FileOperatorClass getFileManager() {
        return fileManagement;
    }

    public void insert(BusinessCLASS b, FrequencyTableClass ft) throws IOException {
        if (bNodeRoot.entries.size() >= (orderOF - 1)) {
            bNodeSplitbySelf();
            fileManagement.writeToRandomAccessFile(bNodeRoot);
        }
        bNodeRoot.insertionFunction(b, ft);
        fileManagement.writeToRandomAccessFile(bNodeRoot);
    }

    private void bNodeSplitbySelf() throws IOException {
        BNODEClass child1 = new BNODEClass(orderOF, fileManagement);
        child1.position = fileManagement.getNextAvailablePosition();
        BNODEClass child2 = new BNODEClass(orderOF, fileManagement);
        child2.position = fileManagement.getNextAvailablePosition();
        // System.out.println("Root split: child1.position: " + child1.position + ", child2.position: " + child2.position);

        int midPos = (orderOF / 2) - 1;

        BNODEClass.Node entry = bNodeRoot.entries.remove(midPos);
        //loop through for left side of middle position
        for (int j = 0; j < midPos; j++) {
            child1.entries.add(bNodeRoot.entries.get(j));     // left hand side of the middle
        }
        for (int j = midPos; j < bNodeRoot.entries.size(); j++) {
            child2.entries.add(bNodeRoot.entries.get(j));     // right hand side of the middle
        }
        if (!bNodeRoot.children.isEmpty()) {
            // split, but check if after splitting, it's not the leaf
            for (int j = 0; j < midPos + 1; j++) {
                child1.children.add(bNodeRoot.children.get(j));
            }
            for (int j = midPos + 1; j < bNodeRoot.children.size(); j++) {
                child2.children.add(bNodeRoot.children.get(j));
            }
        }

        bNodeRoot.entries.clear();
        bNodeRoot.children.clear();
        bNodeRoot.entries.add(entry);
        bNodeRoot.children.add(child1);
        bNodeRoot.children.add(child2);

        fileManagement.writeToRandomAccessFile(child1);
        fileManagement.writeToRandomAccessFile(child2);
        fileManagement.eraseFromRandomAccessFile(bNodeRoot);
        bNodeRoot.position = fileManagement.getNextAvailablePosition();
    }

    public void computeTF() throws IOException {
        bNodeRoot.termFrequencyComputation();
    }

    public int count() {
        return bNodeRoot.getCount();
    }

    // TFIDF
    public void computeIDF() throws IOException {
        if (fileManagement.tfidfFileNotReady()) {
            int nrOfText = count();
            bNodeRoot.inverseDocumentFrequencyComputation(nrOfText);
        }
    }

    public void computeTFIDF(TFIDFCLASS.Node[] table) throws IOException {
        bNodeRoot.computeTFIDF(table);
    }

    public String idByName(String name) {
        return bNodeRoot.idByName(name);
    }

    public FrequencyTableClass search(String id) {
        return bNodeRoot.search(id);
    }

}