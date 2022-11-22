/*
* Graph class
 */
package Assignment3;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;


public class Graph {

    static class Vertex {

        int index;
        Business business;
        FrequencyTable frequencyTable;
        boolean isCentroid;
        Edge[] links;

        Vertex(int i, Business b, FrequencyTable ft) {
            index = i;
            business = b;
            frequencyTable = ft;
            isCentroid = false;
            links = new Edge[4];
        }

        public String toString() {
            String s = business.getName() + ": ";
            for (int i = 0; i < 4; i++) {
                if (links[i] != null) {
                    s = s + links[i].toString();
                }
            }
            return s;
        }

    }

    static class Edge implements Comparable {

        int destination;
        double distance;
        double cost;

        Edge(int i, double d, double c) {
            destination = i;
            distance = d;
            cost = c;
        }

        public String toString() {
            return "[" + destination + ":" + distance + "]";
        }

        @Override
        public int compareTo(Object o) {
            Edge another = (Edge) o;
            Double c = cost;
            if (c.compareTo(another.cost) != 0) {
                return Math.negateExact(c.compareTo(another.cost));
            }
            Double d = distance;
            return d.compareTo(another.distance);
        }
    }

    // Dijkstra implementation
    static class Dijkstra {

        int vertex;
        boolean known;
        double cost;
        int path;

        Dijkstra(int v) {
            vertex = v;
            known = false;
            cost = 0;
            path = -1;
        }

    }

    ArrayList<Vertex> graph;
    private ArrayList<Integer> notIncluded;
    ArrayList<Dijkstra> paths;

    public Graph(ClusterManagement c) throws IOException {
        graph = new ArrayList<>();
        initiateGraph(c);
    }

    private void initiateGraph(ClusterManagement c) throws IOException {
        if (c.tree.getFileManager().graphFileNotReady()) {
            // let all the valid businesses be available vertices
            int count = 0;
            for (HashMap<String, BNode.Node> m : c.clusters) {
                for (BNode.Node n : m.values()) {
                    Vertex v = new Vertex(count, n.key, n.val);
                    if (c.isCentroid(n)) {
                        v.isCentroid = true;
                    }
                    graph.add(v);
                    count++;
                }
            }
            // in terms of getting four geographically closest neighbors
            // start finding links
            for (int i = 0; i < graph.size(); i++) {
                findLinks(i);
            }
            c.tree.getFileManager().graphWriter(graph);
        } else {
            graph = c.tree.getFileManager().graphLoader();
        }
        System.out.println("The Graph is now Ready");
        // constructing a connectivity check
        // in order to report the number of disjoint sets
        // connectivity check: report the number of disjoint sets
        notIncluded = new ArrayList<>();
        IntStream.range(0, graph.size()).forEach(val -> notIncluded.add(val));
        ArrayList<Set<Integer>> sets = findDisjointSets();
        System.out.println("Number of disjoint sets: " + sets.size());
    }

    private void findLinks(int index) {
        // traverse the graph, calculate the distance, update links if shorter
        //traverse through the graph, hence the loop
        // calculate the distance
        // then update links if distance is shorter
        for (int i = index + 1; i < graph.size(); i++) {
            double tempDistance = haversine(graph.get(index), graph.get(i));
            updateLinks(index, i, tempDistance);
            updateLinks(i, index, tempDistance);
        }
    }

    private void updateLinks(int self, int another, double tempDistance) {
        // cost = cosine similarity, higher -> more similar -> preferred
        double cost = dotProduct(graph.get(self).frequencyTable.vector, graph.get(another).frequencyTable.vector);
        for (int i = 0; i < 4; i++) {
            if (graph.get(self).links[i] == null) {     // empty spots available, add link
                graph.get(self).links[i] = new Edge(another, tempDistance, cost);
                return;
            }
        }
        // no empty spot, add if shorter
        Arrays.sort(graph.get(self).links);
        if (tempDistance < graph.get(self).links[3].distance) {
            graph.get(self).links[3] = new Edge(another, tempDistance, cost);
        }
    }

    private double haversine(Vertex v1, Vertex v2) {

        // parameters for calculations
        double lat1 = v1.business.latitude;
        double lon1 = v1.business.longitude;
        double lat2 = v2.business.latitude;
        double lon2 = v2.business.longitude;

        // calculation of longitude and latitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);


        //radian conversion
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.pow(Math.sin(dLon / 2), 2)
                * Math.cos(lat1)
                * Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
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

    private ArrayList<Set<Integer>> findDisjointSets() {
        ArrayList<Set<Integer>> sets = new ArrayList<>();
        // while ensures every vertex is included
        while (setsCount(sets) < graph.size()) {
            int randomIndex = (int) (Math.random() * notIncluded.size());
            int randomRoot = notIncluded.get(randomIndex);
            Set<Integer> s = oneDisjointSet(randomRoot, new HashSet<>());
            sets.add(s);
        }
        return sets;
    }

    private int setsCount(ArrayList<Set<Integer>> sets) {
        int count = 0;
        for (Set<Integer> s : sets) {
            count = count + s.size();
        }
        return count;
    }

    private Set<Integer> oneDisjointSet(int root, Set<Integer> existing) {
        // start from the root, add all links to the set
        // ignore those that are added or in another set already
        Set<Integer> s = existing;
        s.add(root);
        notIncluded.remove(Integer.valueOf(root));
        for (Edge e : graph.get(root).links) {
            if (!s.contains(e.destination) && notIncluded.contains(e.destination)) {
                s.add(e.destination);
                notIncluded.remove(Integer.valueOf(e.destination));
                s.addAll(oneDisjointSet((int) e.destination, s));
            }
        }
        return s;
    }

    private void initiatePaths() {
        // clear Dijkstra table
        paths = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            paths.add(new Dijkstra(i));
        }
    }

    public int search(String id) {
        for (int i = 0; i < graph.size(); i++) {
            if (graph.get(i).business.getID().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<String> generateOutput(String id) {
        initiatePaths();
        int start = search(id);
        // check
        if (graph.get(start).isCentroid) {
            ArrayList<Integer> path = new ArrayList<>();
            ArrayList<String> display = displayPath(start, path);
            return display;
        } else {
            // having the shortest path
            // means that it's the most similar
            runDijkstra(start, 0);
            double pathCost = 0;
            ArrayList<Integer> path = new ArrayList<>();
            for (Dijkstra d : paths) {
                if (graph.get(d.vertex).isCentroid) {
                    //if centroid is successfully found
                    // re-do the path
                    ArrayList<Integer> tempPath = reconstructPath(d.vertex);
                    Collections.reverse(tempPath);
                    double tempCost = d.cost;
                    if (tempCost > pathCost) {
                        path = tempPath;
                        pathCost = tempCost;
                    }
                }
            }
            if (path.isEmpty()) {
                // given that if the path is empty,
                // you will be unable to reach any centroids
                ArrayList<String> display = displayPath(-1, path);
                return display;
            } else {
                // else, the path is not empty,
                // and the path is normal
                ArrayList<String> display = displayPath(start, path);
                return display;
            }
        }
    }

    private void runDijkstra(int start, double accumulate) {
        paths.get(start).known = true;
        Edge[] links = graph.get(start).links;
        for (int i = 0; i < links.length; i++) {
            if (paths.get(links[i].destination).known) {
                continue;     // skip if path is known
            }
            if (paths.get(links[i].destination).path == -1) {
                // new entry, set cost and path directly
                paths.get(links[i].destination).cost += (accumulate + links[i].cost);
                paths.get(links[i].destination).path = start;
            } else {
                // there is a path already -> compare cost, replace if higher
                if ((accumulate + links[i].cost) > paths.get(links[i].destination).cost) {     // more similar
                    paths.get(links[i].destination).cost += (accumulate + links[i].cost);
                    paths.get(links[i].destination).path = start;
                }
            }
        }
        for (int i = 0; i < links.length; i++) {
            if (paths.get(links[i].destination).known) {
                continue;     // skip if path is known
            }
            // run until all reachable vertices are reached
            runDijkstra(links[i].destination, paths.get(links[i].destination).cost);
        }
    }

    private ArrayList<String> displayPath(int start, ArrayList<Integer> path) {
        ArrayList<String> output = new ArrayList<>();
        if (path.isEmpty()) {
            if (start == -1) {     // cannot reach any centroid
                String pString = "Path: N/A";
                String s = "Unfortunately, currently this node is unable to reach any of the cluster centers.";
                output.add(pString);
                output.add(s);
                return output;
            } else {
                // itself is centroid
                String pString = "Path: \n" + graph.get(start).business.name;
                String pCost = "\nCost: Not Available, given that this node is a cluster center, there is not need to travel.";
                String s = "\nBusiness: " + graph.get(start).business.getName() + "\n\nReviews:\n\n" + graph.get(start).frequencyTable.reviewText + "\n";
                output.add(pString);
                output.add(pCost);
                output.add(s);
                return output;
            }
        } else {     // normal path
            String pString = generatePathString(path);
            String pCost = "\nCost (sum of similarity values): " + paths.get(path.get(path.size() - 1)).cost;
            output.add(pString);
            output.add(pCost);
            String s = "";
            for (int i = 0; i < path.size(); i++) {
                if (i == path.size() - 1) {
                    s = "\nBusiness: " + graph.get(path.get(i)).business.getName() + "\n\nReviews:\n\n" + graph.get(path.get(i)).frequencyTable.reviewText + "\n";
                    output.add(s);
                } else {
                    s = "\nBusiness: " + graph.get(path.get(i)).business.getName() + "\n\nReviews:\n\n" + graph.get(path.get(i)).frequencyTable.reviewText + "\n";
                    for (int j = 0; j < graph.get(path.get(i)).links.length; j++) {
                        if (graph.get(path.get(i)).links[j].destination == path.get(i + 1)) {
                            s += "\nCost to next node: " + graph.get(path.get(i)).links[j].cost;
                            output.add(s);
                            break;
                        }
                    }
                }
            }
            return output;
        }
    }

    private ArrayList<Integer> reconstructPath(int last) {
        //Using the Dijkstra table created
        //track back from the centroid
        ArrayList<Integer> temp = new ArrayList<>();
        temp.add(last);
        if (paths.get(last).path != -1) {
            temp.addAll(reconstructPath(paths.get(last).path));
        }
        return temp;
    }

    private String generatePathString(ArrayList<Integer> path) {
        String s = "Path: \n";
        for (int i = 0; i < path.size(); i++) {
            s = s + graph.get(path.get(i)).business.getName() + " -> ";
        }
        s = s.substring(0, s.length() - 4);
        return s;
    }

}