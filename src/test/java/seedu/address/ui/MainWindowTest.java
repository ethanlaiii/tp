package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * Tests help behavior in {@link MainWindow}.
 */
@ExtendWith(ApplicationExtension.class)
public class MainWindowTest {

    private static final String ERROR_MESSAGE = "The data file could not be loaded.\nClose the window now.";
    private static final double MINIMUM_WINDOW_WIDTH = 600;
    private static final double MINIMUM_WINDOW_HEIGHT = 400;
    private static final double[][] SUPPORTED_WINDOW_SIZES = {
        {1920, 1080},
        {1536, 864},
        {853, 480},
        {MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT}
    };

    private MainWindow mainWindow;

    @Start
    public void start(Stage stage) {
        mainWindow = new MainWindow(stage, new LogicStub(), Path.of("data", "addressbook.json"));
        mainWindow.fillInnerParts();
        mainWindow.show();
    }

    @Test
    public void handleHelp_helpRequested_showsCompleteHelpInFeedbackBox(FxRobot robot) {
        robot.interact(() -> {
            mainWindow.handleHelp();
            TextArea resultDisplay = (TextArea) mainWindow.getRoot().getScene()
                    .lookup(".result-display");
            assertEquals(SHOWING_HELP_MESSAGE.lines().toList(),
                    resultDisplay.getText().lines().toList());
        });
    }

    @Test
    public void handleHelp_repeatedRequest_reusesFeedbackBox(FxRobot robot) {
        robot.interact(() -> {
            TextArea originalResultDisplay = (TextArea) mainWindow.getRoot().getScene()
                    .lookup(".result-display");
            mainWindow.handleHelp();
            mainWindow.handleHelp();

            TextArea currentResultDisplay = (TextArea) mainWindow.getRoot().getScene()
                    .lookup(".result-display");
            assertSame(originalResultDisplay, currentResultDisplay);
            assertEquals(1, mainWindow.getRoot().getScene().getRoot()
                    .lookupAll(".result-display").size());
        });
    }

    @Test
    public void windowSupportedSizes_layoutKeepsAllRegionsVisible(FxRobot robot) {
        robot.interact(() -> {
            assertEquals(MINIMUM_WINDOW_WIDTH, mainWindow.getRoot().getMinWidth());
            assertEquals(MINIMUM_WINDOW_HEIGHT, mainWindow.getRoot().getMinHeight());
            for (double[] size : SUPPORTED_WINDOW_SIZES) {
                mainWindow.getRoot().setWidth(size[0]);
                mainWindow.getRoot().setHeight(size[1]);
                mainWindow.getRoot().getScene().getRoot().applyCss();
                mainWindow.getRoot().getScene().getRoot().layout();

                assertVisible("#menuBar");
                assertVisible("#commandBoxPlaceholder");
                assertVisible("#resultDisplayPlaceholder");
                assertVisible("#personListPanelPlaceholder");
                assertVisible("#studentDetailPanelPlaceholder");
                assertVisible("#statusbarPlaceholder");
            }
        });
    }

    @Test
    public void windowBounds_oversizedSavedSize_clampsToVisualBounds(FxRobot robot) {
        Rectangle2D visualBounds = new Rectangle2D(100, 100, 853, 450);
        GuiSettings oversizedSettings = new GuiSettings(1200, 900, 200, 200);

        robot.interact(() -> mainWindow.setWindowDefaultSize(oversizedSettings, visualBounds));

        assertEquals(visualBounds.getWidth(), mainWindow.getRoot().getWidth());
        assertEquals(visualBounds.getHeight(), mainWindow.getRoot().getHeight());
        assertEquals(visualBounds.getMinX(), mainWindow.getRoot().getX());
        assertEquals(visualBounds.getMinY(), mainWindow.getRoot().getY());
    }

    @Test
    public void windowBounds_offScreenCoordinates_movesInsideVisualBounds(FxRobot robot) {
        Rectangle2D visualBounds = new Rectangle2D(100, 50, 853, 450);
        GuiSettings offScreenSettings = new GuiSettings(700, 400, -500, 900);

        robot.interact(() -> mainWindow.setWindowDefaultSize(offScreenSettings, visualBounds));

        assertEquals(700, mainWindow.getRoot().getWidth());
        assertEquals(400, mainWindow.getRoot().getHeight());
        assertEquals(visualBounds.getMinX(), mainWindow.getRoot().getX());
        assertEquals(visualBounds.getMaxY() - mainWindow.getRoot().getHeight(), mainWindow.getRoot().getY());
    }

    @Test
    public void windowBounds_savedSizeBelowMinimum_usesMinimumSize(FxRobot robot) {
        Rectangle2D visualBounds = new Rectangle2D(0, 0, 853, 450);
        GuiSettings undersizedSettings = new GuiSettings(100, 100, 0, 0);

        robot.interact(() -> mainWindow.setWindowDefaultSize(undersizedSettings, visualBounds));

        assertEquals(MINIMUM_WINDOW_WIDTH, mainWindow.getRoot().getWidth());
        assertEquals(MINIMUM_WINDOW_HEIGHT, mainWindow.getRoot().getHeight());
    }

    @Test
    public void showError_multiLineMessage_showsWholeMessageAsErrorInFeedbackBox(FxRobot robot) {
        robot.interact(() -> {
            mainWindow.showError(ERROR_MESSAGE);
            TextArea resultDisplay = (TextArea) mainWindow.getRoot().getScene()
                    .lookup(".result-display");
            assertEquals(ERROR_MESSAGE.lines().toList(), resultDisplay.getText().lines().toList());
            assertTrue(resultDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
            assertFalse(resultDisplay.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
        });
    }

    private void assertVisible(String selector) {
        Node node = mainWindow.getRoot().getScene().lookup(selector);
        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
        Bounds sceneBounds = mainWindow.getRoot().getScene().getRoot().getLayoutBounds();
        assertTrue(node.isVisible());
        assertTrue(node.isManaged());
        assertTrue(nodeBounds.getWidth() > 0);
        assertTrue(nodeBounds.getHeight() > 0);
        assertTrue(nodeBounds.getMinX() >= sceneBounds.getMinX());
        assertTrue(nodeBounds.getMinY() >= sceneBounds.getMinY());
        assertTrue(nodeBounds.getMaxX() <= sceneBounds.getMaxX());
        assertTrue(nodeBounds.getMaxY() <= sceneBounds.getMaxY());
    }

    /**
     * A {@code Logic} stub with an empty student list that rejects every command.
     */
    static class LogicStub implements Logic {

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            throw new AssertionError("Command execution is not expected in this test.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.observableArrayList();
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
