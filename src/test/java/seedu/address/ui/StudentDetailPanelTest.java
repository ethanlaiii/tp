package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.model.person.Band;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests {@link StudentDetailPanel}.
 */
@ExtendWith(ApplicationExtension.class)
public class StudentDetailPanelTest {

    private static final double[][] SUPPORTED_DETAIL_SIZES = {
        {950, 1080},
        {758, 864},
        {416, 480},
        {290, 400}
    };

    private StackPane container;
    private ObservableList<Person> persons;

    @Start
    public void start(Stage stage) {
        container = new StackPane();
        persons = FXCollections.observableArrayList(ALICE);
        stage.setScene(new Scene(container));
        stage.getScene().getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        stage.show();
    }

    @Test
    public void constructor_studentWithDetails_displaysCompleteRecord(FxRobot robot) {
        Person personWithNotes = new PersonBuilder(ALICE)
                .withAddress("12 Example Road")
                .withEmail("alice@example.com")
                .withTags("paid", "exams")
                .withNotes("Strong in algebra", "Revise geometry")
                .build();

        robot.interact(() -> {
            StudentDetailPanel panel = new StudentDetailPanel(personWithNotes, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();

            assertLabelText(".student-detail-heading", "Index: 1");
            assertLabelText("#name", ALICE.getName().fullName);
            assertLabelText("#school", ALICE.getSchool().value);
            assertLabelText("#address", "12 Example Road");
            assertLabelText("#email", "alice@example.com");
            assertLabelText("#tags", "paid, exams");
            assertLabelText("#level", ALICE.getLevel().toString());
            assertLabelText("#subjects", "Mathematics (G1)");
            assertLabelText("#phone", ALICE.getPhone().orElseThrow().value);
            assertLabelText("#guardianName", "—");
            assertLabelText("#guardianPhone", "—");
            assertGridRow("#name", 0);
            assertGridRow("#phone", 1);
            assertGridRow("#school", 2);
            assertGridRow("#level", 3);
            assertGridRow("#subjects", 4);
            assertGridRow("#address", 5);
            assertGridRow("#email", 6);
            assertGridRow("#tags", 7);
            assertGridRow("#guardianName", 8);
            assertGridRow("#guardianPhone", 9);
            assertNull(container.lookup("#heading"));
            assertNoteCardTexts("1. Strong in algebra", "2. Revise geometry");
        });
    }

    @Test
    public void constructor_studentWithDetails_placesEntireRecordInsideScrollPane(FxRobot robot) {
        Person personWithNotes = new PersonBuilder(ALICE).withNotes("Strong in algebra").build();

        robot.interact(() -> {
            StudentDetailPanel panel = new StudentDetailPanel(personWithNotes, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            BorderPane detailPanel = (BorderPane) panel.getRoot();
            ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");
            VBox studentDetailContent = (VBox) detailScrollPane.getContent();

            assertTrue(studentDetailContent.getChildren().indexOf(container.lookup("#name").getParent())
                    < studentDetailContent.getChildren()
                            .indexOf(container.lookup(".student-detail-section-separator")));
            assertTrue(studentDetailContent.getChildren()
                    .indexOf(container.lookup(".student-detail-section-separator"))
                    < studentDetailContent.getChildren()
                            .indexOf(container.lookup(".student-detail-notes-heading")));
            assertSame(detailScrollPane, detailPanel.getCenter());
            assertSame(studentDetailContent, container.lookup("#noteCards").getParent());
            assertNotNull(detailScrollPane.lookup("#name"));
            assertNull(container.lookup("#notesScrollPane"));
        });
    }

    @Test
    public void constructor_studentWithDetails_configuresScrollBars(FxRobot robot) {
        robot.interact(() -> {
            StudentDetailPanel panel = new StudentDetailPanel(ALICE, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");

            assertTrue(detailScrollPane.isFitToWidth());
            assertEquals(ScrollBarPolicy.NEVER, detailScrollPane.getHbarPolicy());
            assertEquals(ScrollBarPolicy.AS_NEEDED, detailScrollPane.getVbarPolicy());
        });
    }

    @Test
    public void constructor_longRecord_scrollsEntireRecord(FxRobot robot) {
        String longNote = "Review algebra, geometry, and statistics before the next lesson. ".repeat(4);
        Person personWithLongRecord = new PersonBuilder(ALICE)
                .withNotes(longNote, longNote, longNote, longNote, longNote)
                .build();

        robot.interact(() -> {
            container.getScene().getWindow().setWidth(500);
            container.getScene().getWindow().setHeight(350);
            StudentDetailPanel panel =
                    new StudentDetailPanel(personWithLongRecord, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            container.layout();
            ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");
            VBox studentDetailContent = (VBox) detailScrollPane.getContent();
            Region viewport = (Region) container.lookup(".student-detail-scroll-pane > .viewport");
            Node nameLabel = container.lookup("#name");

            assertTrue(studentDetailContent.getHeight() > detailScrollPane.getViewportBounds().getHeight());
            Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
            assertTrue(nameLabel.localToScene(nameLabel.getBoundsInLocal()).intersects(viewportBounds));

            detailScrollPane.setVvalue(detailScrollPane.getVmax());
            detailScrollPane.layout();
            assertTrue(nameLabel.localToScene(nameLabel.getBoundsInLocal()).getMaxY()
                    < viewportBounds.getMinY());
        });
    }

    @Test
    public void constructor_studentWithoutNotes_displaysEmptyState(FxRobot robot) {
        robot.interact(() -> {
            StudentDetailPanel panel = new StudentDetailPanel(ALICE, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();

            assertLabelText("#emptyNotes", "No notes recorded.");
            assertLabelText("#tags", "—");
        });
    }

    @Test
    public void constructor_studentWithGuardian_displaysGuardianDetails(FxRobot robot) {
        Person personWithGuardian = new PersonBuilder(ALICE)
                .withGuardian("Mary Tan", "91234567")
                .build();

        robot.interact(() -> {
            StudentDetailPanel panel =
                    new StudentDetailPanel(personWithGuardian, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();

            assertLabelText("#guardianName", "Mary Tan");
            assertLabelText("#guardianPhone", "91234567");
        });
    }

    @Test
    public void constructor_longValuesAtMinimumWidth_wrapWithoutHorizontalClipping(FxRobot robot) {
        String longName = "Alexandra Catherine Elizabeth Tan ".repeat(3).trim();
        String longSchool = "Singapore International Secondary School ".repeat(2).trim();
        String longNote = "Review algebra, geometry, and statistics before the next lesson. ".repeat(4).trim();

        robot.interact(() -> {
            container.getScene().getWindow().setWidth(300);
            container.getScene().getWindow().setHeight(400);
            Person personWithLongValues = new PersonBuilder(ALICE)
                    .withName(longName)
                    .withSchool(longSchool)
                    .withNotes(longNote)
                    .build();
            StudentDetailPanel panel =
                    new StudentDetailPanel(personWithLongValues, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            container.layout();

            assertLabelWraps("#name");
            assertLabelWraps("#school");
            assertLabelWraps("#level");
            assertLabelWraps("#subjects");
            assertLabelWraps("#phone");
            assertLabelWraps("#guardianName");
            assertLabelWraps("#guardianPhone");
            assertLabelWraps(".student-detail-note-card");
            assertContentFitsViewport("#name");
            assertContentFitsViewport("#school");
            assertContentFitsViewport(".student-detail-note-card");
            assertViewportMatchesPanelBackground();
        });
    }

    @Test
    public void constructor_fieldNamesAtMinimumWidth_wrapCompleteText(FxRobot robot) {
        robot.interact(() -> {
            container.getScene().getWindow().setWidth(300);
            container.getScene().getWindow().setHeight(400);
            StudentDetailPanel panel = new StudentDetailPanel(ALICE, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            container.layout();

            assertFieldNameWraps("Subjects and bands");
            assertFieldNameWraps("Guardian phone");
        });
    }

    @Test
    public void constructor_supportedSizesAndRecordVariants_renderCompleteText(FxRobot robot) {
        Person missingOptionalValues = new PersonBuilder(ALICE)
                .withoutPhone()
                .withoutAddress()
                .withoutEmail()
                .withoutGuardian()
                .build();
        Person maximumContentPerson = createMaximumContentPerson();
        List<Person> recordVariants = List.of(ALICE, missingOptionalValues, maximumContentPerson);

        robot.interact(() -> {
            for (double[] size : SUPPORTED_DETAIL_SIZES) {
                container.getScene().getWindow().setWidth(size[0]);
                container.getScene().getWindow().setHeight(size[1]);
                for (Person person : recordVariants) {
                    StudentDetailPanel panel = new StudentDetailPanel(person, INDEX_FIRST_PERSON, persons);
                    container.getChildren().setAll(panel.getRoot());
                    container.applyCss();
                    container.layout();

                    assertRecordTextIsComplete();
                    if (person == maximumContentPerson) {
                        assertLastNoteCanBeScrolledIntoView();
                    }
                }
            }
        });
    }

    @Test
    public void sourceList_studentReplaced_refreshesDisplayedRecord(FxRobot robot) {
        robot.interact(() -> {
            StudentDetailPanel panel = new StudentDetailPanel(ALICE, INDEX_FIRST_PERSON, persons);
            container.getChildren().setAll(panel.getRoot());
            container.applyCss();
            Person updatedPerson = new PersonBuilder(ALICE).withNotes("New note").build();

            persons.set(INDEX_FIRST_PERSON.getZeroBased(), updatedPerson);

            assertNoteCardTexts("1. New note");
        });
    }

    private void assertLabelText(String query, String expectedText) {
        assertEquals(expectedText, ((Label) container.lookup(query)).getText());
    }

    private void assertLabelWraps(String query) {
        assertTrue(((Label) container.lookup(query)).isWrapText());
    }

    private void assertFieldNameWraps(String text) {
        Label fieldName = container.lookupAll(".student-detail-field-name").stream()
                .map(Label.class::cast)
                .filter(label -> label.getText().equals(text))
                .findFirst()
                .orElseThrow();
        assertTrue(fieldName.isWrapText());
        assertTrue(fieldName.getHeight() > fieldName.getFont().getSize() * 1.5);
        assertContentFitsViewport(fieldName);
    }

    private void assertRecordTextIsComplete() {
        VBox content = (VBox) container.lookup("#studentDetailContent");
        ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");
        Bounds viewportBounds = container.lookup(".student-detail-scroll-pane > .viewport")
                .localToScene(container.lookup(".student-detail-scroll-pane > .viewport").getBoundsInLocal());

        assertEquals(ScrollBarPolicy.NEVER, detailScrollPane.getHbarPolicy());
        for (Node node : content.lookupAll(".label")) {
            Label label = (Label) node;
            Text renderedText = (Text) label.lookup(".text");
            Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
            assertEquals(label.getText(), renderedText.getText());
            assertTrue(labelBounds.getMinX() >= viewportBounds.getMinX());
            assertTrue(labelBounds.getMaxX() <= viewportBounds.getMaxX());
        }
    }

    private void assertLastNoteCanBeScrolledIntoView() {
        ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");
        Region viewport = (Region) container.lookup(".student-detail-scroll-pane > .viewport");
        List<Label> noteCards = container.lookupAll(".student-detail-note-card").stream()
                .map(Label.class::cast)
                .toList();
        Label lastNote = noteCards.get(noteCards.size() - 1);

        detailScrollPane.setVvalue(detailScrollPane.getVmax());
        detailScrollPane.layout();
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        Bounds lastNoteBounds = lastNote.localToScene(lastNote.getBoundsInLocal());
        assertTrue(lastNoteBounds.intersects(viewportBounds));
    }

    private Person createMaximumContentPerson() {
        String longEmail = "a".repeat(64) + "@" + "b".repeat(63) + "."
                + "c".repeat(63) + "." + "d".repeat(60);
        return new PersonBuilder(ALICE)
                .withName("Alexandra ".repeat(20).trim())
                .withPhone("+123456789012345")
                .withSchool("S".repeat(100))
                .withAddress("A".repeat(200))
                .withEmail(longEmail)
                .withSubjects(
                        new Subject("Advanced Mathematics and Calculus A", Band.G1),
                        new Subject("Advanced Mathematics and Calculus B", Band.G2),
                        new Subject("Advanced Mathematics and Calculus C", Band.G3),
                        new Subject("Advanced Mathematics and Calculus D", Band.G1),
                        new Subject("Advanced Mathematics and Calculus E", Band.G2),
                        new Subject("Advanced Mathematics and Calculus F", Band.G3),
                        new Subject("Advanced Mathematics and Calculus G", Band.G1),
                        new Subject("Advanced Mathematics and Calculus H", Band.G2))
                .withGuardian("Guardian ".repeat(20).trim(), "+123456789012345")
                .withTags("A".repeat(30), "B".repeat(30), "C".repeat(30), "D".repeat(30))
                .withNotes("N".repeat(500), "O".repeat(500), "P".repeat(500))
                .build();
    }

    private void assertContentFitsViewport(String query) {
        assertContentFitsViewport(container.lookup(query));
    }

    private void assertContentFitsViewport(Node content) {
        ScrollPane detailScrollPane = (ScrollPane) container.lookup("#detailScrollPane");
        assertTrue(content.getBoundsInParent().getWidth() <= detailScrollPane.getViewportBounds().getWidth());
    }

    private void assertGridRow(String query, int expectedRow) {
        assertEquals(expectedRow, GridPane.getRowIndex(container.lookup(query)));
    }

    private void assertNoteCardTexts(String... expectedTexts) {
        List<String> noteTexts = container.lookupAll(".student-detail-note-card").stream()
                .map(Label.class::cast)
                .map(Label::getText)
                .toList();
        assertEquals(List.of(expectedTexts), noteTexts);
    }

    private void assertViewportMatchesPanelBackground() {
        Region viewport = (Region) container.lookup(".student-detail-scroll-pane > .viewport");
        Region panel = (Region) container.lookup(".student-detail-panel");
        assertEquals(panel.getBackground(), viewport.getBackground());
    }
}
