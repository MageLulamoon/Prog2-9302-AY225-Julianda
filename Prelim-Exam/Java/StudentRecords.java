// Programmer: Jurrien Julianda / Student ID: 25-0909-131
// File: StudentRecords.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.*;

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
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
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

        this.setLayout(new BorderLayout(8,8));
        this.add(scroll, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.EAST);

        // Defensive CSV loading
        try {
            loadCSVDefensive("MOCK_DATA.csv");
        } catch (Exception ex) {
            // In case anything unexpected happens, show clear message
            JOptionPane.showMessageDialog(this, "Fatal error loading CSV:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        this.setVisible(true);
    }

    // Try several likely locations for the CSV, and also the classpath
    private void loadCSVDefensive(String fileName) {
        java.util.List<String> tried = new java.util.ArrayList<>();


        // 1) current working directory
        String cwd = System.getProperty("user.dir");
        tried.add(cwd + File.separator + fileName);
        File f1 = new File(cwd, fileName);
        if (f1.exists() && f1.isFile()) {
            loadCSV(f1);
            return;
        }

        // 2) if repo layout uses Java/ subfolder (common), try Java/MOCK_DATA.csv
        File f2 = new File(cwd + File.separator + "Java", fileName);
        tried.add(f2.getAbsolutePath());
        if (f2.exists() && f2.isFile()) {
            loadCSV(f2);
            return;
        }

        // 3) try same folder as this .class (useful if running from IDE build dir or jar)
        try {
            URI codeLocation = StudentRecords.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File codeFile = new File(codeLocation);
            File codeDir = codeFile.isDirectory() ? codeFile : codeFile.getParentFile();
            if (codeDir != null) {
                File f3 = new File(codeDir, fileName);
                tried.add(f3.getAbsolutePath());
                if (f3.exists() && f3.isFile()) {
                    loadCSV(f3);
                    return;
                }
                // also try one level up (some IDEs put classes under bin/ or out/production)
                File f3b = new File(codeDir.getParentFile() != null ? codeDir.getParentFile() : codeDir, fileName);
                tried.add(f3b.getAbsolutePath());
                if (f3b.exists() && f3b.isFile()) {
                    loadCSV(f3b);
                    return;
                }
            }
        } catch (Exception ignored) {}

        // 4) try classpath resource (bundled into jar or resources)
        tried.add("classpath:/" + fileName);
        InputStream resourceStream = StudentRecords.class.getResourceAsStream("/" + fileName);
        if (resourceStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                loadCSVFromReader(reader);
                return;
            } catch (IOException ioe) {
                // fall through to error below
            }
        }

        // If we reach here, none of the locations worked. Show a helpful message listing tries.
        StringBuilder sb = new StringBuilder();
        sb.append("Could not find ").append(fileName).append(".\n\nTried these locations:\n");
        for (String t : tried) sb.append(" - ").append(t).append("\n");
        sb.append("\nSolution: Place ").append(fileName).append(" in the same folder as StudentRecords.java\n(or put it in Java/ if your repo uses that folder), then commit & push.\n");
        sb.append("\nCurrent working dir: ").append(cwd).append("\n");

        // Print to console for debugging
        System.err.println(sb.toString());
        // Show dialog in UI
        JOptionPane.showMessageDialog(this, sb.toString(), "CSV not found", JOptionPane.ERROR_MESSAGE);
    }

    private void loadCSV(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            loadCSVFromReader(reader);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Error reading CSV:\n" + ioe.getMessage(),
                    "Read Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCSVFromReader(BufferedReader reader) throws IOException {
        String first = reader.readLine();
        if (first == null) return;
        boolean hasHeader = first.toLowerCase().contains("studentid") || first.toLowerCase().contains("first_name");
        if (!hasHeader) parseAndAddLine(first);
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) parseAndAddLine(line);
        }
    }

    private void parseAndAddLine(String line) {
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim().replaceAll("^\"|\"$", "");
        if (parts.length == 0) return;
        String id = parts[0];
        String name = "";
        String grade = "";
        if (parts.length == 2) name = parts[1];
        else if (parts.length == 3) {
            if (isNumeric(parts[2])) { name = parts[1]; grade = parts[2]; }
            else name = parts[1] + " " + parts[2];
        } else if (parts.length >= 4) {
            if (!isNumeric(parts[1]) && !isNumeric(parts[2])) {
                name = parts[1] + " " + parts[2];
                grade = computeAverage(parts, 3);
            } else {
                name = parts[1];
                grade = computeAverage(parts, 2);
            }
        }
        model.addRow(new Object[]{id, name, grade});
    }

    private String computeAverage(String[] parts, int start) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < parts.length; i++) {
            String p = parts[i].replaceAll("[^0-9\\.\\-]", "");
            if (p.isEmpty()) continue;
            try {
                double v = Double.parseDouble(p);
                sum += v;
                count++;
            } catch (NumberFormatException ignored) {}
        }
        if (count == 0) return "";
        return String.valueOf(Math.round(sum / count));
    }

    private boolean isNumeric(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        try { Double.parseDouble(s); return true; } catch (Exception e) { return false; }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBtn) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String grade = gradeField.getText().trim();
            if (id.isEmpty() && name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter at least an ID or Name to add.",
                        "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            model.addRow(new Object[]{id, name, grade});
            idField.setText(""); nameField.setText(""); gradeField.setText("");
        } else if (e.getSource() == deleteBtn) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected row?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) model.removeRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentRecords::new);
    }
}
