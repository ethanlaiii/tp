package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddNoteCommandTest {

    private static final String NOTE_TEXT = "Weak in algebra";
    private static final String OTHER_NOTE_TEXT = "WA2 Math: 36/50";
    private static final Note NOTE = new Note(NOTE_TEXT);
    private static final Note OTHER_NOTE = new Note(OTHER_NOTE_TEXT);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_addsNote() {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEXT).build();
        String expectedMessage = String.format(AddNoteCommand.MESSAGE_SUCCESS, person.getName(), person.getSchool(),
                person.getLevel(), NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new AddNoteCommand(INDEX_FIRST_PERSON, NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_personWithGuardian_keepsGuardian() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person person = new PersonBuilder(original).withGuardian("Mary Tan", "91234567").build();
        model.setPerson(original, person);
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEXT).build();
        String expectedMessage = String.format(AddNoteCommand.MESSAGE_SUCCESS, person.getName(), person.getSchool(),
                person.getLevel(), NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new AddNoteCommand(INDEX_FIRST_PERSON, NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_existingNotes_appendsNoteAtEnd() {
        Person person = addNotesToFirstPerson(NOTE_TEXT);
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEXT, OTHER_NOTE_TEXT).build();
        String expectedMessage = String.format(AddNoteCommand.MESSAGE_SUCCESS, person.getName(), person.getSchool(),
                person.getLevel(), OTHER_NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new AddNoteCommand(INDEX_FIRST_PERSON, OTHER_NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_duplicateNote_keepsBothNotes() {
        Person person = addNotesToFirstPerson(NOTE_TEXT);
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEXT, NOTE_TEXT).build();
        String expectedMessage = String.format(AddNoteCommand.MESSAGE_SUCCESS, person.getName(), person.getSchool(),
                person.getLevel(), NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new AddNoteCommand(INDEX_FIRST_PERSON, NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_filteredList_keepsStudentShown() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEXT).build();
        String expectedMessage = String.format(AddNoteCommand.MESSAGE_SUCCESS, person.getName(), person.getSchool(),
                person.getLevel(), NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);
        expectedModel.updateFilteredPersonList(editedPerson::equals);

        assertCommandSuccess(new AddNoteCommand(INDEX_FIRST_PERSON, NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_sameNameStudents_messageNamesSchoolAndLevel() {
        Person first = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person sameName = new PersonBuilder(first).withSchool("Victoria School").withLevel("S3").build();
        model.addPerson(sameName);
        Index sameNameIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person editedPerson = new PersonBuilder(sameName).withNotes(NOTE_TEXT).build();
        String expectedMessage = "Added note to Alice Pauline at Victoria School (S3): Weak in algebra";

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(sameName, editedPerson);

        assertCommandSuccess(new AddNoteCommand(sameNameIndex, NOTE), model,
                CommandResult.showStudent(expectedMessage, editedPerson, sameNameIndex), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new AddNoteCommand(outOfBoundsIndex, NOTE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddNoteCommand(null, NOTE));
        assertThrows(NullPointerException.class, () -> new AddNoteCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equals_sameAndDifferentValues_returnsExpectedResult() {
        AddNoteCommand addNoteCommand = new AddNoteCommand(INDEX_FIRST_PERSON, NOTE);

        // same object -> returns true
        assertTrue(addNoteCommand.equals(addNoteCommand));

        // same values -> returns true
        assertTrue(addNoteCommand.equals(new AddNoteCommand(INDEX_FIRST_PERSON, new Note(NOTE_TEXT))));

        // different index -> returns false
        assertFalse(addNoteCommand.equals(new AddNoteCommand(INDEX_SECOND_PERSON, NOTE)));

        // different note -> returns false
        assertFalse(addNoteCommand.equals(new AddNoteCommand(INDEX_FIRST_PERSON, OTHER_NOTE)));

        // null -> returns false
        assertFalse(addNoteCommand.equals(null));

        // different type -> returns false
        assertFalse(addNoteCommand.equals(new ClearCommand()));
    }

    /**
     * Replaces the first person in {@code model} with a copy that has {@code notes}, and returns the copy.
     */
    private Person addNotesToFirstPerson(String... notes) {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithNotes = new PersonBuilder(person).withNotes(notes).build();
        model.setPerson(person, personWithNotes);
        return personWithNotes;
    }
}
