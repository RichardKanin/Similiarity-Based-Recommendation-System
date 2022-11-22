/*
 * Java class to handle, assign and generate my Clusters
 */

package Assignment2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class
ClusterCLASS {

    BTREEClass tree;
    BNODEClass.Node[] keyClusters;
    ArrayList<HashMap<String, BNODEClass.Node>> clusters;
    // for random keys
    int numberCounter = 0;
    int index = 0;
    // scores declared and intialized
    // to be used to score 10 of the random clusters picked
    double[] scores = new double[10];

    public ClusterCLASS(BTREEClass t) {
        tree = t;
        //made a cluster of 10
        keyClusters = new BNODEClass.Node[10];     // 10 clusters
        clusters = new ArrayList<>(10);     // 10 clusters
    }


    public void clusterGeneration() throws IOException {
        //generate clusters method
        // constructed to generate and assign clusters and calls centroid method
        if (tree.getFileManager().clustersFileNotReady()) {
            BNODEClass.Node[] bestKeys = new BNODEClass.Node[10];
            ArrayList<HashMap<String, BNODEClass.Node>> bestClusters = new ArrayList<>(10);
            double similarityScore = 0;
            //iterates through 10 times
            for (int i = 0; i < 10; i++) {
                //this is where i declare my 10 clusters
                keyClusters = new BNODEClass.Node[10];
                // 10 clusters arraylist formation
                clusters = new ArrayList<>(10);
                numberCounter = 0;
                index = 0;
                scores = new double[10];
                int[] randomIndex = generate10Randoms();
                getKeyRandomFromTree(tree.getRoot(), randomIndex);
                centroidAssignment();
                clusterAssignment(tree.getRoot());
                double tempScore = obtainScoresFromSimilarity();
                if (tempScore > similarityScore) {
                    similarityScore = tempScore;
                    bestKeys = keyClusters;
                    bestClusters = clusters;
                }
            }
            keyClusters = bestKeys;
            clusters = bestClusters;
            tree.getFileManager().writeClusters(clusters);
        } else {
            clusters = tree.getFileManager().loadAllCluster();
        }
    }
    private int[] generate10Randoms() {
        Random r = new Random();
        int[] temp = new int[10];
        for (int i = 0; i < 10; i++) {
            temp[i] = r.nextInt(tree.count());
        }
        Arrays.sort(temp);
        return temp;
    }
    public ArrayList<BNODEClass.Node> retrieveClusterByID(String id) {
        ArrayList<BNODEClass.Node> nodeArrayList = new ArrayList<>();
        for (HashMap<String, BNODEClass.Node> h : clusters) {
            if (h.containsKey(id)) {
                for (Map.Entry e : h.entrySet()) {
                    nodeArrayList.add((BNODEClass.Node) e.getValue());
                }
                return nodeArrayList;
            }
        }
        return nodeArrayList;
    }

    private void getKeyRandomFromTree(BNODEClass n, int[] indices) {
        for (int i = 0; i < n.entries.size(); i++) {
            if (index < keyClusters.length && numberCounter == indices[index]) {
                keyClusters[index] = n.entries.get(i);
                index++;
                if (index >= 10) {
                    return;
                }
            }
            numberCounter++;
        }
        for (int i = 0; i < n.children.size(); i++) {
            getKeyRandomFromTree(n.children.get(i), indices);
        }
    }



    private double dotProductFunction(double[] a, double[] b) {
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

    private void clusterAssignment(BNODEClass n) {
        for (int i = 0; i < n.entries.size(); i++) {
            double dp = 0;
            int index = 0;
            boolean inserted = false;
            for (int j = 0; j < 10; j++) {
                if(keyClusters[j] != null) {
                    if (n.entries.get(i).equals(keyClusters[j])) {
                        inserted = true;
                        break;     // already in clusters, no need to assign
                    } else {
                        double[] v1 = n.entries.get(i).val.vector;
                        double[] v2 = keyClusters[j].val.vector;
                        double d = dotProductFunction(v1, v2);
                        if (d > dp) {
                            dp = d;
                            index = j;
                        }
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
            clusterAssignment(n.children.get(i));
        }
    }

    private void centroidAssignment() {
        for (int i = 0; i < 10; i++) {
            if( keyClusters[i] != null) {
                HashMap<String, BNODEClass.Node> cluster = new HashMap<>();
//            System.out.println(clusterKeys);
//            System.out.println(clusterKeys[i]);
                cluster.put(keyClusters[i].key.business_id, keyClusters[i]);
                clusters.add(cluster);
            }
        }
    }

    private double obtainScoresFromSimilarity() {
        double sum = 0;
        for (int i = 0; i < scores.length; i++) {
            sum = sum + scores[i];
        }
        return sum;
    }

}