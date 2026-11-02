package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.ConfirmationManager;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests student detail behavior in {@link MainWindow}.
 */
@ExtendWith(ApplicationExtension.class)
public class MainWindowStudentDetailTest {

    private static final PseudoClass DISPLAYED_PSEUDO_CLASS = PseudoClass.getPseudoClass("displayed");

    private MainWindow mainWindow;
    private LogicStub logicStub;

    @Start
    public void start(Stage stage) {
        logicStub = new LogicStub();
        mainWindow = new MainWindow(stage, logicStub, Path.of("data", "addressbook.json"));
        mainWindow.fillInnerParts();
        mainWindow.show();
    }

    @Test
    public void startup_noStudentSelected_showsFixedSplitAndEmptyState(FxRobot robot) {
        GridPane contentGridPane = getContentGridPane();

        assertEquals(2, contentGridPane.getColumnConstraints().size());
        assertEquals(50, contentGridPane.getColumnConstraints().get(0).getPercentWidth());
        assertEquals(50, contentGridPane.getColumnConstraints().get(1).getPercentWidth());
        assertEquals(contentGridPane.getHeight(), getPersonListPanelPlaceholder().getHeight(), 1.0);
        assertEquals(contentGridPane.getHeight(), getStudentDetailPanelPlaceholder().getHeight(), 1.0);
        assertEquals("View a student to see details.", getEmptyStateLabel().getText());
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
        assertFalse(getPersonListView().isFocusTraversable());
        assertTrue(contentGridPane.lookupAll(".split-pane-divider").isEmpty());
        assertListCellsUseContentHeight(robot);
    }

    @Test
    public void execute_viewCommand_longRecordKeepsRightPanelFullHeight(FxRobot robot) {
        Node originalList = mainWindow.getPersonListPanel().getRoot();

        enterCommand(robot, "view 1");

        assertEquals("Alice Pauline", ((Label) getStudentDetailNode().lookup("#name")).getText());
        assertTrue(isPersonHighlighted(ALICE.getName().fullName));
        assertFalse(isPersonHighlighted(BENSON.getName().fullName));
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
        assertEquals(2, getContentGridPane().getChildren().size());
        assertSame(originalList, getPersonListPanelPlaceholder().getChildren().get(0));
        robot.interact(() -> {
            mainWindow.getRoot().getScene().getRoot().applyCss();
            mainWindow.getRoot().getScene().getRoot().layout();
        });
        ScrollPane detailScrollPane = (ScrollPane) getStudentDetailNode().lookup("#detailScrollPane");
        VBox studentDetailContent = (VBox) getStudentDetailNode().lookup("#studentDetailContent");
        assertTrue(studentDetailContent.getHeight() > detailScrollPane.getViewportBounds().getHeight());
        assertEquals(getContentGridPane().getHeight(), getPersonListPanelPlaceholder().getHeight(), 0.1);
        assertEquals(getContentGridPane().getHeight(), getStudentDetailPanelPlaceholder().getHeight(), 0.1);

        Label secondPersonName = mainWindow.getPersonListPanel().getRoot().lookupAll("#name").stream()
                .map(Label.class::cast)
                .filter(label -> label.getText().equals(BENSON.getName().fullName))
                .findFirst()
                .orElseThrow();
        robot.interact(() -> secondPersonName.fireEvent(createPrimaryMousePress()));

        assertTrue(isPersonHighlighted(ALICE.getName().fullName));
        assertFalse(isPersonHighlighted(BENSON.getName().fullName));
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
        assertEquals("Alice Pauline", ((Label) getStudentDetailNode().lookup("#name")).getText());
    }

    @Test
    public void execute_repeatedViewCommand_replacesExistingDetails(FxRobot robot) {
        Node originalList = mainWindow.getPersonListPanel().getRoot();
        enterCommand(robot, "view 1");
        enterCommand(robot, "view 2");

        assertEquals("Benson Meier", ((Label) getStudentDetailNode().lookup("#name")).getText());
        assertFalse(isPersonHighlighted(ALICE.getName().fullName));
        assertTrue(isPersonHighlighted(BENSON.getName().fullName));
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
        assertEquals(2, getContentGridPane().getChildren().size());
        assertSame(originalList, getPersonListPanelPlaceholder().getChildren().get(0));
    }

    @Test
    public void execute_listCommand_restoresOriginalListPanel(FxRobot robot) {
        PersonListPanel personListPanel = mainWindow.getPersonListPanel();
        enterCommand(robot, "view 1");

        enterCommand(robot, "list");

        assertEquals(2, getContentGridPane().getChildren().size());
        assertSame(personListPanel.getRoot(), getPersonListPanelPlaceholder().getChildren().get(0));
        assertEquals("View a student to see details.", getEmptyStateLabel().getText());
        assertFalse(isPersonHighlighted(ALICE.getName().fullName));
        assertFalse(isPersonHighlighted(BENSON.getName().fullName));
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
    }

    @Test
    public void execute_failedCommand_keepsStudentDetails(FxRobot robot) {
        enterCommand(robot, "view 1");
        Node originalDetails = getStudentDetailNode();

        enterCommand(robot, "fail");

        assertSame(originalDetails, getStudentDetailNode());
    }

    @Test
    public void execute_commandWithoutDisplayInstruction_keepsStudentDetails(FxRobot robot) {
        enterCommand(robot, "view 1");
        Node originalDetails = getStudentDetailNode();

        enterCommand(robot, "keep");

        assertSame(originalDetails, getStudentDetailNode());
    }

    @Test
    public void execute_commandUpdatesViewedStudent_refreshesStudentDetails(FxRobot robot) {
        enterCommand(robot, "view 1");

        enterCommand(robot, "addnote 1 note/New note");

        List<String> noteTexts = mainWindow.getRoot().getScene().getRoot()
                .lookupAll(".student-detail-note-card").stream()
                .map(Label.class::cast)
                .map(Label::getText)
                .toList();
        assertEquals(List.of("1. New note"), noteTexts);
        assertTrue(isPersonHighlighted(ALICE.getName().fullName));
    }

    @Test
    public void execute_editCommand_updatesListCardAndShowsEditedStudent(FxRobot robot) {
        enterCommand(robot, "edit 1");

        List<String> listNames = mainWindow.getPersonListPanel().getRoot().lookupAll("#name").stream()
                .map(Label.class::cast)
                .map(Label::getText)
                .toList();
        assertTrue(listNames.contains("Alice Edited"));
        assertEquals("Alice Edited", ((Label) getStudentDetailNode().lookup("#name")).getText());
        assertTrue(isPersonHighlighted("Alice Edited"));
        assertEquals(-1, getPersonListView().getSelectionModel().getSelectedIndex());
    }

    @Test
    public void startup_manyStudents_scrollsToLastStudent(FxRobot robot) {
        robot.interact(() -> {
            mainWindow.getRoot().setWidth(600);
            mainWindow.getRoot().setHeight(400);
            logicStub.addStudents(1000);
            int lastIndex = getPersonListView().getItems().size() - 1;
            getPersonListView().scrollTo(lastIndex);
            mainWindow.getRoot().getScene().getRoot().applyCss();
            mainWindow.getRoot().getScene().getRoot().layout();
            getPersonListView().applyCss();
            getPersonListView().layout();

            assertEquals(1002, getPersonListView().getItems().size());
            assertTrue(getPersonListView().lookupAll(".list-cell").stream()
                    .map(ListCell.class::cast)
                    .anyMatch(cell -> !cell.isEmpty() && cell.getIndex() == lastIndex));
        });
    }

    @Test
    public void execute_addCommand_addsCardAndShowsStudent(FxRobot robot) {
        enterCommand(robot, "add student");

        assertEquals(List.of("Alice Pauline", "Benson Meier", "New Student"), getListNames());
        assertEquals("New Student", ((Label) getStudentDetailNode().lookup("#name")).getText());
        assertTrue(isPersonHighlighted("New Student"));
    }

    @Test
    public void execute_deleteCommand_removesCardUpdatesIndicesAndClearsDetails(FxRobot robot) {
        enterCommand(robot, "view 1");
        enterCommand(robot, "delete 1");

        assertEquals(List.of("Benson Meier"), getListNames());
        assertEquals("1. ", ((Label) getPersonListPanelPlaceholder().lookup("#id")).getText());
        assertEquals("View a student to see details.", getEmptyStateLabel().getText());
    }

    @Test
    public void execute_filterCommand_updatesVisibleCardsAndClearsDetails(FxRobot robot) {
        enterCommand(robot, "view 1");
        enterCommand(robot, "filter Benson");

        assertEquals(List.of("Benson Meier"), getListNames());
        assertEquals("View a student to see details.", getEmptyStateLabel().getText());
    }

    @Test
    public void execute_clearCommand_removesAllCardsAndClearsDetails(FxRobot robot) {
        enterCommand(robot, "view 1");
        enterCommand(robot, "clear");

        assertTrue(getPersonListView().getItems().isEmpty());
        assertEquals("View a student to see details.", getEmptyStateLabel().getText());
    }

    @Test
    public void execute_editNoteCommand_refreshesDisplayedRecord(FxRobot robot) {
        enterCommand(robot, "editnote 1");

        assertNoteTexts("1. Updated note");
        assertTrue(isPersonHighlighted(ALICE.getName().fullName));
    }

    @Test
    public void execute_deleteNoteCommand_refreshesDisplayedRecord(FxRobot robot) {
        enterCommand(robot, "deletenote 1");

        assertEquals("No notes recorded.", ((Label) getStudentDetailNode().lookup("#emptyNotes")).getText());
        assertTrue(isPersonHighlighted(ALICE.getName().fullName));
    }

    @Test
    public void execute_warningResult_appliesWarningStyle(FxRobot robot) {
        enterCommand(robot, "warn");

        assertTrue(getResultDisplay().getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
    }

    @Test
    public void execute_standardResultAfterWarning_removesWarningStyle(FxRobot robot) {
        enterCommand(robot, "warn");
        enterCommand(robot, "keep");

        assertFalse(getResultDisplay().getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
    }

    @Test
    public void execute_cancelledCommand_showsRedNoticeAndKeepsResultColor(FxRobot robot) {
        enterCommand(robot, "cancelled");
        Label cancellationDisplay = (Label) mainWindow.getRoot().getScene().lookup("#cancellationDisplay");
        TextArea resultDisplay = getResultDisplay();
        robot.interact(() -> mainWindow.getRoot().getScene().getRoot().applyCss());

        assertEquals("The earlier 'clear' command has been cancelled.", cancellationDisplay.getText());
        assertTrue(cancellationDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
        assertEquals("Listed students", resultDisplay.getText());
        assertFalse(resultDisplay.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
        assertFalse(resultDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));

        enterCommand(robot, "replacement");
        assertTrue(cancellationDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
        assertTrue(resultDisplay.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));

        enterCommand(robot, "cancelled-fail");
        assertTrue(cancellationDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
        assertTrue(resultDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
    }

    @Test
    public void warningAndErrorStyles_defaultStyles_haveDifferentTextColors(FxRobot robot) {
        Label warningLabel = new Label("Warning");
        warningLabel.getStyleClass().add(ResultDisplay.WARNING_STYLE_CLASS);
        Label errorLabel = new Label("Error");
        errorLabel.getStyleClass().add(CommandBox.ERROR_STYLE_CLASS);

        robot.interact(() -> {
            getPersonListPanelPlaceholder().getChildren().setAll(warningLabel, errorLabel);
            mainWindow.getRoot().getScene().getRoot().applyCss();
        });

        assertNotEquals(errorLabel.getTextFill(), warningLabel.getTextFill());
    }

    private void enterCommand(FxRobot robot, String command) {
        robot.interact(() -> {
            TextField commandTextField =
                    (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
            commandTextField.setText(command);
            commandTextField.fireEvent(new ActionEvent());
        });
    }

    private MouseEvent createPrimaryMousePress() {
        return new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, false, null);
    }

    private boolean isPersonHighlighted(String name) {
        Label nameLabel = mainWindow.getPersonListPanel().getRoot().lookupAll("#name").stream()
                .map(Label.class::cast)
                .filter(label -> label.getText().equals(name))
                .findFirst()
                .orElseThrow();
        Node currentNode = nameLabel;
        while (!(currentNode instanceof ListCell<?>)) {
            currentNode = currentNode.getParent();
        }
        return currentNode.getPseudoClassStates().contains(DISPLAYED_PSEUDO_CLASS);
    }

    private void assertListCellsUseContentHeight(FxRobot robot) {
        robot.interact(() -> {
            mainWindow.getRoot().getScene().getRoot().applyCss();
            mainWindow.getRoot().getScene().getRoot().layout();
        });
        getPersonListView().lookupAll(".list-cell").stream()
                .map(ListCell.class::cast)
                .filter(cell -> !cell.isEmpty())
                .forEach(cell -> {
                    Region card = (Region) cell.getGraphic();
                    VBox cardContents = (VBox) card.lookup("#phone").getParent();
                    assertEquals(Region.USE_PREF_SIZE, cell.getMaxHeight());
                    assertEquals(cell.prefHeight(cell.getWidth()), cell.getHeight(), 0.1);
                    assertEquals(card.getHeight(), cardContents.getHeight(), 0.1);
                });
    }

    private GridPane getContentGridPane() {
        return (GridPane) mainWindow.getRoot().getScene().lookup("#contentGridPane");
    }

    private StackPane getPersonListPanelPlaceholder() {
        return (StackPane) mainWindow.getRoot().getScene().lookup("#personListPanelPlaceholder");
    }

    @SuppressWarnings("unchecked")
    private ListView<Person> getPersonListView() {
        return (ListView<Person>) mainWindow.getRoot().getScene().lookup("#personListView");
    }

    private Node getStudentDetailNode() {
        return getStudentDetailPanelPlaceholder().getChildren().get(0);
    }

    private StackPane getStudentDetailPanelPlaceholder() {
        return (StackPane) mainWindow.getRoot().getScene().lookup("#studentDetailPanelPlaceholder");
    }

    private Label getEmptyStateLabel() {
        return (Label) getStudentDetailNode();
    }

    private TextArea getResultDisplay() {
        return (TextArea) mainWindow.getRoot().getScene().lookup(".result-display");
    }

    private List<String> getListNames() {
        return getPersonListView().getItems().stream()
                .map(person -> person.getName().fullName)
                .toList();
    }

    private void assertNoteTexts(String... expectedTexts) {
        List<String> noteTexts = mainWindow.getRoot().getScene().getRoot()
                .lookupAll(".student-detail-note-card").stream()
                .map(Label.class::cast)
                .map(Label::getText)
                .toList();
        assertEquals(List.of(expectedTexts), noteTexts);
    }

    private static class LogicStub implements Logic {

        private static final Person ALICE_WITH_MANY_NOTES = new PersonBuilder(ALICE)
                .withNotes("Note 1", "Note 2", "Note 3", "Note 4", "Note 5", "Note 6",
                        "Note 7", "Note 8", "Note 9", "Note 10", "Note 11", "Note 12")
                .build();

        private final ObservableList<Person> persons =
                FXCollections.observableArrayList(ALICE_WITH_MANY_NOTES, BENSON);

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            return switch (commandText) {
                case "view 1" -> CommandResult.showStudent(
                        "Viewing Alice", ALICE_WITH_MANY_NOTES, INDEX_FIRST_PERSON);
                case "view 2" -> CommandResult.showStudent("Viewing Benson", BENSON, INDEX_SECOND_PERSON);
                case "list" -> CommandResult.showList("Listed all students");
                case "addnote 1 note/New note" -> addNoteToFirstPerson();
                case "edit 1" -> editFirstPerson();
                case "add student" -> addStudent();
                case "delete 1" -> deleteFirstPerson();
                case "filter Benson" -> filterToBenson();
                case "clear" -> clearPersons();
                case "editnote 1" -> replaceFirstPersonNotes("Updated note");
                case "deletenote 1" -> replaceFirstPersonNotes();
                case "keep" -> new CommandResult("No display change");
                case "warn" -> CommandResult.warning("Warning");
                case "cancelled" -> new CommandResult("Listed students").withCancellationNotice(
                        String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT, "clear"));
                case "replacement" -> CommandResult.warning("Delete student?"
                        + ConfirmationManager.MESSAGE_CONFIRMATION_SUFFIX)
                        .withCancellationNotice(String.format(ConfirmationManager.MESSAGE_CANCELLED_FORMAT,
                                "clear"));
                case "cancelled-fail" -> throw new CommandException(
                        "The earlier 'clear' command has been cancelled."
                                + System.lineSeparator() + "Invalid command");
                case "fail" -> throw new CommandException("Invalid command");
                default -> throw new AssertionError("Unexpected command: " + commandText);
            };
        }

        private CommandResult addNoteToFirstPerson() {
            Person updatedPerson = new PersonBuilder(persons.get(INDEX_FIRST_PERSON.getZeroBased()))
                    .withNotes("New note")
                    .build();
            persons.set(INDEX_FIRST_PERSON.getZeroBased(), updatedPerson);
            return CommandResult.showStudent("Added note", updatedPerson, INDEX_FIRST_PERSON);
        }

        private CommandResult editFirstPerson() {
            Person updatedPerson = new PersonBuilder(persons.get(INDEX_FIRST_PERSON.getZeroBased()))
                    .withName("Alice Edited")
                    .build();
            persons.set(INDEX_FIRST_PERSON.getZeroBased(), updatedPerson);
            return CommandResult.showStudent("Edited Alice", updatedPerson, INDEX_FIRST_PERSON);
        }

        private CommandResult addStudent() {
            Person newStudent = new PersonBuilder(ALICE)
                    .withName("New Student")
                    .withSchool("New Secondary School")
                    .build();
            persons.add(newStudent);
            return CommandResult.showStudent("Added student", newStudent,
                    Index.fromOneBased(persons.size()));
        }

        private CommandResult deleteFirstPerson() {
            persons.remove(INDEX_FIRST_PERSON.getZeroBased());
            return CommandResult.showList("Deleted student");
        }

        private CommandResult filterToBenson() {
            persons.setAll(BENSON);
            return CommandResult.showList("Filtered students");
        }

        private CommandResult clearPersons() {
            persons.clear();
            return CommandResult.showList("Cleared students");
        }

        private CommandResult replaceFirstPersonNotes(String... notes) {
            Person updatedPerson = new PersonBuilder(persons.get(INDEX_FIRST_PERSON.getZeroBased()))
                    .withNotes(notes)
                    .build();
            persons.set(INDEX_FIRST_PERSON.getZeroBased(), updatedPerson);
            return CommandResult.showStudent("Updated note", updatedPerson, INDEX_FIRST_PERSON);
        }

        private void addStudents(int count) {
            List<Person> additionalPersons = IntStream.rangeClosed(1, count)
                    .mapToObj(index -> new PersonBuilder(ALICE)
                            .withName("Student " + index)
                            .withSchool("School " + index)
                            .build())
                    .toList();
            persons.addAll(additionalPersons);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return persons;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return new GuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            // No state needs to be stored for this test.
        }
    }
}
