package utilities.consoleredirect;

import java.io.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Create a simple console to display text messages.
 *
 * Messages can be directed here from different sources. Each source can have
 * its messages displayed in a different color.
 *
 * Messages can either be appended to the console or inserted as the first line
 * of the console
 *
 * You can limit the number of lines to hold in the Document.
 */
public class MessageConsole {

    private final JTextComponent _textComponent;
    private final Document _document;
    private final boolean _isAppend;
    private DocumentListener _limitLinesListener;
    public int hello;

    /**
     * Use the text component specified as a simply console to display text
     * messages.
     *
     * The messages can either be appended to the end of the console or inserted
     * as the first line of the console.
     */
    public MessageConsole(final JTextComponent textComponent, final boolean isAppend) {
        _textComponent = textComponent;
        _document = textComponent.getDocument();
        _isAppend = isAppend;
        textComponent.setEditable(false);
    }

    public JTextComponent getTextComponent() {
        return _textComponent;
    }

    public Document getDocument() {
        return _document;
    }

    public boolean getIsAppend() {
        return _isAppend;
    }

    /**
     * Redirect the output from the standard output to the console using the
     * specified color and PrintStream. When a PrintStream is specified the
     * message will be added to the Document before it is also written to the
     * PrintStream.
     */
    public void redirectOut(final Color textColor, final PrintStream printStream) {
        ConsoleOutputStream cos = new ConsoleOutputStream(this, textColor, printStream);
        System.setOut(new PrintStream(cos, true));
    }

    /**
     * Redirect the output from the standard error to the console using the
     * specified color and PrintStream. When a PrintStream is specified the
     * message will be added to the Document before it is also written to the
     * PrintStream.
     */
    public void redirectErr(final Color textColor, final PrintStream printStream) {
        ConsoleOutputStream cos = new ConsoleOutputStream(this, textColor, printStream);
        System.setErr(new PrintStream(cos, true));
    }

    /**
     * To prevent memory from being used up you can control the number of lines
     * to display in the console
     *
     * This number can be dynamically changed, but the console will only be
     * updated the next time the Document is updated.
     */
    public void setMessageLines(final int lines) {
        if (_limitLinesListener != null) {
            _document.removeDocumentListener(_limitLinesListener);
        }

        _limitLinesListener = new LimitLinesDocumentListener(lines, _isAppend);
        _document.addDocumentListener(_limitLinesListener);
    }
}
