package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SCHOOL_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final CommandResult CLEAR_CONFIRMATION_RESULT = confirmationPrompt(
            ClearCommand.MESSAGE_CONFIRMATION, ClearCommand.MESSAGE_CONFIRMATION_INSTRUCTION);
    private static final String CLEAR_CANCELLED = String.format(
            ConfirmationManager.MESSAGE_CANCELLED_FORMAT, ClearCommand.COMMAND_WORD);

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, DeleteCommand.MESSAGE_INVALID_STUDENT_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addSameNameDifferentSchool_promptsThenAddsOnConfirmation() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult prompt = logic.execute("add n/alice pauline sch/Victoria School lvl/S2 "
                + "sub/Mathematics band/G1");

        assertEquals(confirmationPrompt(String.format(AddCommand.MESSAGE_CONFIRMATION, "alice pauline",
                "Maplewood Secondary School (S1)"), AddCommand.MESSAGE_CONFIRMATION_INSTRUCTION), prompt);
        assertEquals(expectedModel, model);

        logic.execute("CONFIRM");

        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals("Victoria School", model.getAddressBook().getPersonList().get(1).getSchool().toString());
        assertEquals("S2", model.getAddressBook().getPersonList().get(1).getLevel().toString());
    }

    @Test
    public void execute_addSameNameSchoolAndLevel_rejectsWithoutChangingModel() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String initialStoredData = Files.readString(addressBookFile);

        assertCommandException("add n/alice  pauline sch/maplewood  secondary school lvl/S1 "
                + "sub/Mathematics band/G1", String.format(AddCommand.MESSAGE_DUPLICATE_PERSON,
                        "alice pauline", "maplewood secondary school", "S1"));

        assertEquals(expectedModel, model);
        assertEquals(initialStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_editToSameNameDifferentSchool_promptsThenEditsOnConfirmation() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BOB);

        CommandResult prompt = logic.execute("edit 2 n/Alice Pauline");

        assertEquals(confirmationPrompt(String.format(EditCommand.MESSAGE_CONFIRMATION, "Alice Pauline",
                "Maplewood Secondary School (S1)"), EditCommand.MESSAGE_CONFIRMATION_INSTRUCTION), prompt);
        logic.execute("CONFIRM");

        assertEquals("Alice Pauline", model.getAddressBook().getPersonList().get(1).getName().toString());
        assertEquals("Victoria School", model.getAddressBook().getPersonList().get(1).getSchool().toString());
    }

    @Test
    public void execute_editToSameNameSchoolAndLevel_rejectsWithoutChangingModel() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BOB);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String initialStoredData = Files.readString(addressBookFile);

        assertCommandException("edit 2 n/Alice Pauline sch/Maplewood Secondary School lvl/S1",
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, "Alice Pauline",
                        "Maplewood Secondary School", "S1"));

        assertEquals(expectedModel, model);
        assertEquals(initialStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_editNonIdentityFieldForExistingNameMatch_doesNotPrompt() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(new PersonBuilder(TypicalPersons.ALICE).withSchool("Victoria School").withLevel("S2")
                .build());

        CommandResult result = logic.execute("edit 2 p/81234567");

        assertFalse(result.isWarning());
        assertEquals("81234567", model.getAddressBook().getPersonList().get(1).getPhone().orElseThrow().toString());
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_deleteSameNameStudent_confirmationNamesSchoolAndLevel() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(new PersonBuilder(TypicalPersons.ALICE).withSchool("Victoria School").withLevel("S2")
                .build());

        CommandResult prompt = logic.execute("delete 2");

        assertEquals(confirmationPrompt(String.format(DeleteCommand.MESSAGE_CONFIRMATION, "Alice Pauline",
                "Victoria School", "S2"), DeleteCommand.MESSAGE_CONFIRMATION_INSTRUCTION).getFeedbackToUser(),
                prompt.getFeedbackToUser());
        assertTrue(prompt.isWarning());
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_deleteNoteFromSameNameStudent_confirmationNamesSchoolAndLevel() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        Person sameNameOtherSchool = new PersonBuilder(TypicalPersons.ALICE).withSchool("Victoria School")
                .withLevel("S2").withNotes("Review algebra").build();
        model.addPerson(sameNameOtherSchool);

        CommandResult prompt = logic.execute("deletenote 2 nid/1");

        assertEquals(confirmationPrompt(String.format(DeleteNoteCommand.MESSAGE_CONFIRMATION, 1, "Alice Pauline",
                "Victoria School", "S2"), DeleteNoteCommand.MESSAGE_CONFIRMATION_INSTRUCTION).getFeedbackToUser(),
                prompt.getFeedbackToUser());
        assertTrue(prompt.isWarning());
        assertEquals(List.of("Review algebra"), model.getAddressBook().getPersonList().get(1).getNotes().stream()
                .map(note -> note.toString()).toList());
    }

    @Test
    public void execute_addNoteToSameNameStudent_preservesBothStudents() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(new PersonBuilder(TypicalPersons.ALICE).withSchool("Victoria School").withLevel("S2")
                .build());

        logic.execute("addnote 2 note/Review algebra");

        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(List.of("Review algebra"), model.getAddressBook().getPersonList().get(1).getNotes().stream()
                .map(note -> note.toString()).toList());
    }

    @Test
    public void execute_addAndEditNameCollisionCancelled_leavesModelAndStorageUnchanged() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BOB);
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String initialStoredData = Files.readString(addressBookFile);

        logic.execute("add n/Alice Pauline sch/Victoria School lvl/S2 sub/Mathematics band/G1");
        CommandResult addCancellation = logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, AddCommand.COMMAND_WORD),
                addCancellation.getCancellationNotice().orElseThrow());
        assertEquals(initialStoredData, Files.readString(addressBookFile));

        logic.execute("edit 2 n/Alice Pauline");
        CommandResult editCancellation = logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, EditCommand.COMMAND_WORD),
                editCancellation.getCancellationNotice().orElseThrow());
        assertEquals(initialStoredData, Files.readString(addressBookFile));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals("Bob Choo", model.getAddressBook().getPersonList().get(1).getName().toString());
    }

    @Test
    public void execute_listWithArguments_throwsParseException() {
        String invalidListCommand = ListCommand.COMMAND_WORD + " 3";
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertParseException(invalidListCommand, expectedMessage);
    }

    @Test
    public void execute_firstClear_showsConfirmationWithoutChangingModelOrStorage() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String expectedStoredData = Files.readString(addressBookFile);

        assertCommandSuccess(ClearCommand.COMMAND_WORD, CLEAR_CONFIRMATION_RESULT, expectedModel);
        assertEquals(expectedStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_clearConfirmed_clearsAddressBook() throws Exception {
        model.addPerson(AMY);
        logic.execute(ClearCommand.COMMAND_WORD);

        assertCommandSuccess(" CONFIRM ",
                CommandResult.showList(ClearCommand.MESSAGE_SUCCESS), new ModelManager());
    }

    @Test
    public void execute_forcedClear_clearsAddressBookWithoutConfirmation() throws Exception {
        model.addPerson(AMY);

        assertCommandSuccess(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION,
                CommandResult.showList(ClearCommand.MESSAGE_SUCCESS), new ModelManager());
    }

    @Test
    public void execute_clearThenForcedClear_clearsWithoutCancellationNotice() throws Exception {
        model.addPerson(AMY);
        logic.execute(ClearCommand.COMMAND_WORD);

        CommandResult result = logic.execute(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION);
        assertEquals(CommandResult.showList(ClearCommand.MESSAGE_SUCCESS), result);
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals(new ModelManager(), model);
    }

    @Test
    public void execute_forcedClearWithEmptyFilteredList_clearsAddressBook() throws Exception {
        model.addPerson(AMY);
        model.updateFilteredPersonList(unused -> false);

        assertCommandSuccess(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION,
                CommandResult.showList(ClearCommand.MESSAGE_SUCCESS), new ModelManager());
    }

    @Test
    public void execute_nonConfirmInput_runsAsCommandAndCancelsPending() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        logic.execute(ClearCommand.COMMAND_WORD);
        CommandResult result = logic.execute("list");
        assertEquals(ListCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CLEAR_CANCELLED, result.getCancellationNotice().orElseThrow());
        assertEquals(CommandResult.DisplayMode.SHOW_LIST, result.getDisplayMode());
        assertEquals(expectedModel, model);

        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_confirmWithNothingPending_throwsParseException() {
        model.addPerson(AMY);
        for (String input : new String[] {"CONFIRM", "  CONFIRM  "}) {
            assertParseException(input, ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
        }
    }

    @Test
    public void execute_confirmVariantWithNothingPending_throwsUnknownCommand() {
        for (String input : new String[] {"confirm", "CONFIRM extra"}) {
            assertParseException(input, MESSAGE_UNKNOWN_COMMAND);
        }
    }

    @Test
    public void execute_confirmationRequiresExactUppercaseWord_otherInputsRunAsCommands() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        for (String input : new String[] {"confirm", "CONFIRM extra", "y"}) {
            logic.execute(ClearCommand.COMMAND_WORD);
            assertParseException(input, CLEAR_CANCELLED + System.lineSeparator() + MESSAGE_UNKNOWN_COMMAND);
            assertEquals(expectedModel, model);
        }
    }

    @Test
    public void execute_newDestructiveCommand_replacesPendingConfirmation() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder(AMY).withName("Other Student").build());
        Person personAtFirstIndex = model.getFilteredPersonList().get(0);
        logic.execute(ClearCommand.COMMAND_WORD);

        CommandResult replacementPrompt = logic.execute("delete 1");
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, ClearCommand.COMMAND_WORD),
                replacementPrompt.getCancellationNotice().orElseThrow());
        assertTrue(replacementPrompt.isWarning());
        assertTrue(replacementPrompt.getFeedbackToUser().startsWith("This will delete "
                + personAtFirstIndex.getName()));

        logic.execute(" CONFIRM ");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertFalse(model.getAddressBook().getPersonList().contains(personAtFirstIndex));
    }

    @Test
    public void execute_repeatedClear_promptsAgainWithoutCancellationNotice() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        logic.execute(ClearCommand.COMMAND_WORD);

        CommandResult result = logic.execute(ClearCommand.COMMAND_WORD);
        assertEquals(CLEAR_CONFIRMATION_RESULT, result);
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals(expectedModel, model);

        logic.execute("CONFIRM");
        assertEquals(new ModelManager(), model);
    }

    @Test
    public void execute_clearWithArgumentsWhilePending_cancelsPendingAndThrowsParseException() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        logic.execute(ClearCommand.COMMAND_WORD);

        assertParseException(ClearCommand.COMMAND_WORD + " extra", CLEAR_CANCELLED + System.lineSeparator()
                + String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
        assertEquals(expectedModel, model);
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_deleteDifferentIndexWhilePending_replacesPendingWithNotice() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder(AMY).withName("Other Student").build());
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person secondPerson = model.getFilteredPersonList().get(1);
        logic.execute("delete 1");

        CommandResult result = logic.execute("delete 2");
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, DeleteCommand.COMMAND_WORD),
                result.getCancellationNotice().orElseThrow());
        assertTrue(result.isWarning());

        logic.execute("CONFIRM");
        assertEquals(List.of(firstPerson), model.getAddressBook().getPersonList());
        assertFalse(model.getAddressBook().getPersonList().contains(secondPerson));
    }

    @Test
    public void execute_forcedDeleteWhileClearPending_deletesWithNotice() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder(AMY).withName("Other Student").build());
        Person secondPerson = model.getFilteredPersonList().get(1);
        logic.execute(ClearCommand.COMMAND_WORD);

        CommandResult result = logic.execute("delete -f 1");
        assertEquals(CLEAR_CANCELLED, result.getCancellationNotice().orElseThrow());
        assertEquals(List.of(secondPerson), model.getAddressBook().getPersonList());
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_forcedDeleteSameIndexWhilePending_deletesWithoutNotice() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder(AMY).withName("Other Student").build());
        Person secondPerson = model.getFilteredPersonList().get(1);
        logic.execute("delete 1");

        CommandResult result = logic.execute("delete -f 1");
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals(List.of(secondPerson), model.getAddressBook().getPersonList());
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_forcedDeleteDifferentIndexWhilePending_deletesWithNotice() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder(AMY).withName("Other Student").build());
        Person firstPerson = model.getFilteredPersonList().get(0);
        logic.execute("delete 1");

        CommandResult result = logic.execute("delete -f 2");
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, DeleteCommand.COMMAND_WORD),
                result.getCancellationNotice().orElseThrow());
        assertEquals(List.of(firstPerson), model.getAddressBook().getPersonList());
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_forcedDeleteNoteSameNoteWhilePending_deletesWithoutNotice() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withNotes("Review algebra", "Practise geometry").build());
        logic.execute("deletenote 1 nid/1");

        CommandResult result = logic.execute("deletenote -f 1 nid/1");
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals(new PersonBuilder(AMY).withNotes("Practise geometry").build(),
                model.getAddressBook().getPersonList().get(0));
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_forcedDeleteNoteDifferentNoteWhilePending_deletesWithNotice() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withNotes("Review algebra", "Practise geometry").build());
        logic.execute("deletenote 1 nid/1");

        CommandResult result = logic.execute("deletenote -f 1 nid/2");
        assertEquals(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, DeleteNoteCommand.COMMAND_WORD),
                result.getCancellationNotice().orElseThrow());
        assertEquals(new PersonBuilder(AMY).withNotes("Review algebra").build(),
                model.getAddressBook().getPersonList().get(0));
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_repeatedDuplicateAdd_promptsAgainAndAddsOnce() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        String addInput = "add n/Alice Pauline sch/Victoria School lvl/S2 sub/Mathematics band/G1";
        logic.execute(addInput);

        CommandResult result = logic.execute(addInput);
        assertTrue(result.isWarning());
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals(1, model.getAddressBook().getPersonList().size());

        logic.execute("CONFIRM");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_repeatedDuplicateEdit_promptsAgainAndEditsOnce() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BOB);
        logic.execute("edit 2 n/Alice Pauline");

        CommandResult result = logic.execute("edit 2 n/Alice Pauline");
        assertTrue(result.isWarning());
        assertTrue(result.getCancellationNotice().isEmpty());
        assertEquals("Bob Choo", model.getAddressBook().getPersonList().get(1).getName().toString());

        logic.execute("CONFIRM");
        assertEquals("Alice Pauline", model.getAddressBook().getPersonList().get(1).getName().toString());
        assertParseException("CONFIRM", ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
    }

    @Test
    public void execute_forcedAddWhileAddPending_cancelsPendingAndThrowsParseException() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        String forcedAddInput = "add -f n/Alice Pauline sch/Victoria School lvl/S2 sub/Mathematics band/G1";
        ParseException parseError = Assertions.assertThrows(ParseException.class, () ->
                new AddressBookParser().parseCommand(forcedAddInput));
        logic.execute("add n/Alice Pauline sch/Victoria School lvl/S2 sub/Mathematics band/G1");

        assertParseException(forcedAddInput, String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT,
                AddCommand.COMMAND_WORD) + System.lineSeparator() + parseError.getMessage());
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_noConfirmation_keepsAddressBookUnchanged() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String expectedStoredData = Files.readString(addressBookFile);
        logic.execute(ClearCommand.COMMAND_WORD);
        assertParseException("n", CLEAR_CANCELLED + System.lineSeparator() + MESSAGE_UNKNOWN_COMMAND);
        assertEquals(expectedStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_deleteConfirmed_deletesSelectedStudent() throws Exception {
        model.addPerson(AMY);
        CommandResult prompt = logic.execute("delete 1");
        assertTrue(prompt.isWarning());
        assertEquals(confirmationPrompt(String.format(DeleteCommand.MESSAGE_CONFIRMATION, AMY.getName(),
                AMY.getSchool(), AMY.getLevel()), DeleteCommand.MESSAGE_CONFIRMATION_INSTRUCTION).getFeedbackToUser(),
                prompt.getFeedbackToUser());

        Model expectedModel = new ModelManager();
        assertCommandSuccess("CONFIRM", CommandResult.showList(
                String.format(DeleteCommand.MESSAGE_DELETE_STUDENT_SUCCESS, AMY.getName(), AMY.getSchool(),
                        AMY.getLevel())), expectedModel);
        model.addPerson(AMY);
        assertCommandSuccess("delete -f 1", CommandResult.showList(String.format(
                DeleteCommand.MESSAGE_DELETE_STUDENT_SUCCESS, AMY.getName(), AMY.getSchool(), AMY.getLevel())),
                new ModelManager());
    }

    @Test
    public void execute_deleteAndDeleteNoteCancelled_leaveDataAndStorageUnchanged() throws Exception {
        Person personWithNote = new PersonBuilder(AMY).withNotes("Review algebra").build();
        model.addPerson(personWithNote);
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String initialStoredData = Files.readString(addressBookFile);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        logic.execute("delete 1");
        assertParseException("n", String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT,
                DeleteCommand.COMMAND_WORD) + System.lineSeparator() + MESSAGE_UNKNOWN_COMMAND);
        assertEquals(initialStoredData, Files.readString(addressBookFile));

        logic.execute("deletenote 1 nid/1");
        assertParseException("n", String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT,
                DeleteNoteCommand.COMMAND_WORD) + System.lineSeparator() + MESSAGE_UNKNOWN_COMMAND);
        assertEquals(initialStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_deleteNoteConfirmed_deletesSelectedNote() throws Exception {
        Person personWithNote = new PersonBuilder(AMY).withNotes("Review algebra").build();
        model.addPerson(personWithNote);
        CommandResult prompt = logic.execute("deletenote 1 nid/1");
        assertTrue(prompt.isWarning());
        assertEquals(confirmationPrompt(String.format(DeleteNoteCommand.MESSAGE_CONFIRMATION, 1, AMY.getName(),
                AMY.getSchool(), AMY.getLevel()), DeleteNoteCommand.MESSAGE_CONFIRMATION_INSTRUCTION)
                .getFeedbackToUser(),
                prompt.getFeedbackToUser());

        Model expectedModel = new ModelManager();
        Person editedPerson = new PersonBuilder(personWithNote).withNotes().build();
        expectedModel.addPerson(editedPerson);
        assertCommandSuccess("CONFIRM", CommandResult.showStudent(
                String.format(DeleteNoteCommand.MESSAGE_SUCCESS,
                        1, AMY.getName(), AMY.getSchool(), AMY.getLevel(),
                        personWithNote.getNotes().getFirst()),
                editedPerson, INDEX_FIRST_PERSON), expectedModel);
        logic.execute("addnote 1 note/Review geometry");
        CommandResult forcedResult = logic.execute("deletenote -f 1 nid/1");
        assertFalse(forcedResult.isWarning());
        assertEquals(String.format(DeleteNoteCommand.MESSAGE_SUCCESS, 1, AMY.getName(), AMY.getSchool(),
                AMY.getLevel(), "Review geometry"),
                forcedResult.getFeedbackToUser());
    }

    @Test
    public void execute_clearOnEmptyAddressBook_showsEmptyMessageWithoutPendingConfirmation() throws Exception {
        assertCommandSuccess(ClearCommand.COMMAND_WORD,
                new CommandResult(ClearCommand.MESSAGE_EMPTY), model);
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(ClearCommand.COMMAND_WORD, CLEAR_CONFIRMATION_RESULT, expectedModel);
    }

    @Test
    public void execute_forcedClearOnEmptyAddressBook_showsEmptyMessage() throws Exception {
        assertCommandSuccess(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION,
                new CommandResult(ClearCommand.MESSAGE_EMPTY), model);
    }

    @Test
    public void execute_clearWithEmptyFilteredList_showsConfirmation() throws Exception {
        model.addPerson(AMY);
        model.updateFilteredPersonList(unused -> false);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(unused -> false);

        assertCommandSuccess(ClearCommand.COMMAND_WORD, CLEAR_CONFIRMATION_RESULT, expectedModel);
    }

    @Test
    public void execute_clearWithArguments_throwsParseException() {
        String invalidClearCommand = ClearCommand.COMMAND_WORD + " 3";
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertParseException(invalidClearCommand, expectedMessage);
    }

    @Test
    public void execute_repeatedForceOption_doesNotChangeModelOrStorage() throws Exception {
        model.addPerson(AMY);
        Path addressBookFile = temporaryFolder.resolve("addressBook.json");
        logic.execute(ListCommand.COMMAND_WORD);
        String expectedStoredData = Files.readString(addressBookFile);
        String invalidClearCommand = String.join(" ", ClearCommand.COMMAND_WORD,
                ClearCommand.FORCE_OPTION, ClearCommand.FORCE_OPTION);
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);

        assertParseException(invalidClearCommand, expectedMessage);
        assertEquals(expectedStoredData, Files.readString(addressBookFile));
    }

    @Test
    public void execute_exitWithArguments_throwsParseException() {
        String invalidExitCommand = ExitCommand.COMMAND_WORD + " 3";
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertParseException(invalidExitCommand, expectedMessage);
    }

    @Test
    public void execute_helpWithArguments_throwsParseException() {
        String invalidHelpCommand = HelpCommand.COMMAND_WORD + " 3";
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        assertParseException(invalidHelpCommand, expectedMessage);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    private static CommandResult confirmationPrompt(String message, String instruction) {
        return CommandResult.warning(message + System.lineSeparator() + instruction + System.lineSeparator()
                + ConfirmationManager.MESSAGE_CONFIRMATION_CANCELLATION);
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command and confirms that the result and model match the expected values.
     */
    private void assertCommandSuccess(String inputCommand, CommandResult expectedCommandResult,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedCommandResult, result);
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + SCHOOL_DESC_AMY
                + LEVEL_DESC_AMY + " sub/Mathematics band/G1";
        Person expectedPerson = new PersonBuilder(AMY).build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
