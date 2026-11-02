package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void withoutForce_forcedCommand_returnsUnforcedCommand() {
        assertEquals(new ClearCommand(),
                new ClearCommand(true).withoutForce());
    }

    @Test
    public void execute_emptyAddressBook_showsAlreadyEmptyMessage() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, new CommandResult(ClearCommand.MESSAGE_EMPTY),
                expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(), model, CommandResult.showList(ClearCommand.MESSAGE_SUCCESS),
                expectedModel);
    }

    @Test
    public void isForced_normalAndForcedCommands_returnsExpectedResult() {
        assertFalse(new ClearCommand().isForced());
        assertTrue(new ClearCommand(true).isForced());
    }

    @Test
    public void messageUsage_clearExamples_listsConfirmedCommandFirst() {
        String expectedExamples = "Examples:\n"
                + "clear (asks for confirmation)\n"
                + "clear -f (clears immediately without confirmation)";

        assertTrue(ClearCommand.MESSAGE_USAGE.endsWith(expectedExamples));
    }

    @Test
    public void equals() {
        ClearCommand normalCommand = new ClearCommand();
        ClearCommand forcedCommand = new ClearCommand(true);

        assertTrue(normalCommand.equals(normalCommand));
        assertTrue(normalCommand.equals(new ClearCommand()));
        assertFalse(normalCommand.equals(forcedCommand));
        assertFalse(normalCommand.equals(null));
        assertFalse(normalCommand.equals(new HelpCommand()));
    }

    @Test
    public void toStringMethod() {
        ClearCommand clearCommand = new ClearCommand(true);
        String expected = ClearCommand.class.getCanonicalName() + "{isForced=true}";

        assertEquals(expected, clearCommand.toString());
    }

}
