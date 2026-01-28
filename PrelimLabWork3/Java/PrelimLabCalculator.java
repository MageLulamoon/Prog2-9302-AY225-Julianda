/* PrelimLabCalculator.java
 *
 * Single-file Java Swing application (resizable, user-friendly layout)
 * - raw attendance counts with late-enrollee handling
 * - automatic-fail (>=4 absences without excuse)
 * - attendance percentage computed from counts
 * - lab averages and class standing
 * - solves for required exam to reach Passing (75) and Excellent (100)
 *
 * UI features:
 * - left pane: inputs (spinners, formatted fields)
 * - right pane: large results area with scroll and copy-friendly text
 * - Load Example dropdown with two test cases
 * - Reset, Calculate buttons
 * - Uses color combo: #9BE9D8 and #69328F
 *
 * Compile: javac PrelimLabCalculator.java
 * Run:     java PrelimLabCalculator
 */

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PrelimLabCalculator extends JFrame {

    private static final Color TEAL = new Color(0x9B, 0xE9, 0xD8);   // #9BE9D8
    private static final Color PURPLE = new Color(0x69, 0x32, 0x8F); // #69328F

    // Inputs
    private JSpinner totalWeeksSpinner;
    private JCheckBox lateEnrolleeCheckbox;
    private JSpinner missedBeforeSpinner;
    private JSpinner absencesSpinner;
    private JCheckBox hasExcuseCheckbox;

    private JFormattedTextField lab1Field;
    private JFormattedTextField lab2Field;
    private JFormattedTextField lab3Field;

    private JComboBox<String> exampleBox;

    // Controls
    private JButton calculateBtn;
    private JButton resetBtn;
    private JButton exportBtn;

    // Results
    private JTextArea resultsArea;
    private final DecimalFormat df = new DecimalFormat("#0.00");

    public PrelimLabCalculator() {
        super("Prelim Lab Grade Calculator");
        initLookAndFeel();
        initComponents();
        layoutComponents();
        attachListeners();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
    }

    private void initLookAndFeel() {
        // optional small polish: platform native look
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
    }

    private void initComponents() {
        // Spinners for weeks and absences
        totalWeeksSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 52, 1));
        missedBeforeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 52, 1));
        absencesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 52, 1));
        lateEnrolleeCheckbox = new JCheckBox("Late enrollee?");
        hasExcuseCheckbox = new JCheckBox("Has valid excuse?");

        // Lab grade formatted fields (0-100)
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        NumberFormatter percentFormatter = new NumberFormatter(nf);
        percentFormatter.setValueClass(Double.class);
        percentFormatter.setMinimum(0.0);
        percentFormatter.setMaximum(100.0);
        percentFormatter.setAllowsInvalid(false);
        percentFormatter.setCommitsOnValidEdit(true);

        lab1Field = new JFormattedTextField(percentFormatter);
        lab2Field = new JFormattedTextField(percentFormatter);
        lab3Field = new JFormattedTextField(percentFormatter);
        lab1Field.setColumns(8);
        lab2Field.setColumns(8);
        lab3Field.setColumns(8);

        // Buttons and controls
        calculateBtn = new JButton("Calculate");
        calculateBtn.setBackground(TEAL);
        calculateBtn.setForeground(PURPLE);
        calculateBtn.setFocusPainted(false);
        calculateBtn.setPreferredSize(new Dimension(140, 40));
        calculateBtn.setOpaque(true);
        calculateBtn.setBorder(BorderFactory.createLineBorder(PURPLE, 2));
        calculateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        resetBtn = new JButton("Reset");
        resetBtn.setPreferredSize(new Dimension(110, 36));

        exportBtn = new JButton("Export TXT");
        exportBtn.setPreferredSize(new Dimension(110, 36));

        // Example loader
        exampleBox = new JComboBox<>(new String[] {
                "Load example...","Example 1 — Typical student","Example 2 — Late enrollee"
        });

        // Results area (large, monospaced)
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        resultsArea.setLineWrap(false); // keep preformatted lines intact
    }

    private void layoutComponents() {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBackground(TEAL);
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // Title
        JLabel title = new JLabel("Prelim Exam Score Calculator", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(PURPLE);
        root.add(title, BorderLayout.NORTH);

        // Left: inputs panel
        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setBackground(TEAL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // Attendance card
        JPanel attendanceCard = new JPanel(new GridBagLayout());
        attendanceCard.setBackground(Color.WHITE);
        attendanceCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PURPLE,2), "Attendance (raw counts)"));

        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(6,8,6,8);
        a.gridy = 0; a.gridx = 0; a.anchor = GridBagConstraints.WEST;
        attendanceCard.add(new JLabel("Total Prelim Weeks (no exam week):"), a);
        a.gridx = 1; attendanceCard.add(totalWeeksSpinner, a);

        a.gridy++; a.gridx = 0;
        attendanceCard.add(lateEnrolleeCheckbox, a);
        a.gridx = 1; attendanceCard.add(missedBeforeSpinner, a);
        missedBeforeSpinner.setToolTipText("Only used if late enrollee is checked");

        a.gridy++; a.gridx = 0;
        attendanceCard.add(new JLabel("Total Absences:"), a);
        a.gridx = 1; attendanceCard.add(absencesSpinner, a);

        a.gridy++; a.gridx = 0;
        attendanceCard.add(new JLabel("Valid Excuse?"), a);
        a.gridx = 1; attendanceCard.add(hasExcuseCheckbox, a);

        // Labs card
        JPanel labsCard = new JPanel(new GridBagLayout());
        labsCard.setBackground(Color.WHITE);
        labsCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PURPLE,2), "Lab Work Grades (0 - 100)"));

        GridBagConstraints l = new GridBagConstraints();
        l.insets = new Insets(6,8,6,8);
        l.anchor = GridBagConstraints.WEST;
        l.gridy = 0; l.gridx = 0; labsCard.add(new JLabel("Lab Work 1:"), l); l.gridx = 1; labsCard.add(lab1Field, l);
        l.gridy++; l.gridx = 0; labsCard.add(new JLabel("Lab Work 2:"), l); l.gridx = 1; labsCard.add(lab2Field, l);
        l.gridy++; l.gridx = 0; labsCard.add(new JLabel("Lab Work 3:"), l); l.gridx = 1; labsCard.add(lab3Field, l);

        // Controls panel (example loader + buttons)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setBackground(TEAL);
        controls.add(exampleBox);
        controls.add(calculateBtn);
        controls.add(resetBtn);
        controls.add(exportBtn);

        // Put attendance and labs stacked
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0; inputs.add(attendanceCard, c);
        c.gridy = 1; inputs.add(labsCard, c);
        c.gridy = 2; inputs.add(controls, c);

        // Right: results area in a titled scroll pane
        JPanel resultsPanel = new JPanel(new BorderLayout(8,8));
        resultsPanel.setBackground(TEAL);
        JScrollPane scroll = new JScrollPane(resultsArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PURPLE,2), "Results (copy-paste friendly)"));

        // Info footer
        JLabel footer = new JLabel("Formula: PrelimGrade = (PrelimExam × 0.30) + (ClassStanding × 0.70)   "
                + "ClassStanding = (Attendance% × 0.40) + (LabAvg × 0.60)");
        footer.setForeground(Color.DARK_GRAY);

        resultsPanel.add(scroll, BorderLayout.CENTER);
        resultsPanel.add(footer, BorderLayout.SOUTH);

        // Use JSplitPane so user can resize result vs inputs
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputs, resultsPanel);
        split.setResizeWeight(0.42);
        split.setOneTouchExpandable(true);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void attachListeners() {
        // enable/disable missedBeforeSpinner
        lateEnrolleeCheckbox.addItemListener(e -> {
            boolean selected = lateEnrolleeCheckbox.isSelected();
            missedBeforeSpinner.setEnabled(selected);
            if (!selected) missedBeforeSpinner.setValue(0);
        });

        // example loader
        exampleBox.addActionListener(e -> {
            int idx = exampleBox.getSelectedIndex();
            if (idx == 1) { // Example 1 — Typical student
                totalWeeksSpinner.setValue(10);
                lateEnrolleeCheckbox.setSelected(false);
                missedBeforeSpinner.setValue(0);
                absencesSpinner.setValue(2);
                hasExcuseCheckbox.setSelected(false);
                lab1Field.setValue(80.0);
                lab2Field.setValue(75.0);
                lab3Field.setValue(90.0);
            } else if (idx == 2) { // Example 2 — Late enrollee
                totalWeeksSpinner.setValue(12);
                lateEnrolleeCheckbox.setSelected(true);
                missedBeforeSpinner.setValue(3);
                absencesSpinner.setValue(1);
                hasExcuseCheckbox.setSelected(true);
                lab1Field.setValue(88.0);
                lab2Field.setValue(92.0);
                lab3Field.setValue(90.0);
            }
        });

        calculateBtn.addActionListener(e -> onCalculate());
        resetBtn.addActionListener(e -> resetForm());
        exportBtn.addActionListener(e -> exportResults());
    }

    private void resetForm() {
        totalWeeksSpinner.setValue(10);
        lateEnrolleeCheckbox.setSelected(false);
        missedBeforeSpinner.setValue(0);
        missedBeforeSpinner.setEnabled(false);
        absencesSpinner.setValue(0);
        hasExcuseCheckbox.setSelected(false);
        lab1Field.setValue(null);
        lab2Field.setValue(null);
        lab3Field.setValue(null);
        resultsArea.setText("");
        exampleBox.setSelectedIndex(0);
    }

    private void onCalculate() {
        // Read inputs with validation
        int totalWeeks = (Integer) totalWeeksSpinner.getValue();
        boolean late = lateEnrolleeCheckbox.isSelected();
        int missedBefore = (Integer) missedBeforeSpinner.getValue();
        int absences = (Integer) absencesSpinner.getValue();
        boolean hasExcuse = hasExcuseCheckbox.isSelected();

        Double lab1 = getDoubleFromField(lab1Field);
        Double lab2 = getDoubleFromField(lab2Field);
        Double lab3 = getDoubleFromField(lab3Field);

        if (lab1 == null || lab2 == null || lab3 == null) {
            JOptionPane.showMessageDialog(this, "Please enter valid lab grades (0 - 100).",
                    "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (late && missedBefore >= totalWeeks) {
            JOptionPane.showMessageDialog(this, "Weeks missed before enrollment must be less than total weeks.",
                    "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int countedWeeks = late ? (totalWeeks - missedBefore) : totalWeeks;
        if (countedWeeks <= 0) {
            JOptionPane.showMessageDialog(this, "Counted weeks must be at least 1.", "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (absences < 0 || absences > countedWeeks) {
            JOptionPane.showMessageDialog(this, "Absences must be between 0 and the counted weeks.", "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Automatic fail rule
        if (absences >= 4 && !hasExcuse) {
            StringBuilder failMsg = new StringBuilder();
            failMsg.append("AUTOMATIC FAIL\n");
            failMsg.append("-------------------------------\n");
            failMsg.append("Reason: 4 or more absences without a valid excuse.\n");
            failMsg.append(String.format("Total prelim weeks counted: %d%n", countedWeeks));
            failMsg.append(String.format("Absences reported         : %d%n", absences));
            resultsArea.setText(failMsg.toString());
            return;
        }

        // compute attendance percentage
        int attendedWeeks = countedWeeks - absences;
        double attendancePct = (attendedWeeks / (double) countedWeeks) * 100.0;

        // lab average and class standing
        double labAvg = (lab1 + lab2 + lab3) / 3.0;
        double classStanding = (attendancePct * 0.40) + (labAvg * 0.60);

        // Required exams using solved formula:
        // PrelimGrade = (Exam * 0.30) + (ClassStanding * 0.70)
        // RequiredExam = (Target - (ClassStanding * 0.70)) / 0.30
        double requiredPass = (75.0 - (classStanding * 0.70)) / 0.30;
        double requiredExcellent = (100.0 - (classStanding * 0.70)) / 0.30;

        // Build output (copy-friendly)
        StringBuilder out = new StringBuilder();
        out.append("Computed values\n");
        out.append("-------------------------------\n");
        out.append(String.format("Total prelim weeks counted : %d%n", countedWeeks));
        if (late) out.append(String.format("Late enrollee, weeks missed before enrollment: %d%n", missedBefore));
        out.append(String.format("Attended weeks             : %d%n", attendedWeeks));
        out.append(String.format("Attendance percentage      : %s%%%n", df.format(attendancePct)));
        out.append(String.format("Lab Work 1                 : %s%n", df.format(lab1)));
        out.append(String.format("Lab Work 2                 : %s%n", df.format(lab2)));
        out.append(String.format("Lab Work 3                 : %s%n", df.format(lab3)));
        out.append(String.format("Lab Work Average           : %s%n", df.format(labAvg)));
        out.append(String.format("Class Standing (component for final, 70%%) : %s%n", df.format(classStanding)));
        out.append("\n");

        out.append("Required Prelim Exam to PASS (final = 75)\n");
        out.append("-------------------------------\n");
        if (requiredPass <= 0) {
            out.append("Required Exam (pass)   : 0.00\n");
            out.append("Remark                 : No exam needed to reach a passing final grade.\n");
        } else if (requiredPass > 100) {
            out.append("Required Exam (pass)   : >100\n");
            out.append("Remark                 : Even a perfect exam cannot reach a passing final grade.\n");
        } else {
            out.append(String.format("Required Exam (pass)   : %s%n", df.format(requiredPass)));
            out.append(String.format("Remark                 : You need at least %s%% on the exam to reach 75.%n", df.format(requiredPass)));
        }
        out.append("\n");

        out.append("Required Prelim Exam to ACHIEVE EXCELLENT (final = 100)\n");
        out.append("-------------------------------\n");
        if (requiredExcellent <= 0) {
            out.append("Required Exam (excellent): 0.00\n");
            out.append("Remark                   : Current standing already yields excellent without exam.\n");
        } else if (requiredExcellent > 100) {
            out.append("Required Exam (excellent): >100\n");
            out.append("Remark                   : Even a perfect exam cannot reach excellent.\n");
        } else {
            out.append(String.format("Required Exam (excellent): %s%n", df.format(requiredExcellent)));
            out.append(String.format("Remark                   : You need at least %s%% on the exam to reach 100.%n", df.format(requiredExcellent)));
        }

        resultsArea.setText(out.toString());
    }

    private Double getDoubleFromField(JFormattedTextField f) {
        Object v = f.getValue();
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void exportResults() {
        String text = resultsArea.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to export. Run a calculation first.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save results as text file");
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write(text);
                JOptionPane.showMessageDialog(this, "Results exported.", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrelimLabCalculator app = new PrelimLabCalculator();
            app.setVisible(true);
        });
    }
}
