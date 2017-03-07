package com.leapest.pdfutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public class PdfStamper {

	private ByteArrayOutputStream output;
	private PdfWriter writer;
	private PdfDocument doc;
	
	private PdfAcroForm acroForm;

	public PdfStamper(InputStream source) throws IOException {
		output = new ByteArrayOutputStream();
		writer = new PdfWriter(output);
		doc = new PdfDocument(new PdfReader(source), writer);
		acroForm = PdfAcroForm.getAcroForm(doc, true);
	}

	/**
	 * Sets the given image as content of fields with key {@code fieldKey}.
	 * The image is scaled to fit into the field.
	 * @param fieldKey the field key
	 * @param imageStream the input stream of te image
	 * @throws IOException
	 */
	public PdfStamper setImageField(String fieldKey, InputStream imageStream) throws IOException {
		PdfFormField pdfField = acroForm.getFormFields().get(fieldKey);
		if (pdfField == null) {
			return this;
		}
		
		PdfArray position = pdfField.getWidgets().get(0).getRectangle();
		float x1 = (float) position.getAsNumber(0).getValue();
		float y1 = (float) position.getAsNumber(1).getValue();
		float x2 = (float) position.getAsNumber(2).getValue();
		float y2 = (float) position.getAsNumber(3).getValue();
		
		float width = x2 - x1;
		float height = y2 - y1;

		byte[] imageBytes = IOUtils.toByteArray(imageStream);
		Image image = new Image(ImageDataFactory.create(imageBytes));
		image.scaleToFit(width, height);
		image.setFixedPosition(x1, y1);

		Document d = new Document(doc);
		d.add(image);
		return this;
	}

	/**
	 * Sets the given text value to fields with key {@code fieldKey}.
	 * @param fieldKey the field key
	 * @param text the text value
	 */
	public PdfStamper setTextField(String fieldKey, String text) {
		PdfFormField pdfField = acroForm.getFormFields().get(fieldKey);
		if (pdfField != null) {
			pdfField.setValue(text);
		}
		
		return this;
	}

	/**
	 * Saves all modifications and flattens the PDF.
	 * @return the stamped PDF holding an input stream to be read from.
	 */
	public StampedPdf stamp() {
		try {
			acroForm.flattenFields();
			doc.close(); // Close preemptively to trigger write
			
			return new StampedPdf(new ByteArrayInputStream(output.toByteArray()), output.size());
		} finally {
			doc.close();
		}
	}
	
	public void dispose() {
		doc.close();
	}
	
	public static class StampedPdf {

		private InputStream stampedPdfStream;
		private long size;

		public StampedPdf(InputStream stream, long size) {
			this.stampedPdfStream = stream;
			this.size = size;
		}

		public long getSize() {
			return size;
		}

		public InputStream getInputStream() {
			return stampedPdfStream;
		}
	}
}
