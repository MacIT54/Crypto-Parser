package org.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.PatternSyntaxException;

public class CurrencyParserGUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public CurrencyParserGUI() {
        setTitle("Crypto Currency Parser");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Price (USD)");
        tableModel.addColumn("Market Cap");

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
        tableModel.setRowCount(0);
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(3, 30));

        JButton refreshButton = new JButton("Get all currencies");
        JButton clearButton = new JButton("Clear table");
        JButton exportButton = new JButton("Export to Excel");
        JButton searchButton = new JButton("Search");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSearchFieldAfterSearch();
                refreshData();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSearchFieldAfterSearch();
                tableModel.setRowCount(0);
            }
        });
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportToExcel();
            }
        });
        JPanel buttonPanel = new JPanel();
        searchField = new JTextField(20);
        buttonPanel.add(searchField);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchCurrency();
            }
        });
        buttonPanel.add(searchButton);
        buttonPanel.add(separator);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(700, 100));
    }

    private void refreshData() {
        try {
            String url = "https://coinmarketcap.com";
            Document doc = Jsoup.connect(url).get();
            Elements currencyTable = doc.select("table.cmc-table");
            int nameIndex = 2;
            int priceIndex = 3;
            int marketCapIndex = 6;
            tableModel.setRowCount(0);
            Currency currency = new Currency();
            for (Element row : currencyTable.select("tr")) {
                Elements cols = row.select("td");
                if (cols.size() >= marketCapIndex + 1) {
                    currency.setName(cols.get(nameIndex).text());
                    currency.setPrice(cols.get(priceIndex).text());
                    currency.setMarketCap(cols.get(marketCapIndex).text());
                    tableModel.addRow(new Object[]{currency.getName(), currency.getPrice(), currency.getMarketCap()});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearSearchFieldAfterSearch() {
        searchField.setText("");
        searchCurrency();
    }

    private void exportToExcel() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Currency Data");

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(table.getColumnName(i));
                }

                for (int i = 0; i < table.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object value = table.getValueAt(i, j);
                        Cell cell = row.createCell(j);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                FileOutputStream fileOut = new FileOutputStream(filePath);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                JOptionPane.showMessageDialog(this, "Data exported to Excel successfully!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting data to Excel: " + e.getMessage());
        }
    }

    private void searchCurrency() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            try {
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
            } catch (PatternSyntaxException e) {
                JOptionPane.showMessageDialog(this, "Invalid search query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            table.setRowSorter(null);
        }
    }
}