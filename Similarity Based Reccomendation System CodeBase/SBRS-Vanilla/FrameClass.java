package Assignment1;


/*
 * Frame for the GUI for Assignment 1
 */

// imports for my Graphical User Interface, dot product calculation,
// and JSON file parser
        import java.awt.BorderLayout;
        import java.awt.Color;
        import java.awt.Container;
        import java.awt.FlowLayout;
        import java.awt.GridLayout;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import javax.swing.JButton;
        import javax.swing.JFrame;
        import javax.swing.JPanel;
        import javax.swing.JTextArea;
        import javax.swing.JTextField;
        import java.io.BufferedReader;
        import java.io.FileNotFoundException;
        import java.io.FileReader;
        import java.io.IOException;
        import java.math.BigDecimal;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;
        import java.util.logging.Level;
        import java.util.logging.Logger;
        import javax.swing.JScrollPane;
        import org.json.simple.JSONObject;
        import org.json.simple.parser.JSONParser;
        import org.json.simple.parser.ParseException;


public class FrameClass extends JFrame implements ActionListener {

    JButton welcome;
    JButton clearPanel;
    JTextArea selectedEntry;
    JTextArea similarEntry;
    JTextField input;

    HashTableClass ht;
    TFIDFClass tfidfClass;

    static private Color BabyBlue = new Color(51,204,255);

    public FrameClass(String title) {
        super(title);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addComponents(getContentPane());
        addListeners();
        setVisible(true);
        setResizable(false);

        try {
            prepareData();
        } catch (ParseException ex) {
            Logger.getLogger(FrameClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FrameClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addComponents(Container contentPane) {
        // northern region
        welcome = new JButton("Welcome to the Similarity-Based Business Recommendation System!");
        clearPanel = new JButton("CLEAR");
        input = new JTextField();
        input.setColumns(50);
        JPanel northernSection = new JPanel();
        northernSection.setLayout(new FlowLayout());
        northernSection.add(welcome);
        northernSection.add(input);
        northernSection.add(clearPanel);
        northernSection.setBackground(BabyBlue);


        //clear button color
        clearPanel.setBackground(Color.GREEN);
        clearPanel.setForeground(Color.BLACK);
        welcome.setBackground(Color.RED);
        welcome.setForeground(Color.black);

        // central region
        int regionWidth = 700;
        int regionHeight = 400; // adjust these two number if you need to
        selectedEntry = new JTextArea(regionWidth, regionHeight);
        selectedEntry.setBackground(BabyBlue);
        selectedEntry.setForeground(Color.BLACK);
        //scroll bar creation
        JScrollPane scrollPane = new JScrollPane(selectedEntry);
        scrollPane.setBackground(Color.RED);
        selectedEntry.setWrapStyleWord(true);
        selectedEntry.setLineWrap(true);
        similarEntry = new JTextArea(regionWidth, regionHeight);
        //scroll bar for similarity region
        JScrollPane scrollPane2 = new JScrollPane(similarEntry);
        scrollPane2.setBackground(Color.RED);
        //
        similarEntry.setBackground(BabyBlue);
        similarEntry.setForeground(Color.BLACK);
        similarEntry.setWrapStyleWord(true);
        similarEntry.setLineWrap(true);
        Container centralRegion = new Container();
        // central Region controls
        centralRegion.setLayout(new GridLayout(2, 1, 6, 6));
        centralRegion.add(scrollPane);
        centralRegion.add(scrollPane2);
        centralRegion.setBackground(Color.BLACK);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(northernSection, BorderLayout.NORTH);
        contentPane.add(centralRegion, BorderLayout.CENTER);


    }

    private void addListeners() {
        welcome.addActionListener(this);
        clearPanel.addActionListener(this);
        input.addActionListener(this);
    }
    private void commandProcessor(String command) {
        //conditional to allow user to type clear
        //sets both of the entry region texts to null
        if (command.equalsIgnoreCase("clear")) {
            selectedEntry.setText(null);
            similarEntry.setText(null);
        } else {
            //else
            selectedEntry.setText(null);
            similarEntry.setText(null);
            String id = ht.getIDByName(command);
            if (id.equals("")) {
                selectedEntry.append("Sorry, desired business was not found...");
            } else {
                // user input is a valid business name
                BusinessClass b = new BusinessClass(id);
                b.setBusiness_name(command);
                String reviews = ht.get(b).reviewText;
                selectedEntry.append("Business: " + command + "\n\nReviews:\n" + reviews);
                String[] similarReviews = findSimilarBusiness(b);
                similarEntry.append("Similar Businesses: " + similarReviews[1] + "\n" + similarReviews[0]);
            }
        }
    }

    // This method serves to implement the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        commandProcessor(command);
        if (event.getSource() instanceof JTextField) {
            input.setText("");
        }
    }

    private void prepareData() throws FileNotFoundException, ParseException, IOException {
        ht = new HashTableClass();


        String fullFileName = createsFullFileName("yelp_academic_dataset_review.json");
        BufferedReader br = new BufferedReader(new FileReader(fullFileName));
        // JSON Parser
        int counter = 0;
        while (counter < 800) {
            String jsonString = br.readLine();
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonString);
            String business_id = (String) obj.get("business_id");
            String reviewText = (String) obj.get("text");
            BusinessClass b = new BusinessClass(business_id);
            FrequencyTableClass ft = new FrequencyTableClass(reviewText);
            ht.put(b, ft);
            counter++;
        }
        br.close();
        fullFileName = createsFullFileName("yelp_academic_dataset_business.json");
        br = new BufferedReader(new FileReader(fullFileName));
        int b = 0;
        String jsonString;
        while ((jsonString = br.readLine()) != null && b < ht.count) {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonString);
            String business_id = (String) obj.get("business_id");
            String name = (String) obj.get("name");
            b = ht.findBusiness(business_id, name, b);
        }
        br.close();
        // Calculate TF-IDF manually
        ht.computeTF();
        tfidfClass = new TFIDFClass(ht);
        tfidfClass.inverseDocumentFrequencyComputation();
        tfidfClass.computeTFIDF();
    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }



    private String[] findSimilarBusiness(BusinessClass b) {
        String[] similar = new String[2];
        double[] vector = ht.get(b).vector;     // vector of the selected entry
        List<Pair<Double, BusinessClass>> similarBusiness = new ArrayList<>();
        for (int i = 0; i < ht.table.length; i++) {
            for (HashTableClass.Node e = ht.table[i]; e != null; e = e.next) {
                double[] anotherVector = e.val.vector;
                if (vector != anotherVector) {
                    double dot = computeDotProduct(vector, anotherVector);
                    if (Double.isNaN(dot)) {
                    } else {
                        similarBusiness.add(new Pair(dot, e.key));
                    }
                    if (similarBusiness.size() > 2) {
                        Collections.sort(similarBusiness);
                        similarBusiness.remove(0);
                    }
                }
            }
        }
        String business1 = similarBusiness.get(0).b().business_name;
        String review1 = ht.get(similarBusiness.get(0).b()).reviewText;
        similar[0] = "\n\nBusiness: " + business1 + "\n\nReviews:\n\n" + review1 + "\n\nSimilarity: " + similarBusiness.get(0).a();
        String business2 = similarBusiness.get(1).b().business_name;
        String review2 = ht.get(similarBusiness.get(1).b()).reviewText;
        similar[1] = "\n\nBusiness: " + business2 + "\n\nReviews:\n\n" + review2 + "\n\nSimilarity: " + similarBusiness.get(1).a();
        return similar;
    }

    private double computeDotProduct(double[] a, double[] b) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0 && b[i] > 0) {
                BigDecimal aBigDeci = new BigDecimal(a[i]);
                BigDecimal bBigDeci = new BigDecimal(b[i]);
                BigDecimal d = aBigDeci.multiply(bBigDeci);
                sum = sum.add(d);
            }
        }
        return sum.doubleValue();
    }

    class Pair<A, B> implements Comparable {

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
