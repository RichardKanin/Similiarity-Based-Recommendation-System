/*
 * additional
 */
package Assignment2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class zAdditionalDataClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        // create a b-tree
        BTREEClass tree = new BTREEClass(32, fullFileNameCreation("Tree.txt"), fullFileNameCreation("Frequency_Table.txt"), fullFileNameCreation("Vectors.txt"), fullFileNameCreation("Clusters.txt"), fullFileNameCreation("Cluster_Vectors.txt"));
        // load json files or load tree file
        if (tree.getRoot().entries.isEmpty()) {
            String fullFileName = fullFileNameCreation("yelp_academic_dataset_review.json");
            BufferedReader br = new BufferedReader(new FileReader(fullFileName));
            String fullFileName2 = fullFileNameCreation("yelp_academic_dataset_business.json");
            // JSON Parser
            int counter = 0;
            while (counter < 1000) {
                String jsonString = br.readLine();
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(jsonString);
                String business_id = (String) obj.get("business_id");
                String reviewText = (String) obj.get("text");
                if (!reviewText.replaceAll("\\p{Punct}", "").substring(0, 1).matches("[a-zA-Z]*")) continue;
                BusinessCLASS b = new BusinessCLASS(business_id);
                BufferedReader br2 = new BufferedReader(new FileReader(fullFileName2));
                String bString;
                while ((bString = br2.readLine()) != null) {
                    JSONParser parser2 = new JSONParser();
                    JSONObject obj2 = (JSONObject) parser2.parse(bString);
                    String id = (String) obj2.get("business_id");
                    String name = (String) obj2.get("name");
                    if (id.equals(business_id)) {
                        b.putName(name);
                        break;
                    }
                }
                br2.close();
                FrequencyTableClass ft = new FrequencyTableClass(reviewText);
                tree.insert(b, ft);
                counter++;
            }
            System.out.println("5000 items inserted");
            br.close();
        } else {
            System.out.println("Tree processed from file");
        }

        // Calculate TF-IDF
        tree.computeTF();
        System.out.println("Frequency Table Ready");
        TFIDFCLASS tfidf = new TFIDFCLASS(tree);
        tfidf.computeIDF();
        tfidf.computeTFIDF();
        System.out.println("TD-IDF Vectors Ready");

        ClusterCLASS ch = new ClusterCLASS(tree);
        ch.clusterGeneration();
        System.out.println("Clusters Ready");
    }

    private static String fullFileNameCreation(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }

}