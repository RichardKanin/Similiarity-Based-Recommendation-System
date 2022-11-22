/*
class that operates through files
 */
package Assignment3;

import Assignment3.Graph.Edge;
import Assignment3.Graph.Vertex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class FileOperator {

    private int order;
    public int blocks;
    private RandomAccessFile raf;
    private int blockSize;
    ArrayList<Integer> availablePositions;

    private RandomAccessFile ftFile;
    private boolean ftReady;

    private RandomAccessFile tfidfVectorsFile;
    private boolean tfidfVectorsReady;

    private RandomAccessFile clustersFile;
    private boolean clustersReady;

    private RandomAccessFile clusterVectorsFile;

    private RandomAccessFile graphFile;
    private boolean graphReady;

    // persistent FT, TDIDF
    public FileOperator(int order, String filepath, String ft, String tfidf, String clusters, String clusterVectors, String graph) throws FileNotFoundException, IOException {
        this.order = order;
        blockSize = ((order - 1) * 8000) + (order * (4 + 1));
        blocks = 0;
        availablePositions = new ArrayList<>();
        raf = new RandomAccessFile(filepath, "rw");
        ftFile = new RandomAccessFile(ft, "rw");
        if (ftFile.read() == -1) {
            ftReady = false;
        } else {
            ftReady = true;
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
        graphFile = new RandomAccessFile(graph, "rw");
        if (graphFile.read() == -1) {
            graphReady = false;
        } else {
            graphReady = true;
        }
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

    public int peek(int i) throws IOException {
        return raf.read();
    }

    public BNode read(int position) throws IOException {
        BNode n = bytesToNode(readFromRAF(position));
        n.position = position;
        return n;
    }

    public void write(BNode n) throws IOException {
        byte[] nodeBytes = nodeToBytes(n);
        writeToRAF(nodeBytes, n.position);
    }

    public void erase(BNode n) throws IOException {
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
            //checker, just in case something happens
            System.out.println("Something went wrong when erasing a node.");
            System.exit(-1);
        }
    }

    private byte[] nodeToBytes(BNode node) {
        //format in which being constructed
        // name|id|review ... || child1 || ...
        String s = "";
        s = s + "[" + node.entries.size() + "]";
        for (int i = 0; i < node.entries.size(); i++) {
            Business b = node.entries.get(i).key;
            FrequencyTable ft = node.entries.get(i).val;
            //Longitude and Latitude Addition
            String entry = b.name + "|" + b.business_id + "|" + b.latitude + "|" + b.longitude + "|" + ft.reviewText + "||";
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

    private BNode bytesToNode(byte[] b) throws IOException {     // recursive
        // name|id|review ||  child1 || ...
        // bytes -> String
        BNode n = new BNode(order, this);
        String s = new String(b).trim();
        int numberOfEntries = Integer.parseInt(s.substring(1, s.indexOf("]")));
        String[] split = s.substring(s.indexOf("]") + 1).split("\\|\\|");
        for (int i = 0; i < numberOfEntries; i++) {
            String[] entry = split[i].split("\\|");
            Business business = new Business(entry[1]);
            business.putName(entry[0]);
            business.putLocation(Float.parseFloat(entry[2]), Float.parseFloat(entry[3]));
            FrequencyTable ft = new FrequencyTable(entry[4]);
            n.insert(business, ft);
        }
        int numberOfChildren = Integer.parseInt(split[numberOfEntries].substring(1, split[numberOfEntries].indexOf("]")));
        for (int i = numberOfEntries + 1; i < numberOfChildren + numberOfEntries + 1; i++) {
            int childPos = Integer.parseInt(split[i]);
            BNode child = bytesToNode(readFromRAF(childPos));
            child.position = childPos;
            n.children.add(child);
        }
        return n;
    }

    //functions to keep methods in check,
    // throws exception if not ready
    public boolean ftFileNotReady() throws IOException {
        return !ftReady;
    }

    public boolean tfidfFileNotReady() throws IOException {
        return !tfidfVectorsReady;
    }

    public boolean clustersFileNotReady() throws IOException {
        return !clustersReady;
    }

    public boolean graphFileNotReady() throws IOException {
        return !graphReady;
    }

    public ArrayList<FrequencyTable> loadFT(int nodePos) throws IOException {
        ArrayList<FrequencyTable> fList = new ArrayList<>();
        ftFile.seek(nodePos * blockSize);
        byte[] b = new byte[blockSize];
        ftFile.read(b);
        String data = new String(b).trim();
        String[] parsed = data.split("\\|\\|");
        for (int i = 0; i < parsed.length; i++) {
            FrequencyTable temp = new FrequencyTable("");
            String[] nodes = parsed[i].split("\\|");
            for (int j = 0; j < nodes.length; j++) {
                String[] ftNode = nodes[j].split(":");
                temp.load(ftNode[0], Integer.parseInt(ftNode[1]), Double.parseDouble(ftNode[2]));
            }
            fList.add(temp);
        }
        return fList;
    }

    public void frequencyTableWRITER(BNode n, int nodePos) throws IOException {
        String s = "";
        for (int j = 0; j < n.entries.size(); j++) {
            FrequencyTable f = n.entries.get(j).val;
            for (int i = 0; i < f.table.length; i++) {
                for (FrequencyTable.Node fn = f.table[i]; fn != null; fn = fn.next) {
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

    public ArrayList<double[]> TFIDF_LOADER(int position) throws IOException {
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

    public void write_tfidf(BNode n, int position) throws IOException {
        String s = "";
        for (int i = 0; i < n.entries.size(); i++) {
            double[] v = n.entries.get(i).val.vector;
            s = s + Arrays.toString(v) + "|";
        }
        s = s.substring(0, s.length() - 1);
        tfidfVectorsFile.seek(position * order * 90000);
        tfidfVectorsFile.write(s.getBytes());
    }

    public ArrayList<HashMap<String, BNode.Node>> clusterLoader() throws FileNotFoundException, IOException {
        ArrayList<HashMap<String, BNode.Node>> temp = new ArrayList<>(10);
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
            HashMap<String, BNode.Node> map = new HashMap<>();
            String[] cluster = data1.split("\\|\\|");
            String[] vectors = data2.split("\\|");
            for (int i = 0; i < cluster.length; i++) {
                String[] entry = cluster[i].split("\\|");
                String id;
                Business b;
                String review;
                if (entry.length > 5) {
                    String name = "[C]" + entry[1];
                    id = entry[2];
                    b = new Business(id);
                    b.putName(name);
                    b.latitude = Double.parseDouble(entry[3]);
                    b.longitude = Double.parseDouble(entry[4]);
                    review = entry[5];
                } else {
                    String name = entry[0];
                    id = entry[1];
                    b = new Business(id);
                    b.putName(name);
                    b.latitude = Double.parseDouble(entry[2]);
                    b.longitude = Double.parseDouble(entry[3]);
                    review = entry[4];
                }
                String vector = vectors[i].substring(1, vectors[i].length() - 1);
                String[] parsed = vector.split(",");
                double[] v = new double[parsed.length];
                for (int j = 0; j < parsed.length; j++) {
                    double d = Double.parseDouble(parsed[j]);
                    v[j] = d;
                }
                FrequencyTable ft = new FrequencyTable(review);
                ft.vector = v;
                BNode.Node n = new BNode.Node(b, ft);
                map.put(id, n);
            }
            temp.add(map);
        }
        return temp;
    }

    public void writeClusters(BNode.Node[] centroids, ArrayList<HashMap<String, BNode.Node>> c) throws IOException {
        for (int i = 0; i < c.size(); i++) {
            String s1 = "";
            String s2 = "";
            for (Map.Entry e : c.get(i).entrySet()) {
                String id = (String) e.getKey();
                BNode.Node val = (BNode.Node) e.getValue();
                String name = val.key.getName();
                String review = val.val.reviewText;
                String vector = Arrays.toString(val.val.vector);
                if (isCentroid(centroids, (BNode.Node) e.getValue())) {
                    s1 = s1 + "c|";
                }
                s1 = s1 + name + "|" + id + "|" + val.key.latitude + "|" + val.key.longitude + "|" + review + "||";
                s2 = s2 + vector + "|";
            }
            s1 = s1.substring(0, s1.length() - 2);
            s2 = s2.substring(0, s2.length() - 2);
            BigInteger bigI = BigInteger.valueOf(i);
            BigInteger bigN = BigInteger.valueOf(1000);
            BigInteger bigS = BigInteger.valueOf(8000);
            long offset1 = bigI.multiply(bigN).multiply(bigS).longValue();
            //clusterFile.seek method contain parameter type long
            clustersFile.seek(offset1);
            clustersFile.write(s1.getBytes());
            bigS = BigInteger.valueOf(90000);
            long offset2 = bigI.multiply(bigN).multiply(bigS).longValue();
            clusterVectorsFile.seek(offset2);
            clusterVectorsFile.write(s2.getBytes());
        }
    }

    private boolean isCentroid(BNode.Node[] centroids, BNode.Node value) {
        for (int i = 0; i < centroids.length; i++) {
            if (centroids[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    //method to load the graph being created
    public ArrayList<Vertex> graphLoader() throws FileNotFoundException, IOException {
        ArrayList<Vertex> temp = new ArrayList<>();
        boolean end = false;
        int count = 0;
        while (!end) {
            BigInteger bigI = BigInteger.valueOf(count);
            BigInteger bigS = BigInteger.valueOf(10000);
            int offset = bigI.multiply(bigS).intValue();
            if (offset < graphFile.length()) {
                graphFile.seek(offset);
                byte[] bytes = new byte[10000];
                graphFile.read(bytes);
                String data = new String(bytes).trim();
                String[] vertex = data.split("\\|\\|");
                String[] entry = vertex[0].split("\\|");
                String[] edges = new String[8];
                try {
                    edges = vertex[1].split("\\|");
                } catch (Exception e) {
                    System.out.println(data);
                }
                Business b;
                String review;
                boolean isCentroid = false;
                if (entry.length > 5) {
                    String name = entry[1];
                    String id = entry[2];
                    b = new Business(id);
                    b.putName(name);
                    b.latitude = Double.parseDouble(entry[3]);
                    b.longitude = Double.parseDouble(entry[4]);
                    review = entry[5];
                    isCentroid = true;
                } else {
                    String name = entry[0];
                    String id = entry[1];
                    b = new Business(id);
                    b.putName(name);
                    b.latitude = Double.parseDouble(entry[2]);
                    b.longitude = Double.parseDouble(entry[3]);
                    review = entry[4];
                }
                FrequencyTable ft = new FrequencyTable(review);
                Vertex v = new Vertex(count,b, ft);
                v.isCentroid = isCentroid;
                for (int j = 0; j < edges.length; j = j + 3) {
                    int dest = Integer.parseInt(edges[j]);
                    double dist = Double.parseDouble(edges[j + 1]);
                    double cost = Double.parseDouble(edges[j + 2]);
                    v.links[j / 3] = new Edge(dest, dist, cost);
                }
                temp.add(v);
                count++;
            } else {
                end = true;
            }
        }
        return temp;
    }

    // method to write graph
    public void graphWriter(ArrayList<Vertex> graph) throws IOException {
        for (int i = 0; i < graph.size(); i++) {
            String s = "";
            String id = graph.get(i).business.getID();
            String name = graph.get(i).business.getName();
            String review = graph.get(i).frequencyTable.reviewText;
            //this writes to centroid as well,
            // following a checker to check if it's a centroid
            if (graph.get(i).isCentroid) {
                s = s + "c|";
            }
            s = s + name + "|" + id + "|" + graph.get(i).business.latitude + "|" + graph.get(i).business.longitude + "|" + review + "||";
            for (int j = 0; j < 4; j++) {
                String dest = String.valueOf(graph.get(i).links[j].destination);
                String dist = String.valueOf(graph.get(i).links[j].distance);
                String cost = String.valueOf(graph.get(i).links[j].cost);
                s = s + dest + "|" + dist + "|" + cost + "|";
            }
            s = s.substring(0, s.length() - 1) + System.getProperty("line.separator");
            BigInteger bigI = BigInteger.valueOf(i);
            BigInteger bigS = BigInteger.valueOf(10000);
            long offset = bigI.multiply(bigS).longValue();
            graphFile.seek(offset);
            graphFile.write(s.getBytes());
        }
    }
}