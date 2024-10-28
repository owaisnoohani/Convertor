package com.word.convert.pdf;




import java.io.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    //  Word to PDF conversion
  @PostMapping("/convert-to-pdf")
	public ResponseEntity<byte[]> convertToPdf(@RequestParam("file") MultipartFile file) {
	    byte[] pdfContent = convertWordToPdf(file);
	    
	    // Get the original file name and change the extension to .pdf
	    String originalFileName = file.getOriginalFilename();
	    String pdfFileName = "converted.pdf"; // Default file name in case original is null

	    if (originalFileName != null) {
	        int dotIndex = originalFileName.lastIndexOf(".");
	        if (dotIndex > 0) {
	            pdfFileName = originalFileName.substring(0, dotIndex) + ".pdf"; // Replace extension with .pdf
	        } else {
	            pdfFileName = originalFileName + ".pdf"; // Append .pdf if there is no extension
	        }
	    }
	    
	   
	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFileName + "\"")
	        .contentType(MediaType.APPLICATION_PDF) // Specify the content type
	        .body(pdfContent);
	}
    
    // PDF to DOC conversion
    @PostMapping("/convert-to-doc")
    public ResponseEntity<byte[]> convertToDoc(@RequestParam("file") MultipartFile file) {
        byte[] docContent = convertPdfToWord(file);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.docx")
            .body(docContent);
    }

    // Method to convert Word to PDF


     private byte[] convertWordToPdf(MultipartFile file) {
        try {
            // Load the Word document
            InputStream fileStream = file.getInputStream();
            XWPFDocument wordDocument = new XWPFDocument(fileStream);

            // Create a PDF document
            PDDocument pdfDocument = new PDDocument();
            PDPage page = new PDPage();
            pdfDocument.addPage(page);

            // Set up content stream
            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float lineHeight = 12; // Adjust line height as needed

            for (XWPFParagraph paragraph : wordDocument.getParagraphs()) {
                // Set font size and style based on the Word paragraph
                float fontSize = 10; // Default font size; adjust if needed
                PDType1Font font = PDType1Font.HELVETICA; // Default font; adjust if needed
                
                // Retrieve font size from the Word document if specified
                if (paragraph.getRuns().size() > 0) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        if (run.getFontSize() != -1) {
                            fontSize = run.getFontSize();
                        }
                        // You can add additional logic to handle different font styles here
                    }
                }

                contentStream.setFont(font, fontSize);
                contentStream.setLeading(fontSize + 2); // Set leading to be slightly more than the font size

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);

                // Process each paragraph as a single entity to maintain sequence
                String paragraphText = paragraph.getText(); // Get the entire paragraph text
                
                // Handle potential word wrap and keep line sequences intact
                String[] lines = paragraphText.split("\n"); // Split by new line
                for (String line : lines) {
                    String[] words = line.split(" "); // Split by space for word wrapping
                    StringBuilder currentLine = new StringBuilder();

                    for (String word : words) {
                        String newLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                        float textWidth = font.getStringWidth(newLine) / 1000 * fontSize;

                        // Check if the current line width exceeds page width
                        if (textWidth > (page.getMediaBox().getWidth() - 2 * margin)) {
                            contentStream.showText(currentLine.toString());
                            contentStream.newLine();
                            yPosition -= lineHeight;

                            // Check for page overflow
                            if (yPosition < margin) {
                                contentStream.endText();
                                contentStream.close();
                                page = new PDPage();
                                pdfDocument.addPage(page);
                                contentStream = new PDPageContentStream(pdfDocument, page);
                                contentStream.setFont(font, fontSize);
                                contentStream.setLeading(fontSize + 2);
                                contentStream.beginText();
                                contentStream.newLineAtOffset(margin, page.getMediaBox().getHeight() - margin);
                                yPosition = page.getMediaBox().getHeight() - margin;
                            }
                            currentLine = new StringBuilder(word); // Start new line with the current word
                        } else {
                            currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
                        }
                    }

                    // Write any remaining text in current line
                    if (currentLine.length() > 0) {
                        contentStream.showText(currentLine.toString());
                        contentStream.newLine();
                        yPosition -= lineHeight;
                    }
                }
                contentStream.endText();
                yPosition -= lineHeight; // Add space after each paragraph
            }

            contentStream.close();

            // Convert PDF to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.save(outputStream);
            pdfDocument.close();
            wordDocument.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }



    // Method to convert PDF to DOC (placeholder example)
    private byte[] convertPdfToWord(MultipartFile file) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // Load the PDF document
            PDDocument pdfDocument = PDDocument.load(file.getInputStream());
            PDPageTree pages = pdfDocument.getPages();

            // Create a Word document
            XWPFDocument wordDocument = new XWPFDocument();

            // Iterate through each page of the PDF
            for (PDPage page : pages) {
                // Extract text from the PDF page
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String pageText = pdfStripper.getText(pdfDocument);

                // Add a new paragraph to the Word document
                XWPFParagraph paragraph = wordDocument.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(pageText);
                run.addBreak(); // Add a break after the text

                // You can add additional formatting here if needed
            }

            // Save the Word document to the output stream
            wordDocument.write(outputStream);
            wordDocument.close();
            pdfDocument.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Error during PDF to DOCX conversion: " + e.getMessage());
            e.printStackTrace();
            return new byte[0]; // Return an empty byte array in case of error
        } finally {
            try {
                outputStream.close(); // Ensure the output stream is closed
            } catch (IOException e) {
                System.err.println("Error closing output stream: " + e.getMessage());
            }
        }
    }




}
