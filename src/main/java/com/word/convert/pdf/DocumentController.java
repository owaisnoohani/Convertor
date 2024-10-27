package com.word.convert.pdf;




import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    //  Word to PDF conversion
    @PostMapping("/convert-to-pdf")
    public ResponseEntity<byte[]> convertToPdf(@RequestParam("file") MultipartFile file) {
        byte[] pdfContent = convertWordToPdf(file);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.pdf")
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

            // Initialize content stream for the page
            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.setLeading(14.5f);
            contentStream.beginText();
            contentStream.newLineAtOffset(25, 750);

            // Loop through each paragraph in the Word document
            for (XWPFParagraph paragraph : wordDocument.getParagraphs()) {
                String paragraphText = paragraph.getText().replace("\t", " ");  // Replace tabs with spaces

                // Split the paragraph text by line breaks
                String[] lines = paragraphText.split("\\r?\\n");
                for (String line : lines) {
                    contentStream.showText(line); // Add text to the PDF
                    contentStream.newLine();      // Move to the next line
                }
                contentStream.newLine(); // Add extra space between paragraphs
            }

            contentStream.endText();
            contentStream.close();

            // Convert PDF to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.save(outputStream);
            pdfDocument.close();
            wordDocument.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0]; // Return an empty byte array in case of error
        }
    }

    // Method to convert PDF to DOC (placeholder example)
    private byte[] convertPdfToWord(MultipartFile file) {
        try {
            // Load the PDF document
            InputStream inputStream = file.getInputStream();
            Document pdfDocument = new Document(inputStream);

            // Convert PDF to DOCX
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.save(outputStream, SaveFormat.DocX);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0]; // Return an empty byte array in case of error
        }
    }
}
