package com.sciencefair.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class HtmlGeneratorGui extends JFrame {
    private JButton openButton;
    private JFileChooser fileChooser;

    public HtmlGeneratorGui() {
        setTitle("Science Fair HTML Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 120);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        openButton = new JButton("Select CSV File and Generate HTML");
        openButton.addActionListener(this::onOpenFile);
        add(openButton);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    }

    private void onOpenFile(ActionEvent e) {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File csvFile = fileChooser.getSelectedFile();
            String htmlFile = csvFile.getParent() + File.separator + "output_from_csv_conversion.html";
            try {
                // Call the main HTML generation logic from ScienceFairTableAssignmentApp
                com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayoutFromCsv(csvFile.getAbsolutePath(), htmlFile);
                Desktop.getDesktop().browse(new File(htmlFile).toURI());
                System.exit(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating HTML: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HtmlGeneratorGui().setVisible(true));
    }
}
