package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.StringUtil.findInvalidCharacter;

import java.util.Optional;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final int MIN_DIGITS = 3;
    /** The longest international number allowed by E.164, including the country code. */
    public static final int MAX_DIGITS = 15;
    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers can only contain digits, with an optional + at the start, and must have "
            + MIN_DIGITS + " to " + MAX_DIGITS + " digits, counting the country code. "
            + "Spaces and hyphens (-) are allowed and are removed.";
    public static final String MESSAGE_EMPTY = "%1$s cannot be empty.";
    public static final String MESSAGE_INVALID_CHARACTER = "%1$s %2$s contains \"%3$s\", which isn't allowed. "
            + "Use only digits, spaces, hyphens (-), and a + at the start.";
    public static final String MESSAGE_MISPLACED_PLUS = "%1$s %2$s can only have one +, at the start.";
    public static final String MESSAGE_WRONG_DIGIT_COUNT = "%1$s %2$s needs " + MIN_DIGITS + " to " + MAX_DIGITS
            + " digits, counting the country code, but has %3$d.";
    /*
     * An optional + followed by 3 to 15 digits, stored without spaces or hyphens.
     */
    public static final String VALIDATION_REGEX = "\\+?\\d{" + MIN_DIGITS + "," + MAX_DIGITS + "}";
    /*
     * A Singapore landline (6) or mobile (8, 9) number, stored without spaces, hyphens, or country code.
     */
    private static final String SINGAPORE_REGEX = "[689]\\d{7}";
    private static final String COUNTRY_CODE = "+65";
    private static final String PLUS = "+";
    private static final String ALLOWED_CHARACTERS_REGEX = "[0-9+]";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns {@code phone} with spaces and hyphens removed, then a leading {@code +65} removed
     * if the rest is a Singapore number.
     * The result may still be invalid; check it with {@link #isValidPhone(String)}.
     */
    public static String normalize(String phone) {
        requireNonNull(phone);
        String compact = phone.replaceAll("[ -]", "");
        if (!compact.startsWith(COUNTRY_CODE)) {
            return compact;
        }
        String local = compact.substring(COUNTRY_CODE.length());
        return isSingaporeNumber(local) ? local : compact;
    }

    /**
     * Returns why {@code phone} is not a valid phone number after {@link #normalize(String)},
     * or an empty {@code Optional} if it is valid.
     *
     * @param label How the message refers to the phone, such as "Phone" or "Guardian phone".
     * @param phone The phone number as the user typed it, with surrounding spaces already removed.
     * @return The reason, naming the field and the value as typed.
     */
    public static Optional<String> getInvalidReason(String label, String phone) {
        requireNonNull(label);
        requireNonNull(phone);
        String compact = normalize(phone);
        if (isValidPhone(compact)) {
            return Optional.empty();
        }
        if (phone.isEmpty()) {
            return Optional.of(String.format(MESSAGE_EMPTY, label));
        }

        Optional<String> invalidCharacter = findInvalidCharacter(compact, ALLOWED_CHARACTERS_REGEX);
        if (invalidCharacter.isPresent()) {
            return Optional.of(String.format(MESSAGE_INVALID_CHARACTER, label, phone, invalidCharacter.get()));
        }
        if (compact.lastIndexOf(PLUS) > 0) {
            return Optional.of(String.format(MESSAGE_MISPLACED_PLUS, label, phone));
        }

        int digitCount = compact.replace(PLUS, "").length();
        assert digitCount < MIN_DIGITS || digitCount > MAX_DIGITS;
        return Optional.of(String.format(MESSAGE_WRONG_DIGIT_COUNT, label, phone, digitCount));
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if a given string is a Singapore number: 8 digits starting with 6, 8, or 9.
     */
    public static boolean isSingaporeNumber(String test) {
        return test.matches(SINGAPORE_REGEX);
    }

    /**
     * Returns true if this phone number is a Singapore number.
     */
    public boolean isSingaporeNumber() {
        return isSingaporeNumber(value);
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
