/*
data class to load the data
 */
package Assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DataLoader {
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        // create a b-tree
        BTree tree = new BTree(32, createsFullFileName("tree.txt"), createsFullFileName("ft.txt"), createsFullFileName("vectors.txt"), createsFullFileName("clusters.txt"), createsFullFileName("clusterVectors.txt"), createsFullFileName("graph.txt"));
        // load json files or load tree file
        if (tree.getRoot().entries.isEmpty()) {
            String fullFileName = createsFullFileName("yelp_academic_dataset_review.json");
            BufferedReader br = new BufferedReader(new FileReader(fullFileName));

            String fullFileName2 = createsFullFileName("yelp_academic_dataset_business.json");

            // JSON Parser
            int counter = 0;
            while (counter < 1000) {
                String jsonString = br.readLine();
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(jsonString);
                String business_id = (String) obj.get("business_id");
                String reviewText = (String) obj.get("text");
                if (!reviewText.replaceAll("\\p{Punct}", "").substring(0, 1).matches("[a-zA-Z]*")) {
                    continue;
                }
                Business b = new Business(business_id);
                BufferedReader br2 = new BufferedReader(new FileReader(fullFileName2));
                String bString;
                while ((bString = br2.readLine()) != null) {
                    JSONParser parser2 = new JSONParser();
                    JSONObject obj2 = (JSONObject) parser2.parse(bString);
                    String id = (String) obj2.get("business_id");
                    String name = (String) obj2.get("name");
                    if (name.contains("|")) {
                        continue;
                    }
                    if (id.equals(business_id)) {
                        b.putName(name);
                        // for computing geographic location
                        double latitude = (double) obj2.get("latitude");
                        double longitude = (double) obj2.get("longitude");
                        b.putLocation(latitude, longitude);
                        break;
                    }
                }
                br2.close();
                FrequencyTable ft = new FrequencyTable(reviewText);
                tree.insert(b, ft);
                counter++;
            }
            System.out.println("1000 items inserted");
            br.close();
        } else {
            System.out.println("The Tree has been processed from file");
        }

        // Calculate TF-IDF
        tree.computeTF();
        //print statement as status notification
        System.out.println("Frequency Table Has Been Processed and is Ready");
        TFIDFClass tfidf = new TFIDFClass(tree);
        tfidf.computeIDF();
        tfidf.computeTFIDF();
        //print statement as status notification
        System.out.println("The TD-IDF Vectors have Processed and is Ready");
        // clustering
       ClusterManagement ch = new ClusterManagement(tree);
        ch.generateClusters();
        //print statement as status notification
        System.out.println("Clusters Ready");
        Graph graph = new Graph(ch);
    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }

}