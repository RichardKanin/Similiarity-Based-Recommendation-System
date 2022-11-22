/*
 * Class to load the data
 */
package Assignment2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DataLoaderCLASS {


    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        // B-Tree Creation
        BTREEClass tree = new BTREEClass(32, createsFullFileName("Tree.txt"), createsFullFileName("Frequency_Table.txt"), createsFullFileName("Vectors.txt"), createsFullFileName("Clusters.txt"), createsFullFileName("Cluster_Vectors.txt"));
        // Loading json files and
        // Loading tree files
        if (tree.getRoot().entries.isEmpty()) {
            String fullFileName = createsFullFileName("yelp_academic_dataset_review.json");
            BufferedReader br = new BufferedReader(new FileReader(fullFileName));

            String fullFileName2 = createsFullFileName("yelp_academic_dataset_business.json");

            // JSON Parser, inserting and iteration of 1000
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
                BusinessCLASS b = new BusinessCLASS(business_id);
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
                        break;
                    }
                }
                br2.close();
                FrequencyTableClass ft = new FrequencyTableClass(reviewText);
                tree.insert(b, ft);
                counter++;
            }
            System.out.println("There has been 1000 items successfully inserted");
            br.close();
        } else {
            System.out.println("Tree processed from file");
        }

        // Calculate TF-IDF
        tree.computeTF();
        System.out.println("The Frequency table has been processed and is ready");
        TFIDFCLASS tfidf = new TFIDFCLASS(tree);
        tfidf.computeIDF();
        tfidf.computeTFIDF();
        System.out.println("The Vectors of TD-IDF have been processed and is ready");

        ClusterCLASS ch = new ClusterCLASS(tree);
        ch.clusterGeneration();
        System.out.println("The Clusters have been processed as is ready");
    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }

}