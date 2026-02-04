// Programmer: Jurrien Julianda / Student ID: 25-0909-131
// File: StudentRecords.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/*
  Student Record System (Java Swing)
  - Reads MOCK_DATA.csv on startup
  - Tries to intelligently parse each CSV line:
    * ID is first column
    * Name is either (first_name + last_name) or the second column
    * Grade is either a single numeric column or the rounded average of numeric columns after the name
  - UI: JTable, ID/Name/Grade text fields, Add and Delete buttons
*/

public class StudentRecords extends JFrame implements ActionListener {
    private DefaultTableModel model;
    private JTable table;
    private JTextField idField, nameField, gradeField;
    private JButton addBtn, deleteBtn;

    public StudentRecords() {
        this.setTitle("Records - Jurrien Julianda 25-0909-131");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(680, 420);
        this.setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Grade"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // make table non-editable directly
            }
        };
        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(10);
        nameField = new JTextField(12);
        gradeField = new JTextField(5);

        addBtn = new JButton("Add");
        deleteBtn = new JButton("Delete");

        addBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        c.gridx = 0; c.gridy = 0; inputPanel.add(new JLabel("ID"), c);
        c.gridx = 1; c.gridy = 0; inputPanel.add(idField, c);

        c.gridx = 0; c.gridy = 1; inputPanel.add(new JLabel("Name"), c);
        c.gridx = 1; c.gridy = 1; inputPanel.add(nameField, c);

        c.gridx = 0; c.gridy = 2; inputPanel.add(new JLabel("Grade"), c);
        c.gridx = 1; c.gridy = 2; inputPanel.add(gradeField, c);

        c.gridx = 0; c.gridy = 3; inputPanel.add(addBtn, c);
        c.gridx = 1; c.gridy = 3; inputPanel.add(deleteBtn, c);

        this.setLayout(new BorderLayout(8, 8));
        this.add(scroll, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.EAST);

        // Load CSV on startup
        loadCSV("MOCK_DATA.csv");

        this.setVisible(true);
    }

    private void loadCSV(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            // Check header line
            line = reader.readLine();
            if (line == null) return;
            // If header contains "StudentID" assume first line is header; otherwise treat it as data
            boolean hasHeader = line.toLowerCase().contains("studentid") || line.toLowerCase().contains("first_name");
            if (!hasHeader) {
                // parse the first line as data
                parseAndAddLine(line);
            }
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                parseAndAddLine(line);
            }
        } catch (FileNotFoundException fnf) {
            JOptionPane.showMessageDialog(this, "CSV file not found: " + filePath,
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Error reading CSV: " + ioe.getMessage(),
                    "Read Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected parse error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        }
    }

    private void parseAndAddLine(String line) {
        // Handle quoted fields simply by removing surrounding quotes
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim().replaceAll("^\"|\"$", "");

        if (parts.length == 0) return;
        String id = parts[0];
        String name = "";
        String grade = "";

        // Heuristic parsing:
        if (parts.length == 1) {
            name = "";
            grade = "";
        } else if (parts.length == 2) {
            name = parts[1];
            grade = "";
        } else if (parts.length == 3) {
            // Could be ID, Name, Grade OR ID, Firstname, Lastname
            if (isNumeric(parts[2])) {
                name = parts[1];
                grade = parts[2];
            } else {
                name = parts[1] + " " + parts[2];
                grade = "";
            }
        } else {
            // parts.length >= 4
            // If parts[1] and parts[2] look like names (letters), combine them
            if (!isNumeric(parts[1]) && !isNumeric(parts[2])) {
                name = parts[1] + " " + parts[2];
                // numeric columns likely start at index 3
                grade = computeAverage(parts, 3);
            } else {
                // fallback: name = parts[1], numeric from index 2
                name = parts[1];
                grade = computeAverage(parts, 2);
            }
        }
        // If grade still blank, leave it empty
        model.addRow(new Object[]{id, name, grade});
    }

    private String computeAverage(String[] parts, int startIndex) {
        double sum = 0;
        int count = 0;
        for (int i = startIndex; i < parts.length; i++) {
            String p = parts[i].replaceAll("[^0-9\\.\\-]", "");
            if (p.isEmpty()) continue;
            try {
                double val = Double.parseDouble(p);
                sum += val;
                count++;
            } catch (NumberFormatException nfe) {
                // skip non-numeric
            }
        }
        if (count == 0) return "";
        long avg = Math.round(sum / count);
        return String.valueOf(avg);
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == addBtn) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String grade = gradeField.getText().trim();
            if (id.isEmpty() && name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter at least an ID or Name to add.",
                        "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            model.addRow(new Object[]{id, name, grade});
            idField.setText("");
            nameField.setText("");
            gradeField.setText("");
        } else if (src == deleteBtn) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected row?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(row);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRecords());
    }
}
