package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Band;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "12";
    private static final String INVALID_LEVEL = "S6";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().get().toString();
    private static final String VALID_SCHOOL = BENSON.getSchool().toString();
    private static final String VALID_LEVEL = BENSON.getLevel().name();
    private static final List<JsonAdaptedSubject> VALID_SUBJECTS =
            BENSON.getSubjects().stream().map(JsonAdaptedSubject::new).toList();

    @Test
    public void toModelType_invalidSchool_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, " ", null, VALID_LEVEL,
                VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, School.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_schoolWithUnicode_throwsIllegalValueException() {
        JsonAdaptedPerson unicodeSchool = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE,
                "École Française", null, VALID_LEVEL, VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, School.MESSAGE_CONSTRAINTS, unicodeSchool::toModelType);
    }

    @Test
    public void toModelType_punctuatedSchool_returnsPerson() throws Exception {
        String school = "St. Joseph's Institution";
        JsonAdaptedPerson punctuatedSchool = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, school, null,
                VALID_LEVEL, VALID_SUBJECTS);
        assertEquals(new PersonBuilder(BENSON).withSchool(school).build(), punctuatedSchool.toModelType());
    }

    @Test
    public void toModelType_nullSchool_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, null, VALID_LEVEL,
                VALID_SUBJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, School.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_addressRoundTripsAndMissingAddressLoads() throws Exception {
        Person personWithAddress = new PersonBuilder(BENSON).withAddress("12 Example Road").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithAddress));
        assertEquals(personWithAddress, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());

        String jsonWithoutAddress = json.replace("\"address\" : \"12 Example Road\",", "");
        assertEquals(new PersonBuilder(BENSON).withoutAddress().build(),
                JsonUtil.fromJsonString(jsonWithoutAddress, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_emailRoundTripsAndMissingEmailLoads() throws Exception {
        Person personWithEmail = new PersonBuilder(BENSON).withEmail("benson@example.com").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithEmail));
        assertEquals(personWithEmail, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());

        String jsonWithoutEmail = json.replaceAll(",\\s*\"email\" : \"benson@example.com\"", "");
        assertFalse(jsonWithoutEmail.contains("email"));
        assertEquals(BENSON, JsonUtil.fromJsonString(jsonWithoutEmail, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_personWithoutEmail_omitsEmailKey() throws Exception {
        assertFalse(JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON)).contains("email"));
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() throws Exception {
        Person personWithEmail = new PersonBuilder(BENSON).withEmail("benson@example.com").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithEmail))
                .replace("benson@example.com", "benson.example.com");
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_tagsRoundTripAndMissingTagsLoad() throws Exception {
        Person personWithTags = new PersonBuilder(BENSON).withTags("paid", "exams").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithTags));
        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(personWithTags, loaded);
        assertEquals(List.of(new Tag("paid"), new Tag("exams")), List.copyOf(loaded.getTags()));

        // files saved before tags existed have no tags key
        ObjectNode jsonWithoutTags = (ObjectNode) new ObjectMapper().readTree(json);
        jsonWithoutTags.remove("tags");
        assertEquals(BENSON, JsonUtil.fromJsonString(jsonWithoutTags.toString(), JsonAdaptedPerson.class)
                .toModelType());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() throws Exception {
        Person personWithTags = new PersonBuilder(BENSON).withTags("paid", "exams").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithTags));

        JsonAdaptedPerson invalidTag = JsonUtil.fromJsonString(
                json.replace("\"exams\"", "\"exam_prep\""), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, invalidTag::toModelType);

        JsonAdaptedPerson repeatedTag = JsonUtil.fromJsonString(
                json.replace("\"exams\"", "\"PAID\""), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, String.format(Tag.MESSAGE_DUPLICATE_TAG, "PAID"),
                repeatedTag::toModelType);

        ObjectNode jsonWithNullTags = (ObjectNode) new ObjectMapper().readTree(json);
        jsonWithNullTags.putNull("tags");
        JsonAdaptedPerson nullTags = JsonUtil.fromJsonString(jsonWithNullTags.toString(), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.NULL_TAGS_MESSAGE, nullTags::toModelType);
    }

    @Test
    public void toModelType_blankAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL, "  ", VALID_LEVEL,
                VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, Address.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nonAsciiAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL, "Rue de l'Église 5",
                VALID_LEVEL, VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, String.format(Address.MESSAGE_INVALID_CHARACTER, "É"),
                person::toModelType);
    }

    @Test
    public void toModelType_personWithNotes_roundTripsThroughJson() throws Exception {
        Person bensonWithNotes = new PersonBuilder(BENSON)
                .withNotes("Weak in algebra", "WA2 Math: 36/50").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithNotes));

        assertEquals(bensonWithNotes, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        assertEquals(bensonWithNotes,
                JsonUtil.fromJsonString(json.replace("G1", "g1"), JsonAdaptedPerson.class).toModelType());
        JsonAdaptedPerson invalidBand = JsonUtil.fromJsonString(json.replace("G1", "G4"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Band.MESSAGE_CONSTRAINTS, invalidBand::toModelType);
        JsonAdaptedPerson missingBand = JsonUtil.fromJsonString(
                json.replace("\"band\" : \"G1\"", "\"band\" : null"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Band.MESSAGE_CONSTRAINTS, missingBand::toModelType);
        JsonAdaptedPerson oldRecord = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL, null,
                VALID_LEVEL, null);
        assertThrows(IllegalValueException.class, Person.MESSAGE_MISSING_SUBJECT, oldRecord::toModelType);
        JsonAdaptedPerson invalidSubject = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL,
                null, VALID_LEVEL, List.of(new JsonAdaptedSubject("123", "G1")));
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, invalidSubject::toModelType);
        JsonAdaptedPerson repeatedSubject = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL,
                null, VALID_LEVEL, List.of(new JsonAdaptedSubject("English", "G1"),
                        new JsonAdaptedSubject("english", "G2")));
        assertThrows(IllegalValueException.class, "Subject is repeated: english.", repeatedSubject::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_SCHOOL, null, VALID_LEVEL, VALID_SUBJECTS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nameWithUnicode_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson("Zoë Lim", VALID_PHONE, VALID_SCHOOL, null, VALID_LEVEL,
                VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_SCHOOL, null, VALID_LEVEL,
                VALID_SUBJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_SCHOOL, null, VALID_LEVEL, VALID_SUBJECTS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_formattedPhone_returnsCleanedPhone() throws Exception {
        // hand-edited files are cleaned the same way as typed phones
        JsonAdaptedPerson singapore =
                new JsonAdaptedPerson(VALID_NAME, "+65 9123-4567", VALID_SCHOOL, null, VALID_LEVEL, VALID_SUBJECTS);
        assertEquals(new Phone("91234567"), singapore.toModelType().getPhone().orElseThrow());

        JsonAdaptedPerson overseas =
                new JsonAdaptedPerson(VALID_NAME, "+44 20 7946 0958", VALID_SCHOOL, null, VALID_LEVEL, VALID_SUBJECTS);
        assertEquals(new Phone("+442079460958"), overseas.toModelType().getPhone().orElseThrow());
    }

    @Test
    public void toModelType_formattedGuardianPhone_returnsCleanedPhone() throws Exception {
        Person bensonWithGuardian = new PersonBuilder(BENSON).withGuardian("Mary Tan", "91234567").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithGuardian));

        JsonAdaptedPerson formattedPhone = JsonUtil.fromJsonString(
                json.replace("\"91234567\"", "\"+65 9123-4567\""), JsonAdaptedPerson.class);
        assertEquals(bensonWithGuardian, formattedPhone.toModelType());
    }

    @Test
    public void toModelType_nullPhone_returnsPersonWithoutPhone() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_SCHOOL, null, VALID_LEVEL,
                VALID_SUBJECTS);
        assertEquals(new PersonBuilder(BENSON).withoutPhone().build(), person.toModelType());
    }

    @Test
    public void toModelType_personWithoutPhone_roundTripsThroughJson() throws Exception {
        Person bensonWithoutPhone = new PersonBuilder(BENSON).withoutPhone().build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithoutPhone));

        assertFalse(json.contains("phone"));
        assertEquals(bensonWithoutPhone, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_overseasPhones_roundTripsThroughJson() throws Exception {
        Person bensonOverseas = new PersonBuilder(BENSON).withPhone("+442079460958")
                .withGuardian("Mary Tan", "+60123456789").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonOverseas));

        assertEquals(bensonOverseas, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_personWithGuardian_roundTripsThroughJson() throws Exception {
        Person bensonWithGuardian = new PersonBuilder(BENSON).withGuardian("Mary Tan", "91234567").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithGuardian));

        assertEquals(bensonWithGuardian, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_personWithoutGuardian_omitsGuardianKey() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));

        assertFalse(json.contains("guardian"));
        assertEquals(BENSON, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_invalidGuardian_throwsIllegalValueException() throws Exception {
        Person bensonWithGuardian = new PersonBuilder(BENSON).withGuardian("Mary Tan", "91234567").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithGuardian));

        JsonAdaptedPerson invalidName = JsonUtil.fromJsonString(
                json.replace("Mary Tan", "Mary&"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Guardian.MESSAGE_NAME_CONSTRAINTS, invalidName::toModelType);

        JsonAdaptedPerson invalidPhone = JsonUtil.fromJsonString(
                json.replace("91234567", "9123a"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Guardian.MESSAGE_PHONE_CONSTRAINTS, invalidPhone::toModelType);

        JsonAdaptedPerson missingName = JsonUtil.fromJsonString(
                json.replace("\"Mary Tan\"", "null"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedGuardian.MISSING_FIELD_MESSAGE_FORMAT,
                Name.class.getSimpleName()), missingName::toModelType);

        JsonAdaptedPerson missingPhone = JsonUtil.fromJsonString(
                json.replace("\"91234567\"", "null"), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedGuardian.MISSING_FIELD_MESSAGE_FORMAT,
                Phone.class.getSimpleName()), missingPhone::toModelType);
    }

    @Test
    public void toModelType_invalidLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL, null,
                INVALID_LEVEL, VALID_SUBJECTS);
        assertThrows(IllegalValueException.class, Level.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_SCHOOL, null, null,
                VALID_SUBJECTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Level.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

}
