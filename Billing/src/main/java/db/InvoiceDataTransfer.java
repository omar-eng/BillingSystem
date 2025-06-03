package db;

import java.sql.*;
import java.math.BigDecimal;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class InvoiceDataTransfer {

    public static void transferDataToCustomerInvoice() {
        String selectQuery = """
            SELECT 
                dial_a as customer_msisdn,
                service_type,
                SUM(volume) as total_volume,
                SUM(total) as total_charges,
                CURRENT_DATE as invoice_date
            FROM rated_cdrs
            GROUP BY dial_a, service_type""";

        String insertQuery = """
            INSERT INTO customer_invoice 
            (customer_msisdn, service_type, total_volume, total_charges, invoice_date)
            VALUES (?, ?, ?, ?, ?)""";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                ResultSet rs = selectStmt.executeQuery();
                int count = 0;

                while (rs.next()) {
                    insertStmt.setString(1, rs.getString("customer_msisdn"));
                    insertStmt.setString(2, rs.getString("service_type"));
                    insertStmt.setInt(3, rs.getInt("total_volume"));
                    insertStmt.setBigDecimal(4, rs.getBigDecimal("total_charges"));
                    insertStmt.setDate(5, rs.getDate("invoice_date"));

                    count += insertStmt.executeUpdate();
                }
                
                // Commit the transaction
                conn.commit();
                System.out.println("Customer invoice data transfer completed! " + count + " records inserted.");
            } catch (SQLException e) {
                // Rollback in case of error
                if (conn != null) {
                    conn.rollback();
                }
                System.err.println("Error during customer_invoice transfer: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void transferDataToInvoice() {
        String selectQuery = """
            SELECT 
                dial_a as customer_msisdn,
                SUM(total) as total_charges,
                CURRENT_DATE as invoice_date
            FROM rated_cdrs
            GROUP BY dial_a""";

        String insertQuery = """
            INSERT INTO invoice 
            (id, customer_msisdn, total_charges, invoice_date)
            VALUES (NULL, ?, ?, ?)""";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                ResultSet rs = selectStmt.executeQuery();
                int count = 0;

                while (rs.next()) {
                    insertStmt.setString(1, rs.getString("customer_msisdn"));
                    insertStmt.setBigDecimal(2, rs.getBigDecimal("total_charges"));
                    insertStmt.setDate(3, rs.getDate("invoice_date"));

                    count += insertStmt.executeUpdate();
                }
                
                // Commit the transaction
                conn.commit();
                System.out.println("Invoice data transfer completed! " + count + " records inserted.");
            } catch (SQLException e) {
                // Rollback in case of error
                if (conn != null) {
                    conn.rollback();
                }
                System.err.println("Error during invoice transfer: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void generateInvoicePDF(String customer_msisdn, BigDecimal total_charges, Date invoice_date, String pdfPath) {
        // Create PDF directory if it doesn't exist
        File pdfDir = new File("Invoices");
        if (!pdfDir.exists()) {
            boolean created = pdfDir.mkdir();
            if (!created) {
                System.err.println("Failed to create Invoices directory");
                return;
            }
        }

        String selectServicesQuery = """
            SELECT 
                service_type,
                SUM(volume) as total_volume,
                SUM(total) as total_charges
            FROM rated_cdrs
            WHERE dial_a = ?
            GROUP BY service_type""";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectServicesQuery)) {
                selectStmt.setString(1, customer_msisdn);
                ResultSet rs = selectStmt.executeQuery();
                
                try {
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
                    document.open();
                    
                    // Add title
                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                    Paragraph title = new Paragraph("INVOICE", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    document.add(title);
                    
                    // Add customer details
                    Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                    Paragraph customerInfo = new Paragraph("Customer MSISDN: " + customer_msisdn, normalFont);
                    customerInfo.setAlignment(Element.ALIGN_LEFT);
                    customerInfo.setSpacingAfter(10);
                    document.add(customerInfo);
                    
                    // Add invoice date
                    Paragraph invoiceDate = new Paragraph("Invoice Date: " + invoice_date, normalFont);
                    invoiceDate.setAlignment(Element.ALIGN_LEFT);
                    invoiceDate.setSpacingAfter(20);
                    document.add(invoiceDate);
                    
                    // Create a table with 3 columns
                    PdfPTable table = new PdfPTable(3);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10);
                    table.setSpacingAfter(10);
                    
                    // Add table headers
                    Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                    table.addCell(new PdfPCell(new Phrase("Service Type", tableHeaderFont)));
                    table.addCell(new PdfPCell(new Phrase("Total Volume", tableHeaderFont)));
                    table.addCell(new PdfPCell(new Phrase("Total Charges", tableHeaderFont)));
                    
                    // Style the header cells
                    for (int i = 0; i < 3; i++) {
                        table.getRow(0).getCells()[i].setBackgroundColor(BaseColor.LIGHT_GRAY);
                        table.getRow(0).getCells()[i].setHorizontalAlignment(Element.ALIGN_CENTER);
                    }
                    
                    // Populate table rows
                    while (rs.next()) {
                        table.addCell(rs.getString("service_type"));
                        table.addCell(String.valueOf(rs.getInt("total_volume")));
                        table.addCell(rs.getBigDecimal("total_charges").toString());
                    }
                    
                    document.add(table);
                    
                    // Add total charges
                    Paragraph totalCharges = new Paragraph("Total Charges: " + total_charges, normalFont);
                    totalCharges.setAlignment(Element.ALIGN_RIGHT);
                    totalCharges.setSpacingBefore(20);
                    document.add(totalCharges);
                    
                    document.close();
                    System.out.println("Invoice PDF generated successfully at: " + pdfPath);
                    
                } catch (Exception e) {
                    System.err.println("Error generating invoice PDF: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}