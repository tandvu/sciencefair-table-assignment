package com.sciencefair.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

public class TableViewerGui extends JFrame {
    private JPanel tablePanel;
    private JButton openButton;
    private JFileChooser fileChooser;

    public TableViewerGui() {
        setTitle("Science Fair Table Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        openButton = new JButton("Open CSV File...");
        openButton.addActionListener(this::onOpenFile);
        add(openButton, BorderLayout.NORTH);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        add(scrollPane, BorderLayout.CENTER);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    }

    private void onOpenFile(ActionEvent e) {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            displayTablesFromCsv(file);
        }
    }

    private void displayTablesFromCsv(File csvFile) {
        tablePanel.removeAll();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            Map<Integer, List<String[]>> rows = new TreeMap<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5 || parts[0].equalsIgnoreCase("Row")) continue;
                int row = Integer.parseInt(parts[0].trim());
                rows.computeIfAbsent(row, k -> new ArrayList<>()).add(parts);
            }
            for (Map.Entry<Integer, List<String[]>> entry : rows.entrySet()) {
                int rowNum = entry.getKey();
                List<String[]> slots = entry.getValue();
                JPanel rowPanel = new JPanel();
                rowPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 8));
                rowPanel.add(new JLabel("Row " + rowNum));
                for (int i = 0; i < slots.size(); i += 2) {
                    JPanel tableBlock = new JPanel();
                    tableBlock.setLayout(new GridLayout(2, 1));
                    tableBlock.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                    tableBlock.setPreferredSize(new Dimension(70, 50));
                    for (int j = 0; j < 2; j++) {
                        if (i + j < slots.size()) {
                            String[] slot = slots.get(i + j);
                            String projectId = slot[2].trim();
                            String category = slot[3].trim();
                            JLabel slotLabel = new JLabel();
                            slotLabel.setOpaque(true);
                            slotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                            slotLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            slotLabel.setText(projectId.equalsIgnoreCase("EMPTY") ? "EMPTY" : "P" + projectId + " (" + category + ")");
                            slotLabel.setBackground(getCategoryColor(category));
                            tableBlock.add(slotLabel);
                        }
                    }
                    rowPanel.add(tableBlock);
                }
                tablePanel.add(rowPanel);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private Color getCategoryColor(String category) {
        if (category.contains("Anim")) return new Color(198, 246, 213);
        if (category.contains("Behv")) return new Color(190, 227, 248);
        if (category.contains("Bioc")) return new Color(251, 211, 141);
        if (category.contains("Biom")) return new Color(254, 215, 215);
        if (category.contains("Cell")) return new Color(233, 216, 253);
        if (category.contains("Chem")) return new Color(254, 215, 215);
        if (category.contains("Comp")) return new Color(198, 247, 237);
        return Color.WHITE;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TableViewerGui().setVisible(true));
    }
}
