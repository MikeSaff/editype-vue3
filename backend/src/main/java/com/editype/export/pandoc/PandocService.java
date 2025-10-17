package com.editype.export.pandoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for exporting documents using Pandoc
 * Requires Pandoc to be installed on the system
 */
@Slf4j
@Service
public class PandocService {
    
    /**
     * Export HTML to DOCX using Pandoc
     */
    public byte[] exportToDocx(String html, String title) throws IOException, InterruptedException {
        // Create temporary files
        Path tempHtml = Files.createTempFile("article", ".html");
        Path tempDocx = Files.createTempFile("article", ".docx");
        
        try {
            // Write HTML to temp file
            Files.writeString(tempHtml, wrapHtml(html, title));
            
            // Execute Pandoc
            ProcessBuilder pb = new ProcessBuilder(
                "pandoc",
                tempHtml.toString(),
                "-o", tempDocx.toString(),
                "--from", "html",
                "--to", "docx",
                "--standalone"
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                String error = new String(process.getErrorStream().readAllBytes());
                log.error("Pandoc failed: {}", error);
                throw new RuntimeException("Pandoc export failed: " + error);
            }
            
            // Read result
            return Files.readAllBytes(tempDocx);
            
        } finally {
            // Cleanup
            Files.deleteIfExists(tempHtml);
            Files.deleteIfExists(tempDocx);
        }
    }
    
    /**
     * Export HTML to PDF using Pandoc + wkhtmltopdf
     */
    public byte[] exportToPdf(String html, String title) throws IOException, InterruptedException {
        Path tempHtml = Files.createTempFile("article", ".html");
        Path tempPdf = Files.createTempFile("article", ".pdf");
        
        try {
            Files.writeString(tempHtml, wrapHtml(html, title));
            
            ProcessBuilder pb = new ProcessBuilder(
                "pandoc",
                tempHtml.toString(),
                "-o", tempPdf.toString(),
                "--from", "html",
                "--to", "pdf",
                "--pdf-engine=wkhtmltopdf"
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                String error = new String(process.getErrorStream().readAllBytes());
                log.error("Pandoc PDF failed: {}", error);
                throw new RuntimeException("PDF export failed: " + error);
            }
            
            return Files.readAllBytes(tempPdf);
            
        } finally {
            Files.deleteIfExists(tempHtml);
            Files.deleteIfExists(tempPdf);
        }
    }
    
    /**
     * Check if Pandoc is installed
     */
    public boolean isPandocAvailable() {
        try {
            Process process = new ProcessBuilder("pandoc", "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.warn("Pandoc not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Wrap HTML content in complete document
     */
    private String wrapHtml(String content, String title) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
            </head>
            <body>
                %s
            </body>
            </html>
            """, title, content);
    }
}




