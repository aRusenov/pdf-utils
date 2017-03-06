package com.leapest.pdfutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PageRange;

public class PdfMerger {

	/**
	 * Merges the given PDF input streams and returns the resulting PDF as raw
	 * bytes. The caller is responsible to close the underlying streams if the method fails.
	 * 
	 * @param sources the source PDF streams
	 * @throws IOException
	 */
	public static byte[] merge(List<Source> sources) throws IOException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (PdfDocument merged = new PdfDocument(new PdfWriter(outputStream))) {
			for (Source source : sources) {
				try (PdfDocument currentDoc = new PdfDocument(new PdfReader(source.getStream()))) {

					if (StringUtils.isNotBlank(source.getPageRange())) {
						currentDoc.copyPagesTo(new PageRange(source.getPageRange()).getAllPages(), merged);
					} else {
						currentDoc.copyPagesTo(1, currentDoc.getNumberOfPages(), merged);
					}
				}
			}
		}

		return outputStream.toByteArray();
	}

	public static class Source {

		private String key;
		private String pageRange;
		private InputStream stream;

		public Source(String key, String pageRange, InputStream stream) {
			this.key = key;
			this.pageRange = pageRange;
			this.stream = stream;
		}

		public String getKey() {
			return key;
		}

		public String getPageRange() {
			return pageRange;
		}

		public InputStream getStream() {
			return stream;
		}
	}
}
