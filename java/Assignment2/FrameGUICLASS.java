package Assignment2;


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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;

import org.json.simple.parser.ParseException;


public class FrameGUICLASS extends JFrame implements ActionListener {

    JButton welcome;
    JButton clearPanel;
    JTextArea selectedEntry;
    JTextArea similarEntry;
    JTextField input;

    BTREEClass tree;
    TFIDFCLASS tfidf;
    ClusterCLASS ch;

    static private Color BabyBlue = new Color(51,204,255);

    public FrameGUICLASS(String title) {
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
            Logger.getLogger(FrameGUICLASS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FrameGUICLASS.class.getName()).log(Level.SEVERE, null, ex);
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
            String id = tree.idByName(command);
            if (id.equals("")) {
                selectedEntry.append("Sorry, your desired business was not found...");
            } else {
                // user input is a valid business name
                String reviews = tree.search(id).reviewText;
                selectedEntry.append("Business Name:" + command + "\n\n Business Reviews:\n\n" + reviews);
                String similarReviews = locateSimilarBusiness(id);
                similarEntry.append("Similar Businesses:" + similarReviews);

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
        tree = new BTREEClass(32, createsFullFileName("tree.txt"), createsFullFileName("ft.txt"), createsFullFileName("vectors.txt"), createsFullFileName("clusters.txt"), createsFullFileName("clusterVectors.txt"));
        System.out.println("The Tree has been successfully processed from file");
        //Calculate TF-IDF
        tree.computeTF();
        System.out.println("Frequency Table is now Ready");
        TFIDFCLASS tfidf = new TFIDFCLASS(tree);
        tfidf.computeIDF();
        tfidf.computeTFIDF();
        System.out.println("The TD-IDF Vectors are now Ready");
        // Clustering
        ch = new ClusterCLASS(tree);
        ch.clusterGeneration();
        System.out.println("The Clusters are now Ready");

    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
    }



    private String locateSimilarBusiness(String id) {
        ArrayList<BNODEClass.Node> entries = ch.retrieveClusterByID(id);
        BNODEClass.Node self = null;
        for(int i = 0; i < entries.size(); i++){
            if(entries.get(i).key.business_id.equals(id)){
                self = entries.get(i);
                break;
            }
        }
        entries.remove(self);
        String similar = sortClusters(self, entries);
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

    private String sortClusters(BNODEClass.Node self, ArrayList<BNODEClass.Node> entries){
        TreeMap<Double, String> map = new TreeMap<>(Collections.reverseOrder());
        double[] v1 = self.val.vector;
        for(BNODEClass.Node n : entries){
            double [] v2 = n.val.vector;
            double dp = computeDotProduct(v1,v2);
            String s = "\n\nBusiness Name: " + n.key.getName() + "\n\nBusiness Reviews:\n\n" + n.val.reviewText + "\n\nBusiness Similarity:" + dp + "\n";
            map.put(dp, s);
        }
        int counter = 0;
        String sorted = "";

        for (String str : map.values()){
            sorted = sorted + str;
            counter++;
            if(counter >= 10){
                break;
            }
        }
        return sorted;
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
