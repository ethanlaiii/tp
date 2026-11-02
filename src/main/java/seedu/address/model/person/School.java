package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Optional;

/**
 * Represents a Person's school in the tutor book.
 * Guarantees: immutable; is valid as declared in {@link #isValidSchool(String)}
 */
public class School {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_BLANK = "School name cannot be blank.";
    public static final String MESSAGE_TOO_LONG = "School name must be at most " + MAX_LENGTH
            + " characters; this value has %d.";
    public static final String MESSAGE_INVALID_CHARACTER = "School name \"%1$s\" contains \"%2$s\", "
            + "which isn't allowed. Use " + getAllowedCharacters() + ".";
    public static final String MESSAGE_MISSING_ASCII_LETTER =
            "School name \"%s\" must contain at least one English letter (A-Z).";
    public static final String MESSAGE_CONSTRAINTS =
            "School names must be 1-" + MAX_LENGTH + " characters, contain at least one English letter "
                    + "(A-Z), and use only " + getAllowedCharacters() + ".";

    private static final String ALLOWED_PUNCTUATION = " .'’&(),-";

    public final String value;
    private final String comparisonValue;

    /**
     * Constructs a {@code School}.
     *
     * @param school A valid school.
     */
    public School(String school) {
        requireNonNull(school);
        checkArgument(isValidSchool(school), MESSAGE_CONSTRAINTS);
        value = school;
        // A data file can hold ’, while typed input is saved with '
        comparisonValue = school.trim().replace('’', '\'').replaceAll(" +", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid school.
     */
    public static boolean isValidSchool(String test) {
        return getInvalidReason(test).isEmpty();
    }

    /**
     * Returns why {@code school} is invalid, or an empty {@code Optional} if it is valid.
     * Checks blank values, length, unsupported characters, and the required ASCII letter in that order.
     *
     * @param school The school name to validate.
     * @return The reason the school name is invalid, if any.
     */
    public static Optional<String> getInvalidReason(String school) {
        requireNonNull(school);
        if (school.isBlank()) {
            return Optional.of(MESSAGE_BLANK);
        }

        int length = school.codePointCount(0, school.length());
        if (length > MAX_LENGTH) {
            return Optional.of(String.format(MESSAGE_TOO_LONG, length));
        }

        boolean hasAsciiLetter = false;
        for (int offset = 0; offset < school.length();) {
            int codePoint = school.codePointAt(offset);
            if (isAsciiLetter(codePoint)) {
                hasAsciiLetter = true;
            }
            if (!isAllowedCharacter(codePoint)) {
                String invalidCharacter = new String(Character.toChars(codePoint));
                return Optional.of(String.format(MESSAGE_INVALID_CHARACTER, school, invalidCharacter));
            }
            offset += Character.charCount(codePoint);
        }

        if (!hasAsciiLetter) {
            return Optional.of(String.format(MESSAGE_MISSING_ASCII_LETTER, school));
        }
        return Optional.empty();
    }

    private static boolean isAllowedCharacter(int codePoint) {
        return isAsciiLetter(codePoint) || (codePoint >= '0' && codePoint <= '9')
                || ALLOWED_PUNCTUATION.indexOf(codePoint) >= 0;
    }

    private static String getAllowedCharacters() {
        return "English letters, digits, spaces, periods (.), "
                + "apostrophes ('), ampersands (&), parentheses, commas, and hyphens (-)";
    }

    private static boolean isAsciiLetter(int codePoint) {
        return (codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z');
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof School otherSchool)) {
            return false;
        }

        return comparisonValue.equals(otherSchool.comparisonValue);
    }

    @Override
    public int hashCode() {
        return comparisonValue.hashCode();
    }

}
