package Assignment1;



/*
 * CSC365 Richard Kanin Assignment 1
 */


        import java.io.BufferedReader;
        import java.io.FileNotFoundException;
        import java.io.FileReader;
        import java.io.IOException;

        import org.json.simple.JSONObject;
        import org.json.simple.parser.JSONParser;
        import org.json.simple.parser.ParseException;


public class MainAssignmentClass {

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        // Hash Table creation followed by JSON parser
        HashTableClass ht = new HashTableClass();

        String fullFileName = createsFullFileName("yelp_academic_dataset_review.json");
        BufferedReader br = new BufferedReader(new FileReader(fullFileName));
        // The JSON Parser
        int counter = 0;
        // while loop set to parse the review json file up to 777 objects
        while (counter < 777) {
            String jsonStringObject = br.readLine();
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStringObject);
            String business_id = (String) obj.get("business_id");
            String reviewText = (String) obj.get("text");
            BusinessClass b = new BusinessClass(business_id);
            FrequencyTableClass ft = new FrequencyTableClass(reviewText);
            ht.put(b, ft);
            counter++;
        }
        br.close();
        // Associate the names of business with ID accordingly
        fullFileName = createsFullFileName("yelp_academic_dataset_business.json");
        // using a buffer reader below
        br = new BufferedReader(new FileReader(fullFileName));
        int b = 0;
        String jsonString;
        while ((jsonString = br.readLine()) != null) {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonString);
            String business_id = (String) obj.get("business_id");
            String name = (String) obj.get("name");
            b = ht.findBusiness(business_id, name, b);
        }
        br.close();
        // used to calculate the TF-IDF
        ht.computeTF();
        TFIDFClass tfidfClass = new TFIDFClass(ht);
        tfidfClass.inverseDocumentFrequencyComputation();
        tfidfClass.computeTFIDF();
    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        //goes into user's home directory system
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }


    private static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    static class Pair<A, B> implements Comparable {

        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A a() {
            return a;
        }

        public B b() {
            return b;
        }

        public String toString() {
            return "(" + a + "," + b + ")";
        }

        @Override
        public int compareTo(Object o) {
            Pair anotherPair = (Pair) o;
            if ((Double) this.a < (Double) anotherPair.a()) {
                return -1;
            }
            if ((Double) this.a > (Double) anotherPair.a()) {
                return 1;
            }
            return 0;
        }

    }

}
