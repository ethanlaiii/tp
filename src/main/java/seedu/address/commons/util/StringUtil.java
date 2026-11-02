package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /** Any Unicode whitespace, such as a tab, a non-breaking space, or a full-width space. */
    private static final String WHITESPACE_REGEX = "\\p{IsWhite_Space}";

    /**
     * Invisible format and control characters, such as a zero-width space or a left-to-right mark.
     * The zero-width joiner and non-joiner are kept, because emoji and some scripts need them.
     */
    private static final String INVISIBLE_CHARACTER_REGEX = "[\\p{Cf}\\p{Cc}&&[^\\u200C\\u200D]]";

    /** Curly quotes, which pasted text often has in place of the ' and " typed on a keyboard. */
    private static final String CURLY_APOSTROPHE_REGEX = "[\\u2018\\u2019]";
    private static final String CURLY_DOUBLE_QUOTE_REGEX = "[\\u201C\\u201D]";

    /** En and em dashes, which pasted or autocorrected text often has in place of a hyphen. */
    private static final String LONG_DASH_REGEX = "[\\u2013\\u2014]";

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns true if {@code text} contains {@code searchText}, ignoring case, treating
     * any run of whitespace in either as a single space, and treating curly quotes and long dashes
     * as {@code '}, {@code "}, and {@code -}.
     *   <br>examples:<pre>
     *       containsIgnoreCaseAndSpaces("Victoria  School", "TORIA sch") == true
     *       containsIgnoreCaseAndSpaces("Victoria School", "raffles") == false
     *       </pre>
     *
     * @param text Text to search in.
     * @param searchText Text to look for. Must not be blank.
     * @return True if {@code text} contains {@code searchText}.
     */
    public static boolean containsIgnoreCaseAndSpaces(String text, String searchText) {
        requireNonNull(text);
        requireNonNull(searchText);
        checkArgument(!searchText.isBlank(), "Search text cannot be blank");
        return normalizeForSearch(text).contains(normalizeForSearch(searchText));
    }

    private static String normalizeForSearch(String value) {
        String spacedValue = value.replaceAll(WHITESPACE_REGEX + "+", " ").strip();
        return straightenPunctuation(spacedValue).toLowerCase(Locale.ROOT);
    }

    private static String straightenPunctuation(String value) {
        return value.replaceAll(CURLY_APOSTROPHE_REGEX, "'")
                .replaceAll(CURLY_DOUBLE_QUOTE_REGEX, "\"")
                .replaceAll(LONG_DASH_REGEX, "-");
    }

    /**
     * Returns {@code input} as if it had been typed with a standard keyboard, so that text pasted
     * from web pages or documents is read the same way as typed text.
     * Every kind of whitespace becomes a normal space, invisible characters are removed,
     * accented letters are composed into their single-character form,
     * and curly quotes and en and em dashes become {@code '}, {@code "}, and {@code -}.
     * For example, a non-breaking space before {@code p/} becomes a normal space, so the prefix is recognized,
     * and a zero-width space pasted inside {@code Bedok} is removed.
     *
     * @param input Text to normalize.
     * @return The normalized text.
     */
    public static String normalizeInput(String input) {
        requireNonNull(input);
        // Whitespace goes first, so that a tab or line break becomes a space instead of being removed
        String spacedInput = input.replaceAll(WHITESPACE_REGEX, " ");
        // An invisible character between a letter and its accent stops them composing, so remove it first
        String visibleInput = spacedInput.replaceAll(INVISIBLE_CHARACTER_REGEX, "");
        return straightenPunctuation(Normalizer.normalize(visibleInput, Normalizer.Form.NFC));
    }

    /**
     * Returns the first character in {@code text} that {@code allowedCharacterRegex} doesn't match,
     * or an empty {@code Optional} if every character is allowed.
     * A character made of two UTF-16 chars, such as an emoji, is returned whole.
     *
     * @param text Text to check.
     * @param allowedCharacterRegex Regex that matches one allowed character.
     * @return The first character that isn't allowed.
     */
    public static Optional<String> findInvalidCharacter(String text, String allowedCharacterRegex) {
        requireNonNull(text);
        requireNonNull(allowedCharacterRegex);
        String invalidCharacters = text.replaceAll(allowedCharacterRegex, "");
        if (invalidCharacters.isEmpty()) {
            return Optional.empty();
        }
        int firstLength = Character.charCount(invalidCharacters.codePointAt(0));
        return Optional.of(invalidCharacters.substring(0, firstLength));
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
