package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Note;

public class JsonAdaptedNoteTest {

    @Test
    public void toModelType_validText_returnsNote() throws Exception {
        JsonAdaptedNote note = new JsonAdaptedNote("WA2 Math: 36/50");
        assertEquals(new Note("WA2 Math: 36/50"), note.toModelType());
    }

    @Test
    public void toModelType_paddedText_returnsTrimmedNote() throws Exception {
        JsonAdaptedNote note = new JsonAdaptedNote("  Weak in algebra  ");
        assertEquals("Weak in algebra", note.toModelType().value);
    }

    @Test
    public void toModelType_blankText_throwsIllegalValueException() {
        JsonAdaptedNote note = new JsonAdaptedNote("   ");
        assertThrows(IllegalValueException.class, Note.MESSAGE_BLANK, note::toModelType);
    }

    @Test
    public void toModelType_tooLongText_throwsIllegalValueException() {
        JsonAdaptedNote note = new JsonAdaptedNote("a".repeat(Note.MAX_LENGTH + 1));
        assertThrows(IllegalValueException.class, Note.MESSAGE_TOO_LONG, note::toModelType);
    }

    @Test
    public void toModelType_nonAsciiText_throwsIllegalValueException() {
        JsonAdaptedNote note = new JsonAdaptedNote("Français oral 18/20");
        assertThrows(IllegalValueException.class, String.format(Note.MESSAGE_INVALID_CHARACTER, "ç"),
                note::toModelType);
    }

    @Test
    public void toJson_note_savesPlainString() throws Exception {
        Note note = new Note("Weak in algebra");
        String json = JsonUtil.toJsonString(new JsonAdaptedNote(note));

        assertEquals("\"Weak in algebra\"", json);
        assertEquals(note, JsonUtil.fromJsonString(json, JsonAdaptedNote.class).toModelType());
    }
}
