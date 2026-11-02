package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddNoteCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Note;

/**
 * Tests that {@link CommandBox} keeps the words of pasted multi-line text apart and ignores blank commands.
 */
@ExtendWith(ApplicationExtension.class)
public class CommandBoxTest {

    private static final String ADD_NOTE_PREFIX = "addnote 1 note/";
    private static final String EXPECTED_NOTE_COMMAND = ADD_NOTE_PREFIX + "Weak in algebra";
    private static final String FAILURE_MESSAGE = "Unknown command.";

    private CommandBox commandBox;
    private String executedCommandText;
    private boolean isExecutionFailing;

    @Start
    public void start(Stage stage) {
        commandBox = new CommandBox(this::recordCommand);
        stage.setScene(new Scene(commandBox.getRoot()));
        stage.show();
    }

    @Test
    public void paste_lineBreak_replacedWithSpace(FxRobot robot) {
        assertPasteResult(robot, "Weak in\nalgebra", EXPECTED_NOTE_COMMAND);
    }

    @Test
    public void paste_windowsLineBreak_replacedWithOneSpace(FxRobot robot) {
        assertPasteResult(robot, "Weak in\r\nalgebra", EXPECTED_NOTE_COMMAND);
    }

    @Test
    public void paste_oldMacLineBreak_replacedWithSpace(FxRobot robot) {
        assertPasteResult(robot, "Weak in\ralgebra", EXPECTED_NOTE_COMMAND);
    }

    @Test
    public void paste_tab_replacedWithSpace(FxRobot robot) {
        assertPasteResult(robot, "Weak in\talgebra", EXPECTED_NOTE_COMMAND);
    }

    @Test
    public void paste_blankLineBetweenParagraphs_replacedWithOneSpace(FxRobot robot) {
        assertPasteResult(robot, "Weak in\n\nalgebra", EXPECTED_NOTE_COMMAND);
        assertPasteResult(robot, "Weak in\n\t\nalgebra", EXPECTED_NOTE_COMMAND);
    }

    @Test
    public void paste_realisticSymbols_unchanged(FxRobot robot) {
        String note = "Rajesh s/o Kumar: WA2 36/50, O'Brien +65 9123-4567.";
        assertPasteResult(robot, note, ADD_NOTE_PREFIX + note);
    }

    @Test
    public void paste_trailingLineBreak_noExtraSpaceInNote(FxRobot robot) throws ParseException {
        robot.interact(() -> {
            pasteAfterPrefix("Weak in algebra\n");
            getTextField().fireEvent(new ActionEvent());
        });
        AddNoteCommand expectedCommand = new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Weak in algebra"));
        assertEquals(expectedCommand, new AddressBookParser().parseCommand(executedCommandText));
    }

    @Test
    public void handleCommandEntered_pastedMultiLineNote_executesWithSpaces(FxRobot robot) {
        robot.interact(() -> {
            pasteAfterPrefix("Weak in\nalgebra");
            getTextField().fireEvent(new ActionEvent());
            assertEquals(EXPECTED_NOTE_COMMAND, executedCommandText);
            assertEquals("", getTextField().getText());
        });
    }

    @Test
    public void typeAndDelete_noLineBreaks_editsNormally(FxRobot robot) {
        robot.interact(() -> {
            TextField textField = getTextField();
            textField.setText("lis");
            textField.end();
            textField.fireEvent(new KeyEvent(KeyEvent.KEY_TYPED, "t", "t", KeyCode.UNDEFINED,
                    false, false, false, false));
            assertEquals("list", textField.getText());

            textField.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.BACK_SPACE,
                    false, false, false, false));
            assertEquals("lis", textField.getText());
        });
    }

    @Test
    public void handleCommandEntered_failingPastedCommand_keepsTextWithSpaces(FxRobot robot) {
        isExecutionFailing = true;
        robot.interact(() -> {
            pasteAfterPrefix("Weak in\nalgebra");
            TextField textField = getTextField();
            textField.fireEvent(new ActionEvent());
            assertEquals(EXPECTED_NOTE_COMMAND, textField.getText());
            assertTrue(textField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void handleCommandEntered_emptyText_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, "");
    }

    @Test
    public void handleCommandEntered_singleSpace_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, " ");
    }

    @Test
    public void handleCommandEntered_multipleSpaces_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, "     ");
    }

    @Test
    public void handleCommandEntered_fullWidthSpace_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, "\u3000");
    }

    @Test
    public void handleCommandEntered_nonBreakingSpace_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, "\u00A0");
    }

    @Test
    public void handleCommandEntered_zeroWidthSpace_notExecuted(FxRobot robot) {
        assertCommandIgnored(robot, "\u200B");
    }

    @Test
    public void handleCommandEntered_commandWithSurroundingSpaces_executed(FxRobot robot) {
        robot.interact(() -> {
            TextField textField = getTextField();
            textField.setText("  list  ");
            textField.fireEvent(new ActionEvent());
            assertEquals("  list  ", executedCommandText);
            assertEquals("", textField.getText());
        });
    }

    private CommandResult recordCommand(String commandText) throws ParseException {
        executedCommandText = commandText;
        if (isExecutionFailing) {
            throw new ParseException(FAILURE_MESSAGE);
        }
        return new CommandResult(commandText);
    }

    private TextField getTextField() {
        return (TextField) commandBox.getRoot().lookup("#commandTextField");
    }

    /**
     * Types the {@code addnote} prefix, then inserts {@code pastedText} the way Ctrl+V or Cmd+V does.
     */
    private void pasteAfterPrefix(String pastedText) {
        TextField textField = getTextField();
        textField.setText(ADD_NOTE_PREFIX);
        textField.end();
        // TextField#paste inserts the clipboard text through replaceSelection
        textField.replaceSelection(pastedText);
    }

    /**
     * Enters {@code commandText} and asserts that it is not executed, is cleared from the box,
     * and shows no error.
     */
    private void assertCommandIgnored(FxRobot robot, String commandText) {
        // Executing the text would fail and show the error style, so the style check catches it.
        isExecutionFailing = true;
        robot.interact(() -> {
            TextField textField = getTextField();
            textField.setText(commandText);
            textField.fireEvent(new ActionEvent());
            assertNull(executedCommandText);
            assertEquals("", textField.getText());
            assertFalse(textField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private void assertPasteResult(FxRobot robot, String pastedText, String expectedCommandText) {
        robot.interact(() -> {
            pasteAfterPrefix(pastedText);
            assertEquals(expectedCommandText, getTextField().getText());
        });
    }
}
