package com.word.convert.pdf;




import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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

            // Set up content stream and initial position
            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
            PDType1Font font = PDType1Font.HELVETICA; // Store the font
            float fontSize = 10; // Store the font size
            contentStream.setFont(font, fontSize);
            contentStream.setLeading(14.5f);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);

            // Track current Y position, margin, and max width
            float yPosition = 750;
            final float margin = 50;
            final float contentWidth = page.getMediaBox().getWidth() - 2 * margin;
            final float lineHeight = 14.5f;

            for (XWPFParagraph paragraph : wordDocument.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.text().replace("\t", " ");

                    // Split text for line-by-line processing and word wrapping
                    String[] lines = text.split("\n");
                    for (String line : lines) {
                        String[] words = line.split(" ");
                        StringBuilder currentLine = new StringBuilder();

                        for (String word : words) {
                            // Calculate text width using stored font and size
                            float textWidth = font.getStringWidth(currentLine + " " + word) / 1000 * fontSize;
                            if (textWidth > contentWidth) {
                                contentStream.showText(currentLine.toString());
                                contentStream.newLine();
                                yPosition -= lineHeight;

                                // Add a new page if the content overflows
                                if (yPosition < margin) {
                                    contentStream.endText();
                                    contentStream.close();
                                    page = new PDPage();
                                    pdfDocument.addPage(page);
                                    contentStream = new PDPageContentStream(pdfDocument, page);
                                    contentStream.setFont(font, fontSize);
                                    contentStream.setLeading(lineHeight);
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(margin, 750);
                                    yPosition = 750;
                                }

                                currentLine = new StringBuilder(word);
                            } else {
                                currentLine.append(" ").append(word);
                            }
                        }

                        // Write remaining text in the current line
                        contentStream.showText(currentLine.toString());
                        contentStream.newLine();
                        yPosition -= lineHeight;
                    }

                    // Handle images in the run
                    for (XWPFPicture picture : run.getEmbeddedPictures()) {
                        PDImageXObject image = PDImageXObject.createFromByteArray(
                                pdfDocument, picture.getPictureData().getData(), picture.getPictureData().getFileName());

                        contentStream.endText(); // Temporarily end text mode
                        contentStream.drawImage(image, margin, yPosition - 100, 100, 100);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition - 120);
                        yPosition -= 120;  // Space adjustment for the image
                    }
                }
                contentStream.newLine(); // Space between paragraphs
                yPosition -= lineHeight;
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
