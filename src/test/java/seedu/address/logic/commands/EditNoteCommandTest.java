package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.predicate.NoteContainsTextPredicate;
import seedu.address.testutil.PersonBuilder;

public class EditNoteCommandTest {

    private static final String NOTE_ALGEBRA = "Weak in algebra";
    private static final String NOTE_TEST_SCORE = "WA2 Math: 36/50";
    private static final String NOTE_CALCULATOR = "Bring calculator";
    private static final String NOTE_IMPROVING = "Improving in algebra";

    private static final Index NID_FIRST = Index.fromOneBased(1);
    private static final Index NID_SECOND = Index.fromOneBased(2);
    private static final Index NID_THIRD = Index.fromOneBased(3);

    private static final Predicate<Person> HAS_ALGEBRA_NOTE = new NoteContainsTextPredicate(NOTE_ALGEBRA);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_middleNid_replacesOnlyThatNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);

        assertEditNoteSuccess(NID_SECOND, NOTE_IMPROVING, NOTE_TEST_SCORE,
                NOTE_ALGEBRA, NOTE_IMPROVING, NOTE_CALCULATOR);
    }

    @Test
    public void execute_firstAndLastNid_replacesNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);

        assertEditNoteSuccess(NID_FIRST, NOTE_IMPROVING, NOTE_ALGEBRA,
                NOTE_IMPROVING, NOTE_TEST_SCORE, NOTE_CALCULATOR);
        assertEditNoteSuccess(NID_THIRD, NOTE_ALGEBRA, NOTE_CALCULATOR,
                NOTE_IMPROVING, NOTE_TEST_SCORE, NOTE_ALGEBRA);
    }

    @Test
    public void execute_duplicateNotes_replacesOnlyGivenNid() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_ALGEBRA);

        assertEditNoteSuccess(NID_THIRD, NOTE_IMPROVING, NOTE_ALGEBRA,
                NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_IMPROVING);
    }

    @Test
    public void execute_textMatchingAnotherNote_replacesNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);

        assertEditNoteSuccess(NID_SECOND, NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_ALGEBRA, NOTE_ALGEBRA);
    }

    @Test
    public void execute_caseChangeOnly_replacesNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA);

        assertEditNoteSuccess(NID_FIRST, "weak in Algebra", NOTE_ALGEBRA, "weak in Algebra");
    }

    @Test
    public void execute_noteWithSymbols_showsWholeNotesInMessage() {
        String symbolNote = "Rajesh s/o Kumar: WA2 36/50 (Mary-Jane's class)";
        String maxLengthNote = "a".repeat(Note.MAX_LENGTH);
        addNotesToFirstPerson(symbolNote);

        assertEditNoteSuccess(NID_FIRST, maxLengthNote, symbolNote, maxLengthNote);
        assertEditNoteSuccess(NID_FIRST, symbolNote, maxLengthNote, symbolNote);
    }

    @Test
    public void execute_personWithGuardian_keepsGuardian() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person person = new PersonBuilder(original).withGuardian("Mary Tan", "91234567")
                .withNotes(NOTE_ALGEBRA).build();
        model.setPerson(original, person);

        assertEditNoteSuccess(NID_FIRST, NOTE_IMPROVING, NOTE_ALGEBRA, NOTE_IMPROVING);
    }

    @Test
    public void execute_filteredList_keepsStudentShown() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_IMPROVING, NOTE_TEST_SCORE).build();
        String expectedMessage = String.format(EditNoteCommand.MESSAGE_SUCCESS, 1, person.getName(),
                person.getSchool(), person.getLevel(), NOTE_ALGEBRA, NOTE_IMPROVING);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);
        expectedModel.updateFilteredPersonList(editedPerson::equals);

        assertCommandSuccess(new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST, new Note(NOTE_IMPROVING)),
                model, CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_filteredByNoteNoLongerMatches_staysAtSameIndex() {
        Person first = addNotesToPerson(INDEX_FIRST_PERSON, NOTE_ALGEBRA);
        Person second = addNotesToPerson(INDEX_SECOND_PERSON, NOTE_ALGEBRA);
        model.updateFilteredPersonList(HAS_ALGEBRA_NOTE);
        Person editedFirst = new PersonBuilder(first).withNotes(NOTE_CALCULATOR).build();
        String expectedMessage = String.format(EditNoteCommand.MESSAGE_SUCCESS, 1, first.getName(),
                first.getSchool(), first.getLevel(), NOTE_ALGEBRA, NOTE_CALCULATOR)
                + "\n" + String.format(EditCommand.MESSAGE_NO_LONGER_MATCHES, first.getName(),
                        first.getSchool(), first.getLevel());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(first, editedFirst);
        expectedModel.updateFilteredPersonList(person -> person.equals(editedFirst) || person.equals(second));

        assertCommandSuccess(new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST, new Note(NOTE_CALCULATOR)),
                model, CommandResult.showStudent(expectedMessage, editedFirst, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_filteredByNoteStillMatches_noExtraLine() {
        Person first = addNotesToPerson(INDEX_FIRST_PERSON, NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person second = addNotesToPerson(INDEX_SECOND_PERSON, NOTE_ALGEBRA);
        model.updateFilteredPersonList(HAS_ALGEBRA_NOTE);
        Person editedFirst = new PersonBuilder(first).withNotes(NOTE_ALGEBRA, NOTE_CALCULATOR).build();
        String expectedMessage = String.format(EditNoteCommand.MESSAGE_SUCCESS, 2, first.getName(),
                first.getSchool(), first.getLevel(), NOTE_TEST_SCORE, NOTE_CALCULATOR);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(first, editedFirst);
        expectedModel.updateFilteredPersonList(HAS_ALGEBRA_NOTE);

        assertCommandSuccess(new EditNoteCommand(INDEX_FIRST_PERSON, NID_SECOND, new Note(NOTE_CALCULATOR)),
                model, CommandResult.showStudent(expectedMessage, editedFirst, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_noLongerMatchesThenEditNoteAgain_editsSameStudent() throws Exception {
        Person first = addNotesToPerson(INDEX_FIRST_PERSON, NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person second = addNotesToPerson(INDEX_SECOND_PERSON, NOTE_ALGEBRA);
        model.updateFilteredPersonList(HAS_ALGEBRA_NOTE);

        new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST, new Note(NOTE_CALCULATOR)).execute(model);
        new EditNoteCommand(INDEX_FIRST_PERSON, NID_SECOND, new Note(NOTE_IMPROVING)).execute(model);

        Person editedFirst = new PersonBuilder(first).withNotes(NOTE_CALCULATOR, NOTE_IMPROVING).build();
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new EditNoteCommand(outOfBoundsIndex, NID_FIRST, new Note(NOTE_IMPROVING)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexOutsideFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new EditNoteCommand(INDEX_SECOND_PERSON, NID_FIRST, new Note(NOTE_IMPROVING)),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_personWithNoNotes_throwsCommandException() {
        assertCommandFailure(new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST, new Note(NOTE_IMPROVING)),
                model, "Alice Pauline at Maplewood Secondary School (S1) has no notes to edit.");
    }

    @Test
    public void execute_nidOneMoreThanNoteCount_throwsCommandException() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);

        assertCommandFailure(new EditNoteCommand(INDEX_FIRST_PERSON, NID_THIRD, new Note(NOTE_IMPROVING)),
                model, "NID 3 does not exist. Alice Pauline at Maplewood Secondary School (S1) has only 2 notes.");
    }

    @Test
    public void execute_nidLargerThanSingleNote_usesSingularNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA);
        Index maxIntNid = Index.fromOneBased(Integer.MAX_VALUE);

        assertCommandFailure(new EditNoteCommand(INDEX_FIRST_PERSON, maxIntNid, new Note(NOTE_IMPROVING)),
                model, "NID 2147483647 does not exist. Alice Pauline at Maplewood Secondary School (S1) has only "
                        + "1 note.");
    }

    @Test
    public void execute_sameText_warnsAndKeepsModelUnchanged() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CommandResult expectedResult = CommandResult.showStudentWarning(
                "No changes made: note 2 of Alice Pauline at Maplewood Secondary School (S1) already says: "
                        + "WA2 Math: 36/50",
                person, INDEX_FIRST_PERSON);

        assertCommandSuccess(new EditNoteCommand(INDEX_FIRST_PERSON, NID_SECOND, new Note(NOTE_TEST_SCORE)),
                model, expectedResult, expectedModel);
    }

    @Test
    public void execute_sameNameStudents_messageNamesSchoolAndLevel() {
        Person first = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person sameName = new PersonBuilder(first).withSchool("Victoria School").withLevel("S3")
                .withNotes(NOTE_ALGEBRA).build();
        model.addPerson(sameName);
        Index sameNameIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person editedPerson = new PersonBuilder(sameName).withNotes(NOTE_IMPROVING).build();
        String expectedMessage = "Edited note 1 of Alice Pauline at Victoria School (S3).\n"
                + "Old: Weak in algebra\nNew: Improving in algebra";

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(sameName, editedPerson);

        assertCommandSuccess(new EditNoteCommand(sameNameIndex, NID_FIRST, new Note(NOTE_IMPROVING)), model,
                CommandResult.showStudent(expectedMessage, editedPerson, sameNameIndex), expectedModel);
    }

    @Test
    public void execute_invalidIndexAndInvalidNid_throwsIndexError() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new EditNoteCommand(outOfBoundsIndex, NID_THIRD, new Note(NOTE_IMPROVING)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals_sameAndDifferentValues_returnsExpectedResult() {
        EditNoteCommand editNoteCommand = new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST,
                new Note(NOTE_IMPROVING));

        // same object -> returns true
        assertTrue(editNoteCommand.equals(editNoteCommand));

        // same values -> returns true
        assertTrue(editNoteCommand.equals(
                new EditNoteCommand(Index.fromOneBased(1), Index.fromOneBased(1), new Note(NOTE_IMPROVING))));

        // different index -> returns false
        assertFalse(editNoteCommand.equals(
                new EditNoteCommand(INDEX_SECOND_PERSON, NID_FIRST, new Note(NOTE_IMPROVING))));

        // different NID -> returns false
        assertFalse(editNoteCommand.equals(
                new EditNoteCommand(INDEX_FIRST_PERSON, NID_SECOND, new Note(NOTE_IMPROVING))));

        // different text -> returns false
        assertFalse(editNoteCommand.equals(
                new EditNoteCommand(INDEX_FIRST_PERSON, NID_FIRST, new Note(NOTE_ALGEBRA))));

        // null -> returns false
        assertFalse(editNoteCommand.equals(null));

        // different type -> returns false
        assertFalse(editNoteCommand.equals(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_FIRST)));
    }

    @Test
    public void toStringMethod() {
        EditNoteCommand editNoteCommand = new EditNoteCommand(INDEX_FIRST_PERSON, NID_SECOND,
                new Note(NOTE_IMPROVING));
        String expected = EditNoteCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", targetNid=" + NID_SECOND + ", newNote=" + NOTE_IMPROVING + "}";

        assertEquals(expected, editNoteCommand.toString());
    }

    /**
     * Asserts that replacing note {@code nid} of the first person in {@code model} with {@code newText}
     * succeeds, reports {@code oldText} and {@code newText}, and leaves the person with {@code expectedNotes}.
     */
    private void assertEditNoteSuccess(Index nid, String newText, String oldText, String... expectedNotes) {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(person).withNotes(expectedNotes).build();
        String expectedMessage = String.format(EditNoteCommand.MESSAGE_SUCCESS, nid.getOneBased(),
                person.getName(), person.getSchool(), person.getLevel(), oldText, newText);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new EditNoteCommand(INDEX_FIRST_PERSON, nid, new Note(newText)), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    /**
     * Replaces the first person in {@code model} with a copy that has {@code notes}, and returns the copy.
     */
    private Person addNotesToFirstPerson(String... notes) {
        return addNotesToPerson(INDEX_FIRST_PERSON, notes);
    }

    /**
     * Replaces the person at {@code index} in {@code model} with a copy that has {@code notes}, and returns
     * the copy.
     */
    private Person addNotesToPerson(Index index, String... notes) {
        Person person = model.getFilteredPersonList().get(index.getZeroBased());
        Person personWithNotes = new PersonBuilder(person).withNotes(notes).build();
        model.setPerson(person, personWithNotes);
        return personWithNotes;
    }
}
