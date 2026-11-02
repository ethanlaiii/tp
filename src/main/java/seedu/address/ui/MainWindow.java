package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.DisplayMode;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private StudentDetailPanel studentDetailPanel;
    private ResultDisplay resultDisplay;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane studentDetailPanelPlaceholder;

    @FXML
    private Label studentDetailEmptyState;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and show
         * help purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().setAll(personListPanel.getRoot());
        showEmptyStudentDetails();

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        Rectangle2D visualBounds = getTargetScreen(guiSettings).getVisualBounds();
        setWindowDefaultSize(guiSettings, visualBounds);
    }

    void setWindowDefaultSize(GuiSettings guiSettings, Rectangle2D visualBounds) {
        double windowWidth = constrainDimension(guiSettings.getWindowWidth(), primaryStage.getMinWidth(),
                visualBounds.getWidth());
        double windowHeight = constrainDimension(guiSettings.getWindowHeight(), primaryStage.getMinHeight(),
                visualBounds.getHeight());
        primaryStage.setWidth(windowWidth);
        primaryStage.setHeight(windowHeight);
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(constrainPosition(guiSettings.getWindowCoordinates().getX(), visualBounds.getMinX(),
                    visualBounds.getMaxX() - windowWidth));
            primaryStage.setY(constrainPosition(guiSettings.getWindowCoordinates().getY(), visualBounds.getMinY(),
                    visualBounds.getMaxY() - windowHeight));
        }
    }

    private Screen getTargetScreen(GuiSettings guiSettings) {
        if (guiSettings.getWindowCoordinates() == null) {
            return Screen.getPrimary();
        }
        double x = guiSettings.getWindowCoordinates().getX();
        double y = guiSettings.getWindowCoordinates().getY();
        List<Screen> screens = Screen.getScreensForRectangle(x, y, 1, 1);
        return screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
    }

    private static double constrainDimension(double requested, double minimum, double available) {
        return Math.min(Math.max(requested, minimum), available);
    }

    private static double constrainPosition(double requested, double minimum, double maximum) {
        return Math.max(minimum, Math.min(requested, Math.max(minimum, maximum)));
    }

    /**
     * Shows help in the result display.
     */
    @FXML
    public void handleHelp() {
        resultDisplay.setFeedbackToUser(HelpCommand.SHOWING_HELP_MESSAGE);
    }

    /**
     * Shows {@code message} in the result display with error styling.
     */
    void showError(String message) {
        resultDisplay.setFeedbackToUser(message, false, null, true);
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser(), commandResult.isWarning(),
                    commandResult.getCancellationNotice().orElse(null), false);

            updateMainContent(commandResult);

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            String errorMessage = e.getMessage();
            int lineBreak = errorMessage.indexOf('\n');
            if (lineBreak > 0 && errorMessage.substring(0, lineBreak).stripTrailing()
                    .endsWith("command has been cancelled.")) {
                resultDisplay.setFeedbackToUser(errorMessage.substring(lineBreak + 1), false,
                        errorMessage.substring(0, lineBreak), true);
            } else {
                resultDisplay.setFeedbackToUser(errorMessage, false, null, true);
            }
            throw e;
        }
    }

    private void updateMainContent(CommandResult commandResult) {
        DisplayMode displayMode = commandResult.getDisplayMode();
        if (displayMode == DisplayMode.SHOW_LIST) {
            showEmptyStudentDetails();
        } else if (displayMode == DisplayMode.SHOW_STUDENT) {
            Index displayedIndex = commandResult.getDisplayedIndex().orElseThrow();
            Person displayedPerson = commandResult.getPersonToDisplay().orElseThrow();
            studentDetailPanel = new StudentDetailPanel(displayedPerson, displayedIndex,
                    logic.getFilteredPersonList());
            studentDetailPanelPlaceholder.getChildren().setAll(studentDetailPanel.getRoot());
            personListPanel.highlightPerson(displayedPerson);
        }
    }

    private void showEmptyStudentDetails() {
        studentDetailPanel = null;
        studentDetailPanelPlaceholder.getChildren().setAll(studentDetailEmptyState);
        personListPanel.clearHighlight();
    }
}
