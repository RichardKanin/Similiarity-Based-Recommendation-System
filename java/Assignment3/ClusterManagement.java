/*
 * Class for cluster computations
 */
package Assignment3;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ClusterManagement {

    BTree tree;
    BNode.Node[] clusterKeys;
    ArrayList<HashMap<String, BNode.Node>> clusters;
    // for random keys
    int counter = 0;
    int index = 0;
    // for refining clusters
    double[] scores = new double[10];     // scores for 10 clusters respectively

    public ClusterManagement(BTree t) {
        tree = t;
        clusterKeys = new BNode.Node[10];     // 10 clusters
        clusters = new ArrayList<>(10);     // 10 clusters
    }

    public void generateClusters() throws IOException {
        if (tree.getFileManager().clustersFileNotReady()) {
            BNode.Node[] bestKeys = new BNode.Node[10];
            ArrayList<HashMap<String, BNode.Node>> bestClusters = new ArrayList<>(10);
            double similarityScore = 0;
            for (int i = 0; i < 10; i++) {     // 10 trials for better clusters
                clusterKeys = new BNode.Node[10];     // 10 clusters
                clusters = new ArrayList<>(10);     // 10 clusters
                counter = 0;
                index = 0;
                scores = new double[10];
                int[] randomIndex = randomTen();
                randomKeysFromTree(tree.getRoot(), randomIndex);
                assignCentroids();
                assignCluster(tree.getRoot());
                double tempScore = getSimilarityScores();
                if (tempScore > similarityScore) {
                    similarityScore = tempScore;
                    bestKeys = clusterKeys;
                    bestClusters = clusters;
                }
            }
            if (bestClusters.size() != 0) {
                clusterKeys = bestKeys;
                clusters = bestClusters;
            }
            tree.getFileManager().writeClusters(clusterKeys, clusters);
        } else {
            clusters = tree.getFileManager().clusterLoader();
            clusterKeys = loadClusterKeys();
        }
    }

    public ArrayList<BNode.Node> getClusterByID(String id) {
        ArrayList<BNode.Node> list = new ArrayList<>();
        for (HashMap<String, BNode.Node> h : clusters) {
            if (h.containsKey(id)) {
                for (Map.Entry e : h.entrySet()) {
                    list.add((BNode.Node) e.getValue());
                }
                return list;
            }
        }
        return list;
    }

    private void randomKeysFromTree(BNode n, int[] indices) {
        for (int i = 0; i < n.entries.size(); i++) {
            if (index < clusterKeys.length && counter == indices[index]) {
                clusterKeys[index] = n.entries.get(i);
                index++;
                if (index >= 10) {
                    return;
                }
            }
            counter++;
        }
        for (int i = 0; i < n.children.size(); i++) {
            randomKeysFromTree(n.children.get(i), indices);
        }
    }

    private int[] randomTen() {
        Random r = new Random();
        int[] temp = new int[10];
        for (int i = 0; i < 10; i++) {
            temp[i] = r.nextInt(tree.count());
        }
        Arrays.sort(temp);
        return temp;
    }

    private double dotProduct(double[] a, double[] b) {
        BigDecimal sum = BigDecimal.valueOf(0);
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0 && b[i] > 0) {
                BigDecimal anum = BigDecimal.valueOf(a[i]);
                BigDecimal bnum = BigDecimal.valueOf(b[i]);
                BigDecimal d = anum.multiply(bnum);
                sum = sum.add(d);
            }
        }
        return sum.doubleValue();
    }

    private void assignCluster(BNode n) {
        for (int i = 0; i < n.entries.size(); i++) {
            double dp = 0;
            int index = 0;
            boolean inserted = false;
            for (int j = 0; j < 10; j++) {
                if (n.entries.get(i).equals(clusterKeys[j])) {
                    inserted = true;
                    break;     // already in clusters, no need to assign
                } else {
                    double[] v1 = n.entries.get(i).val.vector;
                    double[] v2 = clusterKeys[j].val.vector;
                    double d = dotProduct(v1, v2);
                    if (d > dp) {
                        dp = d;
                        index = j;
                    }
                }
            }
            if (inserted) {
                continue;
            }
            scores[index] = scores[index] + dp;
            clusters.get(index).put(n.entries.get(i).key.business_id, n.entries.get(i));
        }
        for (int i = 0; i < n.children.size(); i++) {
            assignCluster(n.children.get(i));
        }
    }

    private void assignCentroids() {
        for (int i = 0; i < 10; i++) {
            if(clusterKeys[i] != null) {
                HashMap<String, BNode.Node> cluster = new HashMap<>();
                cluster.put(clusterKeys[i].key.business_id, clusterKeys[i]);
                clusters.add(cluster);
            }
        }
    }

    private double getSimilarityScores() {
        double sum = 0;
        for (int i = 0; i < scores.length; i++) {
            sum = sum + scores[i];
        }
        return sum;
    }

    private BNode.Node[] loadClusterKeys() {
        BNode.Node[] keys = new BNode.Node[10];
        int count = 0;
        for (HashMap<String, BNode.Node> m : clusters) {
            for (BNode.Node n : m.values()) {
                if (n.key.getName().startsWith("[C]")) {
                    n.key.name = n.key.getName().substring(3);
                    keys[count] = n;
                    count++;
                    break;
                }
            }
        }
        return keys;
    }

    public boolean isCentroid(BNode.Node n) {
        for (int i = 0; i < clusterKeys.length; i++) {
            if (n.equals(clusterKeys[i])) {
                return true;
            }
        }
        return false;
    }
}