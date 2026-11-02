package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_SUBJECT_BAND_PAIR;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_GUARDIAN_PHONE_MESSAGE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_NID;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Band;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "12";
    private static final String INVALID_GUARDIAN_PHONE = "911a";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "exam_prep";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "91234567";
    private static final String VALID_NOTE = "WA2 Math: 36/50";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "exams";
    private static final String VALID_TAG_2 = "paid";

    private static final String WHITESPACE = " \t\r\n";

    private static final String COMMAND_USAGE = "Usage message";

    private static final String HINT = Messages.MESSAGE_PREFIX_WITHOUT_SPACE_HINT;

    private static final List<String> VALID_SUBJECT_NAMES = List.of("English", "Math", "Science", "History",
            "Geography", "Chemistry", "Physics", "Biology", "Literature");

    @Test
    public void validateNoArguments_emptyArguments_success() throws Exception {
        ParserUtil.validateNoArguments("", COMMAND_USAGE);
    }

    @Test
    public void validateNoArguments_nonEmptyArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, COMMAND_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                ParserUtil.validateNoArguments(" extra", COMMAND_USAGE));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void isInteger_integer_returnsTrue() {
        assertTrue(ParserUtil.isInteger("1"));
        assertTrue(ParserUtil.isInteger("0"));
        assertTrue(ParserUtil.isInteger("-1"));
        assertTrue(ParserUtil.isInteger("  99999999999  "));
        assertTrue(ParserUtil.isInteger("  -99999999999  "));
    }

    @Test
    public void isInteger_notInteger_returnsFalse() {
        assertFalse(ParserUtil.isInteger(""));
        assertFalse(ParserUtil.isInteger(WHITESPACE));
        assertFalse(ParserUtil.isInteger("3.5"));
        assertFalse(ParserUtil.isInteger("abc"));
        assertFalse(ParserUtil.isInteger("1 2"));
    }

    @Test
    public void parseNid_validInput_returnsIndex() throws Exception {
        assertEquals(Index.fromOneBased(1), ParserUtil.parseNid("1"));
        assertEquals(Index.fromOneBased(2), ParserUtil.parseNid("  2  "));
    }

    @Test
    public void parseNid_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_NID, () -> ParserUtil.parseNid("0"));
        assertThrows(ParseException.class, MESSAGE_INVALID_NID, () -> ParserUtil.parseNid("-1"));
        assertThrows(ParseException.class, MESSAGE_INVALID_NID, () -> ParserUtil.parseNid("abc"));
        assertThrows(ParseException.class, MESSAGE_INVALID_NID, () -> ParserUtil.parseNid(""));
        assertThrows(ParseException.class, MESSAGE_INVALID_NID, () -> ParserUtil.parseNid("2147483648"));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_repeatedSpaces_returnsCollapsedName() throws Exception {
        assertEquals(new Name("Rachel Walker"), ParserUtil.parseName("  Rachel   Walker  "));
    }

    @Test
    public void parseSchool_repeatedSpaces_returnsCollapsedSchool() throws Exception {
        // School.equals ignores extra spaces, so compare the saved value
        assertEquals("Victoria School", ParserUtil.parseSchool("  Victoria   School  ").value);
    }

    @Test
    public void parseSubject_repeatedSpaces_returnsCollapsedSubject() throws Exception {
        // Subject.equals ignores extra spaces, so compare the saved name
        assertEquals("Additional Mathematics",
                ParserUtil.parseSubject("  Additional   Mathematics  ", "G1").getName());
    }

    @Test
    public void parseAddress_repeatedOrdinarySpaces_collapsesSpaces() throws Exception {
        assertEquals(new Address("123 Example Road"), ParserUtil.parseAddress("  123  Example   Road  "));
    }

    @Test
    public void parseAddress_overMaximumLength_throwsParseException() {
        String longAddress = "A".repeat(Address.MAX_LENGTH + 1);
        assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseAddress(longAddress));
    }

    @Test
    public void parseAddress_nonAsciiValue_throwsParseException() {
        String expectedMessage = String.format(Address.MESSAGE_INVALID_CHARACTER, "É");
        assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseAddress("Rue de l'Église 5"));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parsePhone_formattedSingaporeNumber_returnsCleanedPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone("+65 9123 4567"));
        assertEquals(expectedPhone, ParserUtil.parsePhone("+6591234567"));
        assertEquals(expectedPhone, ParserUtil.parsePhone("9123 4567"));
        assertEquals(expectedPhone, ParserUtil.parsePhone("9123-4567"));
        assertEquals(expectedPhone, ParserUtil.parsePhone("  91234567  "));
    }

    @Test
    public void parsePhone_notSingaporeNumber_returnsCleanedPhone() throws Exception {
        assertEquals(new Phone("+442079460958"), ParserUtil.parsePhone("+44 20 7946-0958"));
        assertEquals(new Phone("+6512345678"), ParserUtil.parsePhone("+65 1234 5678")); // +65 kept
        assertEquals(new Phone("6591234567"), ParserUtil.parsePhone("65 9123 4567")); // 65 without +
        assertEquals(new Phone("9123456"), ParserUtil.parsePhone("9123 456")); // 7 digits
        assertEquals(new Phone("911"), ParserUtil.parsePhone("911")); // 3 digits
        assertEquals(new Phone("123456789012345"), ParserUtil.parsePhone("123 4567 8901 2345")); // 15 digits
    }

    @Test
    public void parsePhone_invalidCharactersOrLength_throwsParseExceptionWithReason() {
        String digitCount = " needs 3 to 15 digits, counting the country code, but has ";
        assertThrows(ParseException.class, "Phone +" + digitCount + "0.", () -> ParserUtil.parsePhone("+"));
        assertThrows(ParseException.class, "Phone 12" + digitCount + "2.", () -> ParserUtil.parsePhone(" 12 "));
        assertThrows(ParseException.class, "Phone 1234 5678 9012 3456" + digitCount + "16.", () ->
                ParserUtil.parsePhone("1234 5678 9012 3456"));

        String allowed = ", which isn't allowed. Use only digits, spaces, hyphens (-), and a + at the start.";
        assertThrows(ParseException.class, "Phone 9123.4567 contains \".\"" + allowed, () ->
                ParserUtil.parsePhone("9123.4567"));

        assertThrows(ParseException.class, "Phone 91+234567 can only have one +, at the start.", () ->
                ParserUtil.parsePhone("91+234567"));
        assertThrows(ParseException.class, "Phone cannot be empty.", () -> ParserUtil.parsePhone("  "));
    }

    @Test
    public void parseGuardianPhone_formattedSingaporeNumber_returnsCleanedPhone() throws Exception {
        assertEquals(new Phone(VALID_PHONE), ParserUtil.parseGuardianPhone("+65 9123-4567"));
    }

    @Test
    public void parseGuardianPhone_invalidValue_throwsParseExceptionNamingGuardianPhone() {
        assertThrows(ParseException.class, "Guardian phone 9123a contains \"a\", which isn't allowed. "
                + "Use only digits, spaces, hyphens (-), and a + at the start.", () ->
                ParserUtil.parseGuardianPhone("9123a"));
        assertThrows(ParseException.class, "Guardian phone - needs 3 to 15 digits, counting the country code, "
                + "but has 0.", () -> ParserUtil.parseGuardianPhone(" - "));
    }

    @Test
    public void parseGuardian_validValues_returnsGuardian() throws Exception {
        Guardian expectedGuardian = new Guardian(new Name(VALID_NAME), new Phone(VALID_PHONE));
        assertEquals(expectedGuardian, ParserUtil.parseGuardian(VALID_NAME, VALID_PHONE));
    }

    @Test
    public void parseGuardian_validValuesWithWhitespace_returnsTrimmedGuardian() throws Exception {
        Guardian expectedGuardian = new Guardian(new Name(VALID_NAME), new Phone(VALID_PHONE));
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        assertEquals(expectedGuardian, ParserUtil.parseGuardian(nameWithWhitespace, "+65 9123-4567"));
    }

    @Test
    public void parseGuardian_invalidName_throwsParseException() {
        assertThrows(ParseException.class, Guardian.MESSAGE_NAME_CONSTRAINTS, () ->
                ParserUtil.parseGuardian(INVALID_NAME, VALID_PHONE));
    }

    @Test
    public void parseGuardian_invalidPhone_throwsParseException() {
        assertThrows(ParseException.class, INVALID_GUARDIAN_PHONE_MESSAGE, () ->
                ParserUtil.parseGuardian(VALID_NAME, INVALID_GUARDIAN_PHONE));
    }

    @Test
    public void parseGuardian_bothInvalid_throwsNameError() {
        assertThrows(ParseException.class, Guardian.MESSAGE_NAME_CONSTRAINTS, () ->
                ParserUtil.parseGuardian(INVALID_NAME, INVALID_GUARDIAN_PHONE));
    }

    @Test
    public void parseGuardian_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseGuardian(null, VALID_PHONE));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseGuardian(VALID_NAME, null));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail(null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, String.format(Email.MESSAGE_MISSING_AT, INVALID_EMAIL), () ->
                ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        assertEquals(new Email(VALID_EMAIL), ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        assertEquals(new Email(VALID_EMAIL), ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseSubjects_validPairs_returnsSubjectsInOrder() throws Exception {
        List<Subject> expectedSubjects = List.of(new Subject("English", Band.G1), new Subject("Math", Band.G3));
        assertEquals(expectedSubjects, ParserUtil.parseSubjects(List.of(" English ", "Math"), List.of("G1", "G3")));
    }

    @Test
    public void parseSubjects_differentSizes_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_SUBJECT_BAND_PAIR, () ->
                ParserUtil.parseSubjects(List.of("English", "Math"), List.of("G1")));
        assertThrows(ParseException.class, MESSAGE_SUBJECT_BAND_PAIR, () ->
                ParserUtil.parseSubjects(List.of("English"), List.of("G1", "G2")));
    }

    @Test
    public void parseSubjects_emptyLists_throwsParseException() {
        assertThrows(ParseException.class, Person.MESSAGE_MISSING_SUBJECT, () ->
                ParserUtil.parseSubjects(List.of(), List.of()));
    }

    @Test
    public void parseSubjects_maxSubjects_success() throws Exception {
        List<String> names = VALID_SUBJECT_NAMES.subList(0, Person.MAX_SUBJECTS);
        List<String> bands = Collections.nCopies(Person.MAX_SUBJECTS, "G1");
        assertEquals(Person.MAX_SUBJECTS, ParserUtil.parseSubjects(names, bands).size());
    }

    @Test
    public void parseSubjects_tooManySubjects_throwsParseException() {
        List<String> names = VALID_SUBJECT_NAMES.subList(0, Person.MAX_SUBJECTS + 1);
        List<String> bands = Collections.nCopies(Person.MAX_SUBJECTS + 1, "G1");
        assertThrows(ParseException.class, Person.MESSAGE_TOO_MANY_SUBJECTS, () ->
                ParserUtil.parseSubjects(names, bands));
    }

    @Test
    public void parseSubjects_repeatedSubjectDifferentCase_throwsParseException() {
        String expectedMessage = String.format(Person.MESSAGE_REPEATED_SUBJECT, "english");
        assertThrows(ParseException.class, expectedMessage, () ->
                ParserUtil.parseSubjects(List.of("English", "english"), List.of("G1", "G2")));
    }

    @Test
    public void parseSubjects_invalidBand_throwsParseException() {
        assertThrows(ParseException.class, Band.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseSubjects(List.of("English"), List.of("G4")));
    }

    @Test
    public void parseNote_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseNote(null));
    }

    @Test
    public void parseNote_blankValue_throwsParseException() {
        assertThrows(ParseException.class, Note.MESSAGE_BLANK, () -> ParserUtil.parseNote(WHITESPACE));
    }

    @Test
    public void parseNote_tooLongValue_throwsParseException() {
        String tooLongNote = "a".repeat(Note.MAX_LENGTH + 1);
        assertThrows(ParseException.class, Note.MESSAGE_TOO_LONG, () -> ParserUtil.parseNote(tooLongNote));
    }

    @Test
    public void parseNote_nonAsciiValue_throwsParseException() {
        String expectedMessage = String.format(Note.MESSAGE_INVALID_CHARACTER, "ç");
        assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseNote("Français oral"));
    }

    @Test
    public void parseNote_validValueWithWhitespace_returnsTrimmedNote() throws Exception {
        String noteWithWhitespace = WHITESPACE + VALID_NOTE + WHITESPACE;
        assertEquals(VALID_NOTE, ParserUtil.parseNote(noteWithWhitespace).value);
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        assertEquals(new Tag(VALID_TAG_1), ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        assertEquals(VALID_TAG_1, ParserUtil.parseTag(tagWithWhitespace).tagName);
    }

    @Test
    public void parseTag_containsSpace_throwsParseException() {
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_TAG_HAS_SPACE, "exam prep"), () ->
                ParserUtil.parseTag("exam prep"));
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_TAG_HAS_SPACE, "paid  urgent"), () ->
                ParserUtil.parseTag(" paid  urgent "));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));
        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseTags_repeatedTagDifferentCase_throwsParseException() {
        String expectedMessage = String.format(Tag.MESSAGE_DUPLICATE_TAG, "Paid");
        assertThrows(ParseException.class, expectedMessage, () ->
                ParserUtil.parseTags(List.of("paid", "Paid")));
    }

    @Test
    public void getSlashHint_slash_returnsHint() {
        // a missing space, a name ending in g before p/, a prefix at the start, and a / that is not a prefix
        for (String value : List.of("John Doep/98765432", "Alex Ngp/91234567", "gp/", "Rajesh s/o Kumar")) {
            assertEquals(Optional.of(HINT), ParserUtil.getSlashHint(value));
        }
    }

    @Test
    public void getSlashHint_noSlash_returnsEmpty() {
        for (String value : List.of("John Doe", "  John   Doe  ", "")) {
            assertEquals(Optional.empty(), ParserUtil.getSlashHint(value));
        }
    }

    @Test
    public void parseName_prefixWithoutSpace_throwsParseExceptionWithHint() {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseName("John Doep/98765432"));
    }

    @Test
    public void parseName_slashWithoutPrefix_throwsParseExceptionWithHint() {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseName("Ali/Bakar"));
    }

    @Test
    public void parseGuardianName_prefixWithoutSpace_throwsParseExceptionWithHint() {
        assertThrows(ParseException.class,
                Guardian.MESSAGE_NAME_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseGuardianName("Mary Tangp/91234567"));
    }

    @Test
    public void parseFields_prefixWithoutSpace_throwsParseExceptionWithHint() {
        String phoneMessage = String.format(Phone.MESSAGE_INVALID_CHARACTER, ParserUtil.PHONE_LABEL,
                "98765432a/Blk 1", "a");
        assertThrows(ParseException.class, phoneMessage + "\n" + HINT, () ->
                ParserUtil.parsePhone("98765432a/Blk 1"));
        String joinedPhoneMessage = String.format(Phone.MESSAGE_INVALID_CHARACTER, ParserUtil.PHONE_LABEL,
                "91234567gp/92345678", "g");
        assertThrows(ParseException.class, joinedPhoneMessage + "\n" + HINT, () ->
                ParserUtil.parsePhone("91234567gp/92345678"));
        String invalidSchool = String.format(School.MESSAGE_INVALID_CHARACTER,
                "Victoria Schoole/a@b.com", "/");
        assertThrows(ParseException.class, invalidSchool + "\n" + HINT, () ->
                ParserUtil.parseSchool("Victoria Schoole/a@b.com"));
        assertThrows(ParseException.class, Level.MESSAGE_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseLevel("S2p/91234567"));
        assertThrows(ParseException.class, Band.MESSAGE_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseSubject("English", "G1t/paid"));
    }

    @Test
    public void parseEmail_prefixWithoutSpace_throwsParseExceptionWithHint() {
        String emailMessage = String.format(Email.MESSAGE_INVALID_DOMAIN_CHARACTER, "a@b.comp/91234567", "/");
        assertThrows(ParseException.class, emailMessage + "\n" + HINT, () ->
                ParserUtil.parseEmail("a@b.comp/91234567"));
    }

    @Test
    public void parseTag_prefixWithoutSpace_throwsParseExceptionWithHint() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS + "\n" + HINT, () ->
                ParserUtil.parseTag("paidp/91234567"));
        String spaceMessage = String.format(Messages.MESSAGE_TAG_HAS_SPACE, "paida/Blk 1");
        assertThrows(ParseException.class, spaceMessage + "\n" + HINT, () ->
                ParserUtil.parseTag("paida/Blk 1"));
    }
}
