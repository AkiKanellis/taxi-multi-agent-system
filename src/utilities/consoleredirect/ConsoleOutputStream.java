package utilities.consoleredirect;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Class to intercept output from a PrintStream and add it to a Document.
 *
 * The output can optionally be redirected to a different PrintStream. The text
 * displayed in the Document can be color coded to indicate the output source.
 */
class ConsoleOutputStream extends ByteArrayOutputStream {

    private final String _EOL = System.getProperty("line.separator");
    private SimpleAttributeSet _attributes;
    private PrintStream _printStream;
    private StringBuffer _buffer = new StringBuffer(80);
    private boolean _isFirstLine;

    private MessageConsole _mc;

    /**
     * Specify the option text color and PrintStream
     */
    public ConsoleOutputStream(MessageConsole mc, Color textColor, PrintStream printStream) {
        _mc = mc;
        if (textColor != null) {
            _attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(_attributes, textColor);
        }

        _printStream = printStream;

        if (_mc.getIsAppend()) {
            _isFirstLine = true;
        }
    }

    /**
     * Override this method to intercept the output text. Each line of text
     * output will actually involve invoking this method twice:
     *
     * a) for the actual text message b) for the newLine string
     *
     * The message will be treated differently depending on whether the line
     * will be appended or inserted into the Document
     */
    public void flush() {
        String message = toString();

        if (message.length() == 0) {
            return;
        }

        if (_mc.getIsAppend()) {
            handleAppend(message);
        } else {
            handleInsert(message);
        }

        reset();
    }

    /**
     * We don't want to have blank lines in the Document. The first line added
     * will simply be the message. For additional lines it will be:
     *
     * newLine + message
     */
    private void handleAppend(String message) {
        //  This check is needed in case the text in the Document has been
        //	cleared. The buffer may contain the EOL string from the previous
        //  message.

        if (_mc.getDocument().getLength() == 0) {
            _buffer.setLength(0);
        }

        if (_EOL.equals(message)) {
            _buffer.append(message);
        } else {
            _buffer.append(message);
            clearBuffer();
        }

    }

    /**
     * We don't want to merge the new message with the existing message so the
     * line will be inserted as:
     *
     * message + newLine
     */
    private void handleInsert(String message) {
        _buffer.append(message);

        if (_EOL.equals(message)) {
            clearBuffer();
        }
    }

    /**
     * The message and the newLine have been added to the buffer in the
     * appropriate order so we can now update the Document and send the text to
     * the optional PrintStream.
     */
    private void clearBuffer() {
        //  In case both the standard out and standard err are being redirected
        //  we need to insert a newline character for the first line only

        if (_isFirstLine && _mc.getDocument().getLength() != 0) {
            _buffer.insert(0, "\n");
        }

        _isFirstLine = false;
        String line = _buffer.toString();

        try {
            if (_mc.getIsAppend()) {
                int offset = _mc.getDocument().getLength();
                _mc.getDocument().insertString(offset, line, _attributes);
                _mc.getTextComponent().setCaretPosition(_mc.getDocument().getLength());
            } else {
                _mc.getDocument().insertString(0, line, _attributes);
                _mc.getTextComponent().setCaretPosition(0);
            }
        } catch (BadLocationException ble) {
        }

        if (_printStream != null) {
            _printStream.print(line);
        }

        _buffer.setLength(0);
    }
}
