package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("12")); // 2 digits
        assertFalse(Phone.isValidPhone("1234567890123456")); // 16 digits
        assertFalse(Phone.isValidPhone("+1234567890123456")); // 16 digits with +
        assertFalse(Phone.isValidPhone("+")); // + only
        assertFalse(Phone.isValidPhone("9123+4567")); // + not at the start
        assertFalse(Phone.isValidPhone("++6591234567")); // two +
        assertFalse(Phone.isValidPhone("9123 4567")); // spaces are removed by the parser, not stored
        assertFalse(Phone.isValidPhone("9123-4567")); // hyphens are removed by the parser, not stored

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // 3 digits
        assertTrue(Phone.isValidPhone("123456789012345")); // 15 digits
        assertTrue(Phone.isValidPhone("+123456789012345")); // 15 digits with +
        assertTrue(Phone.isValidPhone("91234567")); // Singapore number
        assertTrue(Phone.isValidPhone("+447911123456")); // overseas number
        assertTrue(Phone.isValidPhone("51234567")); // 8 digits but not a Singapore number
        assertTrue(Phone.isValidPhone("71234567"));
        assertTrue(Phone.isValidPhone("01234567"));
    }

    @Test
    public void isSingaporeNumber() {
        assertTrue(Phone.isSingaporeNumber("61234567")); // landline
        assertTrue(Phone.isSingaporeNumber("81234567")); // mobile starting with 8
        assertTrue(Phone.isSingaporeNumber("91234567")); // mobile starting with 9

        assertFalse(Phone.isSingaporeNumber("51234567")); // starts with 5
        assertFalse(Phone.isSingaporeNumber("71234567")); // starts with 7
        assertFalse(Phone.isSingaporeNumber("01234567")); // starts with 0
        assertFalse(Phone.isSingaporeNumber("9123456")); // 7 digits
        assertFalse(Phone.isSingaporeNumber("912345678")); // 9 digits
        assertFalse(Phone.isSingaporeNumber("+6591234567")); // +65 is removed before storing

        assertTrue(new Phone("91234567").isSingaporeNumber());
        assertFalse(new Phone("+447911123456").isSingaporeNumber());
    }

    @Test
    public void normalize() {
        assertThrows(NullPointerException.class, () -> Phone.normalize(null));

        // spaces and hyphens removed
        assertEquals("91234567", Phone.normalize("9123 4567"));
        assertEquals("91234567", Phone.normalize("9123-4567"));

        // +65 removed when the rest is a Singapore number
        assertEquals("91234567", Phone.normalize("+65 9123 4567"));
        assertEquals("91234567", Phone.normalize("+6591234567"));

        // +65 kept when the rest is not a Singapore number
        assertEquals("+6512345678", Phone.normalize("+65 1234 5678"));
        assertEquals("+65", Phone.normalize("+65"));

        // overseas numbers keep their country code
        assertEquals("+447911123456", Phone.normalize("+44 7911-123456"));

        // 65 without + is not a country code
        assertEquals("6591234567", Phone.normalize("65 9123 4567"));

        // other dashes are not hyphens, so they are kept
        assertEquals("9123\u20134567", Phone.normalize("9123\u20134567"));
    }

    @Test
    public void getInvalidReason_validPhone_returnsEmpty() {
        assertTrue(Phone.getInvalidReason("Phone", "+65 9123-4567").isEmpty());
        assertTrue(Phone.getInvalidReason("Phone", "+44 20 7946 0958").isEmpty());
        assertTrue(Phone.getInvalidReason("Phone", "911").isEmpty());
    }

    @Test
    public void getInvalidReason_empty_returnsEmptyReason() {
        assertEquals(Optional.of("Phone cannot be empty."), Phone.getInvalidReason("Phone", ""));
    }

    @Test
    public void getInvalidReason_invalidCharacter_namesFirstInvalidCharacter() {
        String allowed = "which isn't allowed. Use only digits, spaces, hyphens (-), and a + at the start.";
        assertEquals(Optional.of("Phone 9123.4567 contains \".\", " + allowed),
                Phone.getInvalidReason("Phone", "9123.4567"));
        assertEquals(Optional.of("Phone (65) 9123 4567 contains \"(\", " + allowed),
                Phone.getInvalidReason("Phone", "(65) 9123 4567"));
        assertEquals(Optional.of("Phone 9123\u20134567 contains \"\u2013\", " + allowed),
                Phone.getInvalidReason("Phone", "9123\u20134567")); // en dash
        assertEquals(Optional.of("Guardian phone 911a contains \"a\", " + allowed),
                Phone.getInvalidReason("Guardian phone", "911a"));
    }

    @Test
    public void getInvalidReason_misplacedPlus_returnsPlusReason() {
        assertEquals(Optional.of("Phone 9123+4567 can only have one +, at the start."),
                Phone.getInvalidReason("Phone", "9123+4567"));
        assertEquals(Optional.of("Phone ++6591234567 can only have one +, at the start."),
                Phone.getInvalidReason("Phone", "++6591234567"));
    }

    @Test
    public void getInvalidReason_wrongDigitCount_returnsDigitCount() {
        assertEquals(Optional.of("Phone 12 needs 3 to 15 digits, counting the country code, but has 2."),
                Phone.getInvalidReason("Phone", "12"));
        assertEquals(Optional.of("Phone +65 needs 3 to 15 digits, counting the country code, but has 2."),
                Phone.getInvalidReason("Phone", "+65"));
        assertEquals(Optional.of("Phone 1234 5678 9012 3456 needs 3 to 15 digits, counting the country code, "
                + "but has 16."), Phone.getInvalidReason("Phone", "1234 5678 9012 3456"));
    }

    @Test
    public void getInvalidReason_onlyHyphensOrSpaces_returnsDigitCount() {
        assertEquals(Optional.of("Phone - needs 3 to 15 digits, counting the country code, but has 0."),
                Phone.getInvalidReason("Phone", "-"));
        assertEquals(Optional.of("Phone - - needs 3 to 15 digits, counting the country code, but has 0."),
                Phone.getInvalidReason("Phone", "- -"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("91234567");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("91234567")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("91234568")));
    }
}
