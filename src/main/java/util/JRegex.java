package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JRegex emulates a core subset of the Stevesoft Regex methods.
 *
 * The methods supported are:
 *
 * JRegex(String regex, String replacementText)
 * JRegex(String regex)
 *
 * String replaceFirst(String sourceText)
 * String replaceAll(String sourceText)
 * boolean search(String sourceText)
 * String stringMatch()
 * String stringMatch(int groupNbr)
 */

public class JRegex {

    private static final Logger sLogger = Logger.getLogger(JRegex.class.getName());

    String regex;
    String replacementText;
    Pattern pattern;
    Matcher matcher;

    public JRegex() {}

    public JRegex(String regex, String replacementText) {
        this.regex = regex;
        this.replacementText = replacementText;
        this.pattern = Pattern.compile(regex);
    }

    public JRegex(String regex) {
        this(regex, null);
    }
    
    public JRegex clone() {
        return new JRegex(regex,replacementText);
    }

    /**
     * @return <code>sourceText</code> with the first occurrence of the 
     * regex replaced with the replacement text. Returns null when sourceText
     * is null (Stevesoft throws a NullPointerException).
     */
    public String replaceFirst(String sourceText) {
        String modText = sourceText;

        // Stevesoft throws a NullPointerException when sourceText is null.
        if (sourceText != null && replacementText != null) {
            try {
                Matcher matcher = pattern.matcher(sourceText);
                modText = matcher.replaceFirst(replacementText);
            } catch (Exception ex) {
                sLogger.info("replaceFirst");
            }
        }

        return modText;
    }

    /**
     * @return <code>sourceText</code> with the all occurrences of the 
     * regex replaced with the replacement text. Returns null when sourceText
     * is null (Stevesoft throws a NullPointerException).
     */
    public String replaceAll(String sourceText) {
        String modText = sourceText;

        // Stevesoft throws a NullPointerException when sourceText is null.
        if (sourceText != null && replacementText != null) {
            try {
                Matcher matcher = pattern.matcher(sourceText);
                modText = matcher.replaceAll(replacementText);
            } catch (Exception ex) {
                sLogger.info("replaceAll");
            }
        }

        return modText;
    }

    /**
     * @return true if the <code>sourceText</code> contains the regex
     * pattern specified in the constructor.
     */
    public boolean search(String sourceText) {
        boolean found = false;

        if (sourceText != null) {
            try {
                this.matcher = pattern.matcher(sourceText);
                found = matcher.find();
            } catch (Exception ex) {
                sLogger.info("search");
            }
        }

        return found;
    }

    public String left(final String sourceText) throws Exception {
        //String modText = sourceText;
        if (sourceText != null) {
            Matcher matcher = pattern.matcher(sourceText);
            if (matcher.find() && matcher.start() > -1) {
                return sourceText.substring(0, matcher.start());
            }
        }
        return "";
    }

    public String right(final String sourceText) throws Exception {
        //String modText = sourceText;
        if (sourceText != null) {
            Matcher matcher = pattern.matcher(sourceText);
            if (matcher.find() && matcher.start() > -1) {
                return sourceText.substring(matcher.end());
            }
        }
        return "";
    }

    /**
     * @return the entire input sequence matched by the previous search.
     */
    public String stringMatched() {
        return stringMatched(Integer.MAX_VALUE);
    }

    /**
     * @return the input sequence captured by the given <code>groupNbr</code>
     * during the previous search.
     */
    public String stringMatched(int groupNbr) {
        String matched = null;

        // Stevesoft returns null when the groupNbr is 0, and throws an
        // ArrayIndexOutOfBounds exception when the groupNbr is negative.
        // We return null in both cases rather than emulate Stevesoft's bugs.
        if (groupNbr <= 0) {
            return null;
        }

        // A groupNbr of MAX_VALUE is a flag used then stringMatched is
        // called without a parameter. With no parameter, stringMatched
        // returns the entire matching pattern.
        if (groupNbr == Integer.MAX_VALUE) {
            groupNbr = 0;
        }

        if (groupNbr <= this.matcher.groupCount()) {
            matched = this.matcher.group(groupNbr);
        }

        return matched;
    }

    @XmlElement(name="regex")
    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    @XmlElement(name="replacement")
    public String getReplacementText() {
        return replacementText;
    }

    public void setReplacementText(String replacementText) {
        this.replacementText = replacementText;
    }
}
