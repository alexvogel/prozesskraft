package license4j.examples;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class UpperCaseDocumentFilter extends DocumentFilter {

    private int len;
    
    public UpperCaseDocumentFilter(int len) {
        super();
        
        this.len = len;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,AttributeSet attr) throws BadLocationException {
        Document doc = fb.getDocument();
        String oldText = doc.getText(0, doc.getLength());
        StringBuilder sb = new StringBuilder(oldText);
        sb.insert(offset, text);

        if (sb.toString().length() <= len) {
            fb.insertString(offset, text.toUpperCase(), attr);
        }
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        String oldText = doc.getText(0, doc.getLength());
        StringBuilder sb = new StringBuilder(oldText);

        sb.replace(offset, offset + length, text);

        if (sb.toString().length() <= len) {
            fb.replace(offset, length, text.toUpperCase(), attrs);
        }
    }
}