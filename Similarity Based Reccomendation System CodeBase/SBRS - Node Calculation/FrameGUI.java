package Assignment3;
/*
 * Frame for the GUI for Assignment 1
 */

// imports for my Graphical User Interface, dot product calculation,
// and JSON file parser
//Graphical User Interface extended from Assignment 2

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


public class FrameGUI extends JFrame implements ActionListener {

    JButton welcome;
    JButton clearPanel;
    JTextArea selectedEntry;
    JTextArea similarEntry;
    JTextField input;

    BTree tree;
    TFIDFClass tfidf;
    ClusterManagement ch;
    Graph graph;

    static private Color BabyBlue = new Color(51,204,255);

    public FrameGUI(String title) {
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
            Logger.getLogger(FrameGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FrameGUI.class.getName()).log(Level.SEVERE, null, ex);
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
    private void processCommand(String command) {
        if (command.equalsIgnoreCase("clear")) {
            selectedEntry.setText(null);
            similarEntry.setText(null);
        } else {
            selectedEntry.setText(null);
            similarEntry.setText(null);
            String id = tree.idByName(command);
            if (id.equals("")) {
                selectedEntry.append("Business not found. ");
            } else {     // user input is a valid business name
                String reviews = graph.graph.get(graph.search(id)).frequencyTable.reviewText;
                selectedEntry.append("Business: " + command + "\n\nReviews:\n\n" + reviews);
                ArrayList<String> output = graph.generateOutput(id);
                for (int i = 0; i < output.size(); i++) {
                    similarEntry.append(output.get(i) + "\n");
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        processCommand(command);
        if (event.getSource() instanceof JTextField) {
            input.setText("");
        }
    }

    private void prepareData() throws FileNotFoundException, ParseException, IOException {
        tree = new BTree(32, createsFullFileName("tree.txt"), createsFullFileName("ft.txt"), createsFullFileName("vectors.txt"), createsFullFileName("clusters.txt"), createsFullFileName("clusterVectors.txt"), createsFullFileName("graph.txt"));
        System.out.println("Tree processed from file");
        // Calculate TF-IDF
        tree.computeTF();
        System.out.println("Frequency Table Ready");
        TFIDFClass tfidf = new TFIDFClass(tree);
        tfidf.computeIDF();
        tfidf.computeTFIDF();
        System.out.println("TD-IDF Vectors Ready");
        // clustering
        ch = new ClusterManagement(tree);
        ch.generateClusters();
        System.out.println("Clusters Ready");
        // graph
        graph = new Graph(ch);
    }

    private static String createsFullFileName(String filename) {
        String separator = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        String path = home + separator;
        String fullFileName = path + filename;
        return fullFileName;
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
