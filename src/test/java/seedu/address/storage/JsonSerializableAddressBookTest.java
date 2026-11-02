package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.School;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path OLD_FORMAT_TYPICAL_PERSONS_FILE =
            TEST_DATA_FOLDER.resolve("oldFormatTypicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path NON_ASCII_SCHOOL_FILE = TEST_DATA_FOLDER.resolve("nonAsciiSchoolAddressBook.json");
    private static final Path NON_ASCII_SUBJECT_FILE = TEST_DATA_FOLDER.resolve("nonAsciiSubjectAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path VALID_NOTES_FILE = TEST_DATA_FOLDER.resolve("validNotesAddressBook.json");
    private static final Path MISSING_NOTES_FILE = TEST_DATA_FOLDER.resolve("missingNotesAddressBook.json");
    private static final Path NULL_NOTES_FILE = TEST_DATA_FOLDER.resolve("nullNotesAddressBook.json");
    private static final Path NULL_NOTE_ENTRY_FILE =
            TEST_DATA_FOLDER.resolve("nullNoteEntryAddressBook.json");
    private static final Path NULL_PERSON_FILE = TEST_DATA_FOLDER.resolve("nullPersonAddressBook.json");
    private static final Path BLANK_NOTE_FILE = TEST_DATA_FOLDER.resolve("blankNoteAddressBook.json");
    private static final Path TOO_LONG_NOTE_FILE = TEST_DATA_FOLDER.resolve("tooLongNoteAddressBook.json");
    private static final Path NON_ASCII_NOTE_FILE = TEST_DATA_FOLDER.resolve("nonAsciiNoteAddressBook.json");
    private static final Path NON_ASCII_ADDRESS_FILE =
            TEST_DATA_FOLDER.resolve("nonAsciiAddressAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_sameNameDifferentSchoolAndLevel_success() throws Exception {
        AddressBook source = new AddressBook();
        Person sameNameOtherSchool = new PersonBuilder(ALICE).withSchool("Victoria School").withLevel("S2").build();
        source.addPerson(ALICE);
        source.addPerson(sameNameOtherSchool);

        JsonSerializableAddressBook jsonAddressBook = new JsonSerializableAddressBook(source);

        assertEquals(List.of(ALICE, sameNameOtherSchool), jsonAddressBook.toModelType().getPersonList());
    }

    @Test
    public void toModelType_oldFormatFile_loadsAddressesAndRemovedFields() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(OLD_FORMAT_TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        List<Address> addresses = addressBookFromFile.getPersonList().stream()
                .map(person -> person.getAddress().orElseThrow()).toList();
        assertEquals(List.of(new Address("123, Jurong West Ave 6, #08-111"),
                new Address("311, Clementi Ave 2, #02-25"), new Address("wall street"),
                new Address("10th street"), new Address("michegan ave"), new Address("little tokyo"),
                new Address("4th street")), addresses);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nonAsciiSchoolFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NON_ASCII_SCHOOL_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, School.MESSAGE_CONSTRAINTS, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nonAsciiSubjectFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NON_ASCII_SUBJECT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_validNotesFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(VALID_NOTES_FILE,
                JsonSerializableAddressBook.class).get();
        Person expectedPerson = new PersonBuilder(ALICE).withNotes("Weak in algebra", "WA2 Math: 36/50",
                "Rajesh s/o Kumar's mother called", "Weak in algebra", "Bring past-year papers").build();
        assertEquals(List.of(expectedPerson), dataFromFile.toModelType().getPersonList());
    }

    @Test
    public void toModelType_missingNotesFile_loadsEmptyNotes() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(MISSING_NOTES_FILE,
                JsonSerializableAddressBook.class).get();
        assertEquals(List.of(ALICE), dataFromFile.toModelType().getPersonList());
    }

    @Test
    public void toModelType_nullNotesFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NULL_NOTES_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.NULL_NOTES_MESSAGE,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nullNoteEntryFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NULL_NOTE_ENTRY_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.NULL_NOTE_MESSAGE,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nullPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NULL_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_blankNoteFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(BLANK_NOTE_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, Note.MESSAGE_BLANK, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_tooLongNoteFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TOO_LONG_NOTE_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, Note.MESSAGE_TOO_LONG, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nonAsciiNoteFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NON_ASCII_NOTE_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, String.format(Note.MESSAGE_INVALID_CHARACTER, "ç"),
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nonAsciiAddressFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NON_ASCII_ADDRESS_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, String.format(Address.MESSAGE_INVALID_CHARACTER, "É"),
                dataFromFile::toModelType);
    }

}
