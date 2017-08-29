/**
 * Simple document filter for license key.
 */
package com.license4j;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class UpperCaseDocumentFilter extends DocumentFilter {

    public UpperCaseDocumentFilter() {
        super();
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        Document doc = fb.getDocument();
        String oldText = doc.getText(0, doc.getLength());
        StringBuilder sb = new StringBuilder(oldText);
        sb.insert(offset, text);

        fb.insertString(offset, text.toUpperCase(), attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        String oldText = doc.getText(0, doc.getLength());
        StringBuilder sb = new StringBuilder(oldText);

        sb.replace(offset, offset + length, text);

        fb.replace(offset, length, text.toUpperCase(), attrs);
    }
}
