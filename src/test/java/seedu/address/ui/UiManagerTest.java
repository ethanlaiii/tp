package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Tests the startup error message shown by {@link UiManager}.
 */
@ExtendWith(ApplicationExtension.class)
public class UiManagerTest {

    private static final Path DATA_FILE_PATH = Path.of("data", "addressbook.json");
    private static final String ERROR_MESSAGE = "The data file could not be loaded.\nClose the window now.";

    @Test
    public void start_withStartupError_showsErrorInFeedbackBox(FxRobot robot) {
        robot.interact(() -> {
            TextArea resultDisplay = startAndGetResultDisplay(Optional.of(ERROR_MESSAGE));
            assertEquals(ERROR_MESSAGE.lines().toList(), resultDisplay.getText().lines().toList());
            assertTrue(resultDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
            resultDisplay.getScene().getWindow().hide();
        });
    }

    @Test
    public void start_withoutStartupError_leavesFeedbackBoxEmpty(FxRobot robot) {
        robot.interact(() -> {
            TextArea resultDisplay = startAndGetResultDisplay(Optional.empty());
            assertEquals("", resultDisplay.getText());
            assertFalse(resultDisplay.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
            resultDisplay.getScene().getWindow().hide();
        });
    }

    /**
     * Starts a {@code UiManager} on a new stage and returns its result display.
     * Must be called on the JavaFX application thread.
     */
    private TextArea startAndGetResultDisplay(Optional<String> startupError) {
        Stage stage = new Stage();
        new UiManager(new MainWindowTest.LogicStub(), DATA_FILE_PATH, startupError).start(stage);
        return (TextArea) stage.getScene().lookup(".result-display");
    }
}
