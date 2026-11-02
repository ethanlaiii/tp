package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Note;

/**
 * Jackson-friendly version of {@link Note}.
 */
class JsonAdaptedNote {

    private final String text;

    /**
     * Constructs a {@code JsonAdaptedNote} with the given {@code text}.
     */
    @JsonCreator
    public JsonAdaptedNote(String text) {
        this.text = text;
    }

    /**
     * Converts a given {@code Note} into this class for Jackson use.
     */
    public JsonAdaptedNote(Note source) {
        text = source.value;
    }

    @JsonValue
    public String getText() {
        return text;
    }

    /**
     * Converts this Jackson-friendly adapted note object into the model's {@code Note} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted note.
     */
    public Note toModelType() throws IllegalValueException {
        assert text != null : "JsonAdaptedPerson rejects null notes first";
        if (!Note.isValidNote(text)) {
            throw new IllegalValueException(Note.getConstraintMessage(text));
        }
        return new Note(text);
    }

}
