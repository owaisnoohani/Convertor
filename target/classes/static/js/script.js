// Word to PDF conversion
document.getElementById("wordToPdfForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const wordInput = document.getElementById("wordInput");
    const formData = new FormData();
    formData.append("file", wordInput.files[0]);

    fetch('/api/document/convert-to-pdf', {
        method: 'POST',
        body: formData
    })
    .then(response => response.blob())
    .then(blob => {
        const downloadLink = document.getElementById("pdfDownloadLink");
        const pdfResult = document.getElementById("pdfResult");

        const url = window.URL.createObjectURL(blob);
        downloadLink.href = url;
        downloadLink.download = 'converted.pdf';

        pdfResult.style.display = "block";
        downloadLink.style.display = "inline";
    })
    .catch(error => {
        console.error('Error:', error);
    });
});

// PDF to Word conversion
document.getElementById("pdfToDocForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const pdfInput = document.getElementById("pdfInput");
    const formData = new FormData();
    formData.append("file", pdfInput.files[0]);

    fetch('/api/document/convert-to-doc', {
        method: 'POST',
        body: formData
    })
    .then(response => response.blob())
    .then(blob => {
        const downloadLink = document.getElementById("docDownloadLink");
        const docResult = document.getElementById("docResult");

        const url = window.URL.createObjectURL(blob);
        downloadLink.href = url;
        downloadLink.download = 'converted.docx';

        docResult.style.display = "block";
        downloadLink.style.display = "inline";
    })
    .catch(error => {
        console.error('Error:', error);
    });
});
