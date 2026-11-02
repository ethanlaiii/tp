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
import seedu.address.testutil.PersonBuilder;

public class DeleteNoteCommandTest {

    private static final String NOTE_ALGEBRA = "Weak in algebra";
    private static final String NOTE_TEST_SCORE = "WA2 Math: 36/50";
    private static final String NOTE_CALCULATOR = "Bring calculator";

    private static final Index NID_FIRST = Index.fromOneBased(1);
    private static final Index NID_SECOND = Index.fromOneBased(2);
    private static final Index NID_THIRD = Index.fromOneBased(3);

    // stands in for a search by note text, which no command offers yet
    private static final Predicate<Person> HAS_ALGEBRA_NOTE =
            person -> person.getNotes().contains(new Note(NOTE_ALGEBRA));

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void withoutForce_forcedCommand_returnsUnforcedCommand() {
        assertEquals(new DeleteNoteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                new DeleteNoteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, true).withoutForce());
    }

    @Test
    public void execute_middleNid_deletesNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);

        assertDeleteNoteSuccess(NID_SECOND, NOTE_TEST_SCORE, NOTE_ALGEBRA, NOTE_CALCULATOR);
    }

    @Test
    public void execute_firstNid_laterNotesMoveUp() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);

        assertDeleteNoteSuccess(NID_FIRST, NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);
    }

    @Test
    public void execute_lastNid_deletesNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_CALCULATOR);

        assertDeleteNoteSuccess(NID_THIRD, NOTE_CALCULATOR, NOTE_ALGEBRA, NOTE_TEST_SCORE);
    }

    @Test
    public void execute_onlyNote_leavesNoNotes() {
        addNotesToFirstPerson(NOTE_ALGEBRA);

        assertDeleteNoteSuccess(NID_FIRST, NOTE_ALGEBRA);
    }

    @Test
    public void execute_duplicateNotes_deletesOnlyGivenNid() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE, NOTE_ALGEBRA);

        assertDeleteNoteSuccess(NID_THIRD, NOTE_ALGEBRA, NOTE_ALGEBRA, NOTE_TEST_SCORE);
    }

    @Test
    public void execute_personWithGuardian_keepsGuardian() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person person = new PersonBuilder(original).withGuardian("Mary Tan", "91234567")
                .withNotes(NOTE_ALGEBRA, NOTE_TEST_SCORE).build();
        model.setPerson(original, person);

        assertDeleteNoteSuccess(NID_FIRST, NOTE_ALGEBRA, NOTE_TEST_SCORE);
    }

    @Test
    public void execute_noteWithSymbols_showsWholeNoteInMessage() {
        String symbolNote = "Rajesh s/o Kumar: WA2 36/50 (Mary-Jane's class)";
        String maxLengthNote = "a".repeat(500);
        addNotesToFirstPerson(symbolNote, maxLengthNote);

        assertDeleteNoteSuccess(NID_FIRST, symbolNote, maxLengthNote);
        assertDeleteNoteSuccess(NID_FIRST, maxLengthNote);
    }

    @Test
    public void execute_filteredList_keepsStudentShown() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person editedPerson = new PersonBuilder(person).withNotes(NOTE_TEST_SCORE).build();
        String expectedMessage = String.format(DeleteNoteCommand.MESSAGE_SUCCESS, 1, person.getName(),
                person.getSchool(), person.getLevel(), NOTE_ALGEBRA);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);
        expectedModel.updateFilteredPersonList(editedPerson::equals);

        assertCommandSuccess(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_FIRST), model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_filteredByNoteNoLongerMatches_staysAtSameIndex() {
        Person first = addNotesToPerson(INDEX_FIRST_PERSON, NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person second = addNotesToPerson(INDEX_SECOND_PERSON, NOTE_ALGEBRA);
        model.updateFilteredPersonList(HAS_ALGEBRA_NOTE);
        Person editedFirst = new PersonBuilder(first).withNotes(NOTE_TEST_SCORE).build();
        String expectedMessage = String.format(DeleteNoteCommand.MESSAGE_SUCCESS, 1, first.getName(),
                first.getSchool(), first.getLevel(), NOTE_ALGEBRA)
                + "\n" + String.format(EditCommand.MESSAGE_NO_LONGER_MATCHES, first.getName(),
                        first.getSchool(), first.getLevel());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(first, editedFirst);
        expectedModel.updateFilteredPersonList(person -> person.equals(editedFirst) || person.equals(second));

        assertCommandSuccess(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_FIRST), model,
                CommandResult.showStudent(expectedMessage, editedFirst, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_filteredByNoteStillMatches_noExtraLine() {
        Person first = addNotesToPerson(INDEX_FIRST_PERSON, NOTE_ALGEBRA, NOTE_TEST_SCORE);
        Person second = addNotesToPerson(INDEX_SECOND_PERSON, NOTE_ALGEBRA);
        model.updateFilteredPersonList(HAS_ALGEBRA_NOTE);
        Person editedFirst = new PersonBuilder(first).withNotes(NOTE_ALGEBRA).build();
        String expectedMessage = String.format(DeleteNoteCommand.MESSAGE_SUCCESS, 2, first.getName(),
                first.getSchool(), first.getLevel(), NOTE_TEST_SCORE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(first, editedFirst);
        expectedModel.updateFilteredPersonList(HAS_ALGEBRA_NOTE);

        assertCommandSuccess(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_SECOND), model,
                CommandResult.showStudent(expectedMessage, editedFirst, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DeleteNoteCommand(outOfBoundsIndex, NID_FIRST), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexOutsideFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DeleteNoteCommand(INDEX_SECOND_PERSON, NID_FIRST), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_personWithNoNotes_throwsCommandException() {
        assertCommandFailure(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_FIRST), model,
                "Alice Pauline at Maplewood Secondary School (S1) has no notes to delete.");
    }

    @Test
    public void execute_nidOneMoreThanNoteCount_throwsCommandException() {
        addNotesToFirstPerson(NOTE_ALGEBRA, NOTE_TEST_SCORE);

        assertCommandFailure(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_THIRD), model,
                "NID 3 does not exist. Alice Pauline at Maplewood Secondary School (S1) has only 2 notes.");
    }

    @Test
    public void execute_nidLargerThanSingleNote_usesSingularNote() {
        addNotesToFirstPerson(NOTE_ALGEBRA);

        assertCommandFailure(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_SECOND), model,
                "NID 2 does not exist. Alice Pauline at Maplewood Secondary School (S1) has only 1 note.");
    }

    @Test
    public void execute_maxIntNid_throwsCommandException() {
        addNotesToFirstPerson(NOTE_ALGEBRA);
        Index maxIntNid = Index.fromOneBased(Integer.MAX_VALUE);

        assertCommandFailure(new DeleteNoteCommand(INDEX_FIRST_PERSON, maxIntNid), model,
                "NID 2147483647 does not exist. Alice Pauline at Maplewood Secondary School (S1) has only 1 note.");
    }

    @Test
    public void execute_invalidIndexAndInvalidNid_throwsIndexError() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DeleteNoteCommand(outOfBoundsIndex, NID_THIRD), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals_sameAndDifferentValues_returnsExpectedResult() {
        DeleteNoteCommand deleteNoteCommand = new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_FIRST);

        // same object -> returns true
        assertTrue(deleteNoteCommand.equals(deleteNoteCommand));

        // same values -> returns true
        assertTrue(deleteNoteCommand.equals(
                new DeleteNoteCommand(Index.fromOneBased(1), Index.fromOneBased(1))));

        // different index -> returns false
        assertFalse(deleteNoteCommand.equals(new DeleteNoteCommand(INDEX_SECOND_PERSON, NID_FIRST)));

        // different NID -> returns false
        assertFalse(deleteNoteCommand.equals(new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_SECOND)));

        // null -> returns false
        assertFalse(deleteNoteCommand.equals(null));

        // different type -> returns false
        assertFalse(deleteNoteCommand.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        DeleteNoteCommand deleteNoteCommand = new DeleteNoteCommand(INDEX_FIRST_PERSON, NID_SECOND);
        String expected = DeleteNoteCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", targetNid=" + NID_SECOND + ", isForced=false}";

        assertEquals(expected, deleteNoteCommand.toString());
    }

    /**
     * Asserts that deleting note {@code nid} of the first person in {@code model} succeeds, reports
     * {@code deletedNote}, and leaves the person with {@code remainingNotes} in order.
     */
    private void assertDeleteNoteSuccess(Index nid, String deletedNote, String... remainingNotes) {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(person).withNotes(remainingNotes).build();
        String expectedMessage = String.format(DeleteNoteCommand.MESSAGE_SUCCESS, nid.getOneBased(),
                person.getName(), person.getSchool(), person.getLevel(), deletedNote);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, editedPerson);

        assertCommandSuccess(new DeleteNoteCommand(INDEX_FIRST_PERSON, nid), model,
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
