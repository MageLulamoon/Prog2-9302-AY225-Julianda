// File: PrelimLabCalculator.java
// Place in: PrelimLabWork3/Java/
// Compile: javac PrelimLabCalculator.java
// Run:     java PrelimLabCalculator

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PrelimLabCalculator extends JFrame {

    private JFormattedTextField attendanceField;
    private JFormattedTextField lab1Field;
    private JFormattedTextField lab2Field;
    private JFormattedTextField lab3Field;

    private JButton calculateBtn;
    private JTextArea resultsArea;

    private static final Color TEAL = new Color(155, 233, 216); // #9BE9D8
    private static final Color PURPLE = new Color(105, 50, 143); // #69328F
    private static final DecimalFormat DF = new DecimalFormat("#0.00");

    public PrelimLabCalculator() {
        super("Prelim Grade Calculator");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(TEAL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 14, 10, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Prelim Exam Score Calculator", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        root.add(title, gbc);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(PURPLE, 2));
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(8, 10, 8, 10);
        c2.fill = GridBagConstraints.HORIZONTAL;

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(nf);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setMaximum(100.0);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        attendanceField = new JFormattedTextField(formatter);
        lab1Field = new JFormattedTextField(formatter);
        lab2Field = new JFormattedTextField(formatter);
        lab3Field = new JFormattedTextField(formatter);

        Dimension fieldSize = new Dimension(180, 32);
        attendanceField.setPreferredSize(fieldSize);
        lab1Field.setPreferredSize(fieldSize);
        lab2Field.setPreferredSize(fieldSize);
        lab3Field.setPreferredSize(fieldSize);

        attendanceField.setHorizontalAlignment(SwingConstants.CENTER);
        lab1Field.setHorizontalAlignment(SwingConstants.CENTER);
        lab2Field.setHorizontalAlignment(SwingConstants.CENTER);
        lab3Field.setHorizontalAlignment(SwingConstants.CENTER);

        attendanceField.setToolTipText("Enter attendance score, 0 to 100");
        lab1Field.setToolTipText("Enter Lab 1 grade, 0 to 100");
        lab2Field.setToolTipText("Enter Lab 2 grade, 0 to 100");
        lab3Field.setToolTipText("Enter Lab 3 grade, 0 to 100");

        // Row: Attendance
        c2.gridx = 0; c2.gridy = 0; c2.weightx = 0.3;
        card.add(makeLabel("Attendance (0 - 100):"), c2);
        c2.gridx = 1; c2.weightx = 0.7;
        card.add(attendanceField, c2);

        // Row: Lab 1
        c2.gridx = 0; c2.gridy++;
        card.add(makeLabel("Lab Work 1 (0 - 100):"), c2);
        c2.gridx = 1;
        card.add(lab1Field, c2);

        // Row: Lab 2
        c2.gridx = 0; c2.gridy++;
        card.add(makeLabel("Lab Work 2 (0 - 100):"), c2);
        c2.gridx = 1;
        card.add(lab2Field, c2);

        // Row: Lab 3
        c2.gridx = 0; c2.gridy++;
        card.add(makeLabel("Lab Work 3 (0 - 100):"), c2);
        c2.gridx = 1;
        card.add(lab3Field, c2);

        // Calculate button
        c2.gridx = 0; c2.gridy++; c2.gridwidth = 2;
        c2.anchor = GridBagConstraints.CENTER;
        calculateBtn = new JButton("Calculate");
        calculateBtn.setBackground(PURPLE);
        calculateBtn.setForeground(Color.WHITE);
        calculateBtn.setPreferredSize(new Dimension(160, 38));
        calculateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        calculateBtn.setFocusPainted(false);
        calculateBtn.addActionListener(ev -> onCalculateClicked());
        card.add(calculateBtn, c2);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        root.add(card, gbc);

        resultsArea = new JTextArea(12, 48);
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        resultsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PURPLE, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        resultsArea.setBackground(new Color(231, 247, 245));
        JScrollPane scroll = new JScrollPane(resultsArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        root.add(scroll, gbc);

        setContentPane(root);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(PURPLE);
        return l;
    }

    private void onCalculateClicked() {
        // ensure fields are committed and non-null
        try {
            attendanceField.commitEdit();
            lab1Field.commitEdit();
            lab2Field.commitEdit();
            lab3Field.commitEdit();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for all fields.", "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (attendanceField.getValue() == null || lab1Field.getValue() == null ||
            lab2Field.getValue() == null || lab3Field.getValue() == null) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Input error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double attendance = ((Number) attendanceField.getValue()).doubleValue();
        double lab1 = ((Number) lab1Field.getValue()).doubleValue();
        double lab2 = ((Number) lab2Field.getValue()).doubleValue();
        double lab3 = ((Number) lab3Field.getValue()).doubleValue();

        if (!inRange(attendance) || !inRange(lab1) || !inRange(lab2) || !inRange(lab3)) {
            JOptionPane.showMessageDialog(this, "Values must be between 0 and 100.", "Range error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double labAvg = (lab1 + lab2 + lab3) / 3.0;
        double classStanding = (attendance * 0.40) + (labAvg * 0.60);

        // Corrected formula: PrelimGrade = (PrelimExam * 0.30) + (ClassStanding * 0.70)
        // Solve: RequiredExam = (Target - (ClassStanding * 0.70)) / 0.30
        double requiredPass = (75.0 - (classStanding * 0.70)) / 0.30;
        double requiredExcellent = (100.0 - (classStanding * 0.70)) / 0.30;

        StringBuilder sb = new StringBuilder();
        sb.append("Computed values\n");
        sb.append("-------------------------------\n");
        sb.append(String.format("Attendance score       : %s\n", DF.format(attendance)));
        sb.append(String.format("Lab Work 1             : %s\n", DF.format(lab1)));
        sb.append(String.format("Lab Work 2             : %s\n", DF.format(lab2)));
        sb.append(String.format("Lab Work 3             : %s\n", DF.format(lab3)));
        sb.append(String.format("Lab Work Average       : %s\n", DF.format(labAvg)));
        sb.append(String.format("Class Standing (component for final, 70%%) : %s\n", DF.format(classStanding)));
        sb.append("\n");

        sb.append("Required Prelim Exam to PASS (final = 75)\n");
        sb.append("-------------------------------\n");
        if (requiredPass <= 0) {
            sb.append("Required Exam (pass)   : 0.00\n");
            sb.append("Remark                 : No exam needed to reach a passing final grade.\n");
        } else if (requiredPass > 100) {
            sb.append("Required Exam (pass)   : >100\n");
            sb.append("Remark                 : Even a perfect exam cannot reach a passing final grade.\n");
        } else {
            sb.append(String.format("Required Exam (pass)   : %s\n", DF.format(requiredPass)));
            sb.append(String.format("Remark                 : You need at least %s%% on the exam to reach 75.\n", DF.format(requiredPass)));
        }
        sb.append("\n");

        sb.append("Required Prelim Exam to ACHIEVE EXCELLENT (final = 100)\n");
        sb.append("-------------------------------\n");
        if (requiredExcellent <= 0) {
            sb.append("Required Exam (excellent): 0.00\n");
            sb.append("Remark                   : Current standing already yields excellent without exam.\n");
        } else if (requiredExcellent > 100) {
            sb.append("Required Exam (excellent): >100\n");
            sb.append("Remark                   : Even a perfect exam cannot reach excellent.\n");
        } else {
            sb.append(String.format("Required Exam (excellent): %s\n", DF.format(requiredExcellent)));
            sb.append(String.format("Remark                   : You need at least %s%% on the exam to reach 100.\n", DF.format(requiredExcellent)));
        }

        resultsArea.setText(sb.toString());
    }

    private boolean inRange(double v) {
        return v >= 0.0 && v <= 100.0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrelimLabCalculator app = new PrelimLabCalculator();
            app.setVisible(true);
        });
    }
}
