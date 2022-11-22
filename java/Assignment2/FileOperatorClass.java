/*
 * Class to manage all the data files
 */
package Assignment2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class FileOperatorClass {

    private int order;
    public int blocks;
    private RandomAccessFile raf;
    private int blockSize;
    ArrayList<Integer> availablePositions;

    private RandomAccessFile ftFile;
    private boolean frequencyTableReady;

    private RandomAccessFile tfidfVectorsFile;
    private boolean tfidfVectorsReady;

    private RandomAccessFile clustersFile;
    private boolean clustersReady;

    private RandomAccessFile clusterVectorsFile;

    // persistent FT, TDIDF
    // A persistent Frequency Table and Term Document Inverse Document Frequency

    public FileOperatorClass(int order, String filepath, String ft, String tfidf, String clusters, String clusterVectors) throws FileNotFoundException, IOException {
        this.order = order;
        blockSize = ((order - 1) * 8000) + (order * (4 + 1));
        blocks = 0;
        availablePositions = new ArrayList<>();
        raf = new RandomAccessFile(filepath, "rw");
        ftFile = new RandomAccessFile(ft, "rw");
        if (ftFile.read() == -1) {
            frequencyTableReady = false;
        } else {
            frequencyTableReady = true;
        }
        tfidfVectorsFile = new RandomAccessFile(tfidf, "rw");
        if (tfidfVectorsFile.read() == -1) {
            tfidfVectorsReady = false;
        } else {
            tfidfVectorsReady = true;
        }
        clustersFile = new RandomAccessFile(clusters, "rw");
        if (clustersFile.read() == -1) {
            clustersReady = false;
        } else {
            clustersReady = true;
        }
        clusterVectorsFile = new RandomAccessFile(clusterVectors, "rw");
    }

    public int getNextAvailablePosition() {
        if (availablePositions.isEmpty()) {
            blocks++;
            return (blocks - 1);
        }
        Collections.sort(availablePositions);
        int position = availablePositions.remove(0);
        return position;
    }

    public int randomAccessFilePeak(int i) throws IOException {
        return raf.read();
    }

    public BNODEClass randomAccessFileRead(int position) throws IOException {
        BNODEClass n = bytesToNode(readFromRAF(position));
        n.position = position;
        return n;
    }

    public void writeToRandomAccessFile(BNODEClass n) throws IOException {
        byte[] nodeBytes = nodeToBytes(n);
        writeToRAF(nodeBytes, n.position);
    }

    public void eraseFromRandomAccessFile(BNODEClass n) throws IOException {
        eraseFromRAF(n.position);
    }

    public void erase(int position) throws IOException {
        eraseFromRAF(position);
    }

    private byte[] readFromRAF(int position) throws IOException {
        raf.seek(position * blockSize);
        byte[] b = new byte[blockSize];
        raf.read(b);
        return b;
    }

    private void writeToRAF(byte[] b, int position) throws IOException {
        raf.seek(position * blockSize);
        raf.write(b);
    }

    private void eraseFromRAF(int position) throws IOException {
        if (position == blocks) {
            byte[] b = new byte[blockSize];
            writeToRAF(b, position);
            blocks--;
        } else if (position < blocks) {
            byte[] b = new byte[blockSize];
            writeToRAF(b, position);
            availablePositions.add(position);
        } else {
            // should never happen
            System.out.println("Sorry, an error occurred erasing a node");
            System.exit(-1);
        }
    }

    private byte[] nodeToBytes(BNODEClass node) {
        String s = "";
        s = s + "[" + node.entries.size() + "]";
        for (int i = 0; i < node.entries.size(); i++) {
            BusinessCLASS b = node.entries.get(i).key;
            FrequencyTableClass ft = node.entries.get(i).val;
            String entry = b.name + "|" + b.business_id + "|" + ft.reviewText + "||";
            s = s + entry;
        }
        s = s + "[" + node.children.size() + "]||";
        for (int i = 0; i < node.children.size(); i++) {
            String index = String.valueOf(node.children.get(i).position);
            s = s + index;
            if (i != node.children.size() - 1) {
                s = s + "||";
            }
        }
        return s.getBytes();
    }

    private BNODEClass bytesToNode(byte[] b) throws IOException {     // recursive
        // name|id|review || name|id|review || ... || child1 || child2 || ...
        // bytes -> String
        BNODEClass n = new BNODEClass(order, this);
        String s = new String(b).trim();
        int numberOfEntries = Integer.parseInt(s.substring(1, s.indexOf("]")));
        String[] split = s.substring(s.indexOf("]") + 1).split("\\|\\|");
        for (int i = 0; i < numberOfEntries; i++) {
            String[] entry = split[i].split("\\|");
            BusinessCLASS business = new BusinessCLASS(entry[1]);
            business.putName(entry[0]);
            FrequencyTableClass ft = new FrequencyTableClass(entry[2]);
            n.insertionFunction(business, ft);
        }
        int numberOfChildren = Integer.parseInt(split[numberOfEntries].substring(1, split[numberOfEntries].indexOf("]")));
        for (int i = numberOfEntries + 1; i < numberOfChildren + numberOfEntries + 1; i++) {
            int childPos = Integer.parseInt(split[i]);
            BNODEClass child = bytesToNode(readFromRAF(childPos));
            child.position = childPos;
            n.children.add(child);
        }
        return n;
    }

    public boolean frequencyTableFileNotReady() throws IOException {
        return !frequencyTableReady;
    }

    public boolean tfidfFileNotReady() throws IOException {
        return !tfidfVectorsReady;
    }

    public boolean clustersFileNotReady() throws IOException {
        return !clustersReady;
    }

    public ArrayList<FrequencyTableClass> loadFrequencyTable(int nodePos) throws IOException {
        ArrayList<FrequencyTableClass> fList = new ArrayList<>();
        ftFile.seek(nodePos * blockSize);
        byte[] b = new byte[blockSize];
        ftFile.read(b);
        String data = new String(b).trim();
        String[] parsed = data.split("\\|\\|");
        for (int i = 0; i < parsed.length; i++) {
            FrequencyTableClass temp = new FrequencyTableClass("");     // review text doesn't matter here
            String[] nodes = parsed[i].split("\\|");
            for (int j = 0; j < nodes.length; j++) {
                String[] ftNode = nodes[j].split(":");
                temp.load(ftNode[0], Integer.parseInt(ftNode[1]), Double.parseDouble(ftNode[2]));
            }
            fList.add(temp);
        }
        return fList;
    }

    public void writeFT(BNODEClass n, int nodePos) throws IOException {
        String s = "";
        for (int j = 0; j < n.entries.size(); j++) {
            FrequencyTableClass f = n.entries.get(j).val;
            for (int i = 0; i < f.nodeTable.length; i++) {
                for (FrequencyTableClass.Node fn = f.nodeTable[i]; fn != null; fn = fn.next) {
                    String word = fn.key;
                    int frequency = fn.val;
                    double tf = fn.tf;
                    s = s + word + ":" + frequency + ":" + tf + "|";
                }
            }
            if (s.isEmpty()) {
                System.out.println(f.reviewText);
                System.out.println(nodePos);
            }
            s = s.substring(0, s.length() - 1) + "||";
        }
        s = s.substring(0, s.length() - 2);
        ftFile.seek(nodePos * blockSize);
        ftFile.write(s.getBytes());
    }

    public ArrayList<double[]> loadTFIDF(int position) throws IOException {
        ArrayList<double[]> temp = new ArrayList<>();
        tfidfVectorsFile.seek(position * order * 90000);
        byte[] b = new byte[order * 90000];
        tfidfVectorsFile.read(b);
        String data = new String(b).trim();
        String[] vectors = data.split("\\|");
        for (int j = 0; j < vectors.length; j++) {
            String vector = vectors[j].substring(1, vectors[j].length() - 1);
            String[] parsed = vector.split(",");
            double[] v = new double[parsed.length];
            for (int i = 0; i < parsed.length; i++) {
                double d = Double.parseDouble(parsed[i]);
                v[i] = d;
            }
            temp.add(v);
        }
        return temp;
    }

    public void writeTFIDF(BNODEClass n, int position) throws IOException {
        String s = "";
        for (int i = 0; i < n.entries.size(); i++) {
            double[] v = n.entries.get(i).val.vector;
            s = s + Arrays.toString(v) + "|";
        }
        s = s.substring(0, s.length() - 1);
        tfidfVectorsFile.seek(position * order * 90000);
        tfidfVectorsFile.write(s.getBytes());
    }

    public ArrayList<HashMap<String, BNODEClass.Node>> loadAllCluster() throws FileNotFoundException, IOException {
        ArrayList<HashMap<String, BNODEClass.Node>> temp = new ArrayList<>(10);
        for (int index = 0; index < 10; index++) {
            BigInteger bigI = BigInteger.valueOf(index);
            BigInteger bigN = BigInteger.valueOf(1000);
            BigInteger bigS = BigInteger.valueOf(8000);
            long offset1 = bigI.multiply(bigN).multiply(bigS).longValue();
            clustersFile.seek(offset1);
            byte[] bytes1 = new byte[1000 * 8000];
            clustersFile.read(bytes1);
            String data1 = new String(bytes1).trim();
            bigS = BigInteger.valueOf(90000);
            long offset = bigI.multiply(bigN).multiply(bigS).longValue();
            clusterVectorsFile.seek(offset);
            byte[] bytes2 = new byte[1000 * 90000];
            clusterVectorsFile.read(bytes2);
            String data2 = new String(bytes2).trim();
            HashMap<String, BNODEClass.Node> map = new HashMap<>();
            String[] cluster = data1.split("\\|\\|");
            String[] vectors = data2.split("\\|");
            for (int i = 0; i < cluster.length; i++) {
                String[] entry = cluster[i].split("\\|");
                if (entry.length < 3) {
                    System.out.println(cluster[i]);
                }
                String name = entry[0];
                String id = entry[1];
                BusinessCLASS b = new BusinessCLASS(id);
                b.putName(name);
                String review = entry[2];
                String vector = vectors[i].substring(1, vectors[i].length() - 1);
                String[] parsed = vector.split(",");
                double[] v = new double[parsed.length];
                for (int j = 0; j < parsed.length; j++) {
                    double d = Double.parseDouble(parsed[j]);
                    v[j] = d;
                }
                FrequencyTableClass ft = new FrequencyTableClass(review);
                ft.vector = v;
                BNODEClass.Node n = new BNODEClass.Node(b, ft);
                map.put(id, n);
            }
            temp.add(map);
        }
        return temp;
    }

    public void writeClusters(ArrayList<HashMap<String, BNODEClass.Node>> c) throws IOException {
        for (int i = 0; i < c.size(); i++) {
            String s1 = "";
            String s2 = "";
            for (Map.Entry e : c.get(i).entrySet()) {
                String id = (String) e.getKey();
                BNODEClass.Node val = (BNODEClass.Node) e.getValue();
                String name = val.key.getName();
                String review = val.val.reviewText;
                String vector = Arrays.toString(val.val.vector);
                s1 = s1 + name + "|" + id + "|" + review + "||";
                s2 = s2 + vector + "|";
            }
            s1 = s1.substring(0, s1.length() - 2);
            s2 = s2.substring(0, s2.length() - 2);
            BigInteger bigI = BigInteger.valueOf(i);
            BigInteger bigN = BigInteger.valueOf(1000);
            BigInteger bigS = BigInteger.valueOf(8000);
            long offset1 = bigI.multiply(bigN).multiply(bigS).longValue();
            clustersFile.seek(offset1); // change to long
            clustersFile.write(s1.getBytes());
            bigS = BigInteger.valueOf(90000);
            long offset2 = bigI.multiply(bigN).multiply(bigS).longValue();
            clusterVectorsFile.seek(offset2);
            clusterVectorsFile.write(s2.getBytes());
        }
    }
}