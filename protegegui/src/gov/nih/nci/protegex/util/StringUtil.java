package gov.nih.nci.protegex.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

/**
 * Contains utility methods to manipulate strings.
 * 
 * @author David Yee
 */
public class StringUtil {
    /**
     * Formats a message that contains a list of objects so that it can be displayed "nicely" to the user.
     * 
     * @param header The text before printing the list.
     * @param list The list of objects.
     * @param footer The text after printing the list.
     * @param indent How many spaces to use as an indent.
     * @param maxOnLine The maximum objects on a line.
     * @return the formatted message.
     */
    public static String formatMessage(String header, ArrayList<Object> list, String footer, String indent,
            int maxOnLine) {
        if (list.size() <= 0)
            return "";

        StringBuffer buffer = new StringBuffer(header);

        for (int i = 0; i < list.size(); ++i) {
            if (i == 0)
                buffer.append(indent);
            else if (i % maxOnLine != 0)
                buffer.append(", ");
            buffer.append(list.get(i).toString());
            if (i % maxOnLine == (maxOnLine - 1))
                buffer.append(",\n" + indent);
        }

        buffer.append(footer);
        return buffer.toString();
    }

    /**
     * Collapses consecutive characters within a string into one.
     * 
     * @param c The targeted character.
     * @param s The string.
     * @return The new string.
     */
    public static String collapseConsecutiveChar(char c, String s) {
        StringBuffer buffer = new StringBuffer();
        char[] array = s.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            if (i + 1 < array.length && array[i] == c && array[i + 1] == c)
                continue;
            buffer.append(array[i]);
        }
        return buffer.toString();
    }

    /**
     * Replaces UTF8 characters less than 32.
     * 
     * @param s The string.
     * @param replaceString The replacement.
     * @note To delete these characters, set replaceString to "".
     * @return the new value.
     */
    public static String replaceBadUTF8Chars(String s, String replaceString) {
        StringBuffer buffer = new StringBuffer();
        char[] array = s.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            if (array[i] < 32) {
                if (replaceString != null)
                    buffer.append(replaceString);
            } else {
                buffer.append(array[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * Returns true if the specified character is a punctuation.
     * 
     * @param c The character.
     * @return true if the specified character is a punctuation.
     */
    public static boolean isPunctuation(char c) {
        return c == '.' || c == '?' || c == '!';
    }

    /**
     * Collapse consecutive spaces into one. The only exception is a when these spaces are after a punctuation. The two
     * spaces are allowed.
     * 
     * @param s The string.
     * @return The new string.
     */
    public static String collapseSpaces(String s) {
        s = s.trim();
        StringBuffer buffer = new StringBuffer();
        char[] array = s.toCharArray();
        boolean isPrevPunctuation = false;
        for (int i = 0; i < array.length; ++i) {
            if (i + 1 < array.length && !isPrevPunctuation && array[i] == ' ' && array[i + 1] == ' ')
                continue;
            buffer.append(array[i]);
            isPrevPunctuation = isPunctuation(array[i]);
        }
        return buffer.toString();
    }

    /**
     * Returns true if text contains consecutive spaces.
     * 
     * @param text The text.
     * @return true if text contains consecutive spaces.
     */
    public static boolean containsConsecutiveSpaces(String text) {
        int n = text.length();
        char c1 = '0';
        for (int i = 0; i < n; ++i) {
            char c2 = text.charAt(i);
            if (c1 == ' ' && c2 == ' ')
                return true;
            c1 = c2;
        }
        return false;
    }

    /**
     * Extracts the first integer from the string.
     * 
     * @param s The string.
     * @return The integer.
     */
    public static int extractFirstInteger(String s) {
        int len = s.length(), i = 0, j = 0;
        if (s == null || len <= 0)
            return 0;
        for (; i < len; ++i)
            if (Character.isDigit(s.charAt(i)))
                break;
        for (j = i; j < len; ++j)
            if (!Character.isDigit(s.charAt(j)))
                break;
        if (i >= len)
            return 0;
        return Integer.parseInt(s.substring(i, j));
    }

    /**
     * Returns a value enclosed within an xml tag.
     * 
     * @param buffer The string buffer.
     * @param escapeXml If true, escapes special characters in the value field.
     * @param tagName The name of the tag.
     * @param value The value.
     * @return a value enclosed within an xml tag.
     */
    public static String createXmlTag(StringBuffer buffer, boolean escapeXml, String tagName, String value) {
        if (value == null || value.length() <= 0)
            return "";

        buffer.append("<" + tagName + ">");
        if (escapeXml)
            value = escapeXML(value);
        buffer.append(value);
        buffer.append("</" + tagName + ">");
        return buffer.toString();
    }

    public static String createXmlTag(StringBuffer buffer, boolean escapeXml, String tagName, String value,
            String attName, String attVal) {
        if (value == null || value.length() <= 0)
            return "";

        buffer.append("<" + tagName + " " + attName + "=\"" + attVal + "\">");
        if (escapeXml)
            value = escapeXML(value);
        buffer.append(value);
        buffer.append("</" + tagName + ">");
        return buffer.toString();
    }

    /**
     * Returns a value enclosed within an xml tag.
     * 
     * @param buffer The string buffer.
     * @param escapeXml If true, escapes special characters in the value field.
     * @param hashMap The hashMap that contains the values.
     * @param tagName The name of the tag.
     * @return a value enclosed within an xml tag.
     */
    public static String createXmlTag(StringBuffer buffer, boolean escapeXml, HashMap<String, String> hashMap,
            String tagName) {
        String value = "";
        if (tagName.indexOf(":") > 0) {
            value = (String) hashMap.get(tagName.substring(tagName.indexOf(":") + 1));

        } else {
            value = (String) hashMap.get(tagName);
        }
        return createXmlTag(buffer, escapeXml, tagName, value);
    }

    public static String createXmlTag(StringBuffer buffer, boolean escapeXml, HashMap<String, String> hashMap,
            String tagName, String attNam, String attVal) {
        String value = (String) hashMap.get(tagName);
        return createXmlTag(buffer, escapeXml, tagName, value, attNam, attVal);
    }

    /**
     * Returns the URL without the ending slash or backslash (if any).
     * 
     * @param url The URL.
     * @return the URL without the ending slash or backslash (if any).
     */
    public static String stripSlashfromURL(String url) {
    	if (url == null) return null;
        String newURL = url.trim();
        int n = newURL.length();
        char lastChar = newURL.charAt(n - 1);
        if (lastChar == '/' || lastChar == '\\')
            newURL = newURL.substring(0, n - 1);
        return newURL;
    }

    /**
     * Takes a string value and checks it for the five XML chars that need to be escaped so for example "foo<bar>baz"
     * becomes "foo&lt;bar&gt;baz"
     * 
     * @param s
     * @return an escaped string
     */
    public static String escapeXML(String s) {

        if ((s.indexOf("&") >= 0) || (s.indexOf("<") >= 0)) {
        	
        	
        	if (s.indexOf("<![CDATA[") > -1) {
        		
        	} else {

            return "<![CDATA[" + s + "]]>";
        	}
        	

        }

        return s;
    }

    /**
     * Converts a string to HTML.
     * 
     * @param value The string value.
     * @return a HTML string.
     */
    public static String convertToHTML(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, "\n", true);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>");
        while (tokenizer.hasMoreTokens()) {
            String text = tokenizer.nextToken().trim();
            if (text.length() > 0)
                buffer.append(text);
            else
                buffer.append("<br>");
        }
        buffer.append("</html>");
        return buffer.toString();
    }

    /**
     * Manually wraps the text.
     * 
     * @param maxCharInLine The maximum character on a line.
     * @param text The text.
     * @return The wrapped text.
     */
    public static String wrap(int maxCharInLine, String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);
        StringBuffer buffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            buffer.append(wrapOneLine(maxCharInLine, "\n", line));
        }
        return buffer.toString();
    }

    /**
     * Returns the index of the last white space.
     * 
     * @param text The text.
     * @return the index of the last white space.
     */
    public static int indexOfLastWhiteSpace(String text) {
        for (int i = text.length() - 1; i >= 0; --i) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c))
                return i;
        }
        return -1;
    }

    /**
     * Wraps only one line.
     * 
     * @param maxCharInLine The maximum character on a line.
     * @param endOfLine The end of line string (or character).
     * @param text The text.
     * @return The wrapped text.
     */
    public static String wrapOneLine(int maxCharInLine, String endOfLine, String text) {
        StringBuffer buffer = new StringBuffer();
        do {
            int start = 0, n = text.length();
            int end = start + maxCharInLine;
            if (end >= n)
                end = n;

            if (end < n && !Character.isWhitespace(text.charAt(end))) {
                String line = text.substring(start, end);
                int i = indexOfLastWhiteSpace(line);
                if (i > 0)
                    end = i;
            }

            String line = text.substring(start, end);
            if (buffer.length() > 0)
                buffer.append(endOfLine);
            buffer.append(line);
            text = text.substring(end).trim();
        } while (text.length() > 0);
        return buffer.toString();
    }

    /**
     * Returns the token in the Nth position. This method uses the entire delimiter word as a delimiter where as
     * StringTokenizer does not. When you pass the following delimiter string "DELIMITER" into the StringTokenizer, it
     * treats each character as a delimiter. So, 'D', 'E', 'L', etc are all delimiters.
     * 
     * @param text The text string.
     * @param delimiter The string delimiter.
     * @param position The specified token position.
     * @return the token in the Nth position.
     */
    public static String getToken(String text, String delimiter, int position) {
        int delimiterLength = delimiter.length(), loopCtr = 0;
        String value = "";
        while (true) {
            int i = text.indexOf(delimiter);
            if (i >= 0)
                value = text.substring(0, i);
            else
                value = text;
            if (loopCtr == position)
                break;
            if (i >= 0)
                text = text.substring(i + delimiterLength);
            else {
                value = "";
                break;
            }
            ++loopCtr;
        }
        return value;
    }

    /**
     * Returns the last token.
     * 
     * @param text The text string.
     * @param delimiter The delimiter.
     * @return the last token.
     */
    public static String getLastToken(String text, String delimiter) {
        if (text.length() <= 0)
            return "";

        int i = text.lastIndexOf(delimiter);
        if (i >= 0)
            text = text.substring(i + 1);
        return text;
    }

    /**
     * Returns the value after the delimiter in the name value pair.
     * 
     * @param delimiter The delimiter.
     * @param text The text.
     * @return the value after the delimiter in the name value pair.
     */
    public static String valueAfter(String delimiter, String text) {
        if (text == null || text.length() <= 0)
            return "";

        int i = text.indexOf(delimiter);
        if (i < 0)
            return text;

        text = text.substring(i + delimiter.length());
        text = text.trim();
        return text;
    }

    /**
     * Cleans up the text (calls trim, deleteBadUTF8Chars, collapseSpaces and escapeXML methods).
     * 
     * @param text The text.
     * @param escapeXml if true, escape special XML characters.
     * @return up the text.
     */
    public static String cleanString(String text, boolean escapeXml) {
        text = text.trim();
        text = replaceBadUTF8Chars(text, " ");
        text = collapseSpaces(text);
        if (escapeXml)
            text = escapeXML(text);
        return text;
    }

    public static String getLanguage(Object obj) {

        if (obj == null)
            return null;
        if (obj instanceof RDFSLiteral) {
            RDFSLiteral literal = (RDFSLiteral) obj;
            return literal.getLanguage();
        } else if (obj instanceof String) {
            return null;
        } else {
            HashMap<String, String> hmap = ComplexPropertyParser.parseXML((String) obj);
            return (String) hmap.get("xml:lang");
        }
    }
}
