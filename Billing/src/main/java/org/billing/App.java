package org.billing;
import db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Application for transferring billing data to invoice tables
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println("Starting billing data transfer...");
        
        try {
            // Test database connection
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            conn.close();

            // Transfer data to customer_invoice table
            InvoiceDataTransfer.transferDataToCustomerInvoice();
            
            // Transfer data to invoice table
            InvoiceDataTransfer.transferDataToInvoice();
            
            // Generate PDFs for all customers
            try {
                conn = DatabaseConnection.getConnection();
                String query = """
                    
                    SELECT DISTINCT i.customer_msisdn, i.total_charges, i.invoice_date
                    FROM invoice i
                    JOIN customer_invoice ci ON i.customer_msisdn = ci.customer_msisdn
                    WHERE i.invoice_date = CURRENT_DATE""";
                
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String customer_msisdn = rs.getString("customer_msisdn");
                        BigDecimal total_charges = rs.getBigDecimal("total_charges");
                        Date invoice_date = rs.getDate("invoice_date");
                        String pdfPath = "Invoices/invoice_" + customer_msisdn + ".pdf";
                        
                        System.out.println("Generating PDF for customer: " + customer_msisdn);
                        InvoiceDataTransfer.generateInvoicePDF(customer_msisdn, total_charges, invoice_date, pdfPath);
                    }
                }
                conn.close();
                System.out.println("All PDFs generated successfully!");
            } catch (Exception e) {
                System.err.println("Error during PDF generation: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("All data transfers completed successfully!");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Application completed.");
    }
}
