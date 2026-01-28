import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Attendance Tracker Application
 * Compatible with Java 8+
 * Tested with Java 21 (Temurin)
 */
public class AttendanceTracker {

    public static void main(String[] args) {
        // Run GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(AttendanceTracker::new);
    }

    public AttendanceTracker() {
        // Create main frame
        JFrame frame = new JFrame("Attendance Tracker");
        frame.setSize(420, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Main panel with padding
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels
        JLabel nameLabel = new JLabel("Attendance Name:");
        JLabel courseLabel = new JLabel("Course / Year:");
        JLabel timeLabel = new JLabel("Time In:");
        JLabel sigLabel = new JLabel("E-Signature:");

        // Text fields
        JTextField nameField = new JTextField(20);
        JTextField courseField = new JTextField(20);
        JTextField timeField = new JTextField(20);
        JTextField sigField = new JTextField(20);

        // Time and signature fields should not be editable
        timeField.setEditable(false);
        sigField.setEditable(false);

        // Buttons
        JButton timeInBtn = new JButton("Time In");
        JButton saveBtn = new JButton("Save");
        JButton clearBtn = new JButton("Clear");
        JButton printBtn = new JButton("Print");
        JButton exitBtn = new JButton("Exit");

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(courseLabel, gbc);
        gbc.gridx = 1;
        panel.add(courseField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(timeLabel, gbc);
        gbc.gridx = 1;
        panel.add(timeField, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(sigLabel, gbc);
        gbc.gridx = 1;
        panel.add(sigField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        buttonPanel.add(timeInBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(printBtn);
        buttonPanel.add(exitBtn);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        frame.add(panel);
        frame.setVisible(true);

        // Time In button logic
        timeInBtn.addActionListener((ActionEvent e) -> {
            LocalDateTime now = LocalDateTime.now();

            // Human-friendly format
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            timeField.setText(now.format(formatter));

            // Shortened UUID for e-signature
            String shortUUID = UUID.randomUUID().toString().substring(0, 8);
            sigField.setText(shortUUID);
        });

        // Save button logic
        saveBtn.addActionListener((ActionEvent e) -> {
            if (nameField.getText().isEmpty() ||
                courseField.getText().isEmpty() ||
                timeField.getText().isEmpty()) {

                JOptionPane.showMessageDialog(frame,
                        "Please complete all fields and click Time In first.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (FileWriter writer = new FileWriter("attendance_records.txt", true)) {
                writer.write("Name: " + nameField.getText() + "\n");
                writer.write("Course/Year: " + courseField.getText() + "\n");
                writer.write("Time In: " + timeField.getText() + "\n");
                writer.write("E-Signature: " + sigField.getText() + "\n");
                writer.write("----------------------------------\n");

                JOptionPane.showMessageDialog(frame,
                        "Attendance saved successfully.",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error saving file.",
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Clear button logic
        clearBtn.addActionListener(e -> {
            nameField.setText("");
            courseField.setText("");
            timeField.setText("");
            sigField.setText("");
        });

        // Print button (simulated)
        printBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "Print feature not connected to a printer.\n(This is a simulation.)",
                        "Print",
                        JOptionPane.INFORMATION_MESSAGE)
        );

        // Exit button
        exitBtn.addActionListener(e -> System.exit(0));
    }
}
