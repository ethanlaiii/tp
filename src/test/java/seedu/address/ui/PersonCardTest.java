package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests {@link PersonCard}.
 */
@ExtendWith(ApplicationExtension.class)
public class PersonCardTest {

    private static final double[] SUPPORTED_CARD_WIDTHS = {950, 758, 416, 290};

    private StackPane container;

    @Start
    public void start(Stage stage) {
        container = new StackPane();
        stage.setScene(new Scene(container));
        stage.getScene().getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        stage.setHeight(900);
        stage.show();
    }

    @Test
    public void constructor_personWithAllDetails_displaysOnlySummaryFields(FxRobot robot) {
        Person person = new PersonBuilder(ALICE)
                .withAddress("12 Example Road")
                .withEmail("alice@example.com")
                .withGuardian("Mary Tan", "91234567")
                .withNotes("Weak in algebra")
                .build();

        robot.interact(() -> {
            PersonCard card = new PersonCard(person, 3);
            assertLabelText(card, "#id", "3. ");
            assertLabelText(card, "#name", person.getName().fullName);
            assertLabelText(card, "#phone", person.getPhone().orElseThrow().value);
            assertLabelText(card, "#school", person.getSchool().value);
            assertLabelText(card, "#address", "12 Example Road");
            assertLabelText(card, "#email", "alice@example.com");
            assertLabelText(card, "#level", person.getLevel().name());
            assertLabelWraps(card, "#name");
            assertLabelWraps(card, "#phone");
            assertLabelWraps(card, "#school");
            assertLabelWraps(card, "#address");
            assertLabelWraps(card, "#email");
            assertLabelWraps(card, "#level");
            assertEquals(Pos.TOP_LEFT, ((HBox) card.getRoot().lookup("#name").getParent()).getAlignment());
            VBox cardContents = (VBox) card.getRoot().lookup("#phone").getParent();
            assertEquals(new Insets(12, 14, 12, 18), cardContents.getPadding());
            assertNull(card.getRoot().lookup("#guardian"));
            assertNull(card.getRoot().lookup("#subjects"));
            assertNull(card.getRoot().lookup("#notes"));
        });
    }

    @Test
    public void constructor_personWithoutPhone_hidesPhoneField(FxRobot robot) {
        Person person = new PersonBuilder(ALICE).withoutPhone().build();

        robot.interact(() -> {
            PersonCard card = new PersonCard(person, 1);
            Label phone = (Label) card.getRoot().lookup("#phone");
            assertFalse(phone.isVisible());
            assertFalse(phone.isManaged());
            Label address = (Label) card.getRoot().lookup("#address");
            assertFalse(address.isVisible());
            assertFalse(address.isManaged());
            Label email = (Label) card.getRoot().lookup("#email");
            assertFalse(email.isVisible());
            assertFalse(email.isManaged());
        });
    }

    @Test
    public void constructor_personWithTags_showsTagsInOrder(FxRobot robot) {
        Person person = new PersonBuilder(ALICE).withTags("paid", "exams").build();

        robot.interact(() -> {
            PersonCard card = new PersonCard(person, 1);
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertTrue(tags.isVisible());
            assertEquals(List.of("paid", "exams"), tags.getChildren().stream()
                    .map(tag -> ((Label) tag).getText()).toList());
        });
    }

    @Test
    public void constructor_personWithoutTags_hidesTags(FxRobot robot) {
        robot.interact(() -> {
            PersonCard card = new PersonCard(ALICE, 1);
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertFalse(tags.isVisible());
            assertFalse(tags.isManaged());
        });
    }

    @Test
    public void constructor_supportedWidthsAndMaximumContent_renderCompleteText(FxRobot robot) {
        String longEmail = "a".repeat(64) + "@" + "b".repeat(63) + "."
                + "c".repeat(63) + "." + "d".repeat(60);
        Person person = new PersonBuilder(ALICE)
                .withName("Alexandra ".repeat(20).trim())
                .withPhone("+123456789012345")
                .withSchool("S".repeat(100))
                .withAddress("A".repeat(200))
                .withEmail(longEmail)
                .withTags("A".repeat(30), "B".repeat(30), "C".repeat(30), "D".repeat(30))
                .build();

        robot.interact(() -> {
            for (double width : SUPPORTED_CARD_WIDTHS) {
                container.getScene().getWindow().setWidth(width);
                PersonCard card = new PersonCard(person, 1);
                container.getChildren().setAll(card.getRoot());
                container.applyCss();
                container.layout();

                assertCardTextIsComplete(card);
            }
        });
    }

    private void assertCardTextIsComplete(PersonCard card) {
        Bounds cardBounds = card.getRoot().localToScene(card.getRoot().getBoundsInLocal());
        for (Node node : card.getRoot().lookupAll(".label")) {
            Label label = (Label) node;
            if (!label.isVisible()) {
                continue;
            }
            Text renderedText = (Text) label.lookup(".text");
            Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
            assertEquals(label.getText(), renderedText.getText());
            assertTrue(labelBounds.getMinX() >= cardBounds.getMinX());
            assertTrue(labelBounds.getMaxX() <= cardBounds.getMaxX());
        }
    }

    private static void assertLabelText(PersonCard card, String query, String expectedText) {
        assertEquals(expectedText, ((Label) card.getRoot().lookup(query)).getText());
    }

    private static void assertLabelWraps(PersonCard card, String query) {
        assertTrue(((Label) card.getRoot().lookup(query)).isWrapText());
    }
}
