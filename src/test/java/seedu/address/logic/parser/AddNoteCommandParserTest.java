package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddNoteCommand;
import seedu.address.model.person.Note;

public class AddNoteCommandParserTest {

    private final AddNoteCommandParser parser = new AddNoteCommandParser();

    @Test
    public void parse_validArgs_returnsAddNoteCommand() {
        AddNoteCommand expectedCommand = new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Weak in algebra"));

        assertParseSuccess(parser, "1 note/Weak in algebra", expectedCommand);
        assertParseSuccess(parser, "  1   note/  Weak in algebra  ", expectedCommand);
    }

    @Test
    public void parse_noteWithSymbols_returnsAddNoteCommand() {
        assertParseSuccess(parser, "1 note/WA2 Math: 36/50",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("WA2 Math: 36/50")));
        assertParseSuccess(parser, "1 note/Rajesh s/o Kumar called",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Rajesh s/o Kumar called")));
        assertParseSuccess(parser, "1 note/Revise t/f questions",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Revise t/f questions")));
        assertParseSuccess(parser, "1 note/Call Mrs Tan c/o school",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Call Mrs Tan c/o school")));
        assertParseSuccess(parser, "1 note/see nid/2",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("see nid/2")));
    }

    @Test
    public void parse_maxLengthNote_returnsAddNoteCommand() {
        String maxLengthText = "a".repeat(Note.MAX_LENGTH);
        assertParseSuccess(parser, "1 note/" + maxLengthText,
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note(maxLengthText)));
    }

    @Test
    public void parse_missingIndexOrPrefix_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddNoteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " note/hi", expectedMessage);
        assertParseFailure(parser, " 1 hi", expectedMessage);

        // missing space between index and prefix
        assertParseFailure(parser, " 1note/hi", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, " 0 note/hi", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " -1 note/hi", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 99999999999 note/hi", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_nonIntegerIndex_throwsCommandFormatError() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddNoteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " abc note/hi", expectedMessage);

        // non-integer index is reported before the blank note
        assertParseFailure(parser, " abc note/   ", expectedMessage);
    }

    @Test
    public void parse_textAfterIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddNoteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " 1 abc note/hi", expectedMessage);

        // prefixes that addnote does not take, such as nid/ from editnote or t/ from older versions
        assertParseFailure(parser, " 1 nid/1 note/Weak in algebra", expectedMessage);
        assertParseFailure(parser, " 1 t/x note/Weak in algebra", expectedMessage);
        assertParseFailure(parser, "  1   nid/1   note/hi", expectedMessage);

        // extra text is reported before an invalid index
        assertParseFailure(parser, " 0 nid/1 note/hi", expectedMessage);
    }

    @Test
    public void parse_invalidIndexAndInvalidNote_throwsIndexError() {
        assertParseFailure(parser, " 0 note/", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 0 note/a note/b", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 0 note/" + "a".repeat(Note.MAX_LENGTH + 1), MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_blankNote_throwsParseException() {
        assertParseFailure(parser, " 1 note/", Note.MESSAGE_BLANK);
        assertParseFailure(parser, " 1 note/   ", Note.MESSAGE_BLANK);
    }

    @Test
    public void parse_tooLongNote_throwsParseException() {
        assertParseFailure(parser, " 1 note/" + "a".repeat(Note.MAX_LENGTH + 1), Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_nonAsciiNote_throwsParseException() {
        assertParseFailure(parser, " 1 note/Français oral 18/20 😀",
                String.format(Note.MESSAGE_INVALID_CHARACTER, "ç"));
        assertParseFailure(parser, " 1 note/Good effort 😀",
                String.format(Note.MESSAGE_INVALID_CHARACTER, "😀"));
    }

    @Test
    public void parse_trailingTextTooLong_throwsParseException() {
        // the text before the repeated note/ fits, but the repeated note/ and its text count toward the length
        assertParseFailure(parser, " 1 note/" + "a".repeat(Note.MAX_LENGTH - 5) + " note/abcdef",
                Note.MESSAGE_TOO_LONG);
    }

    @Test
    public void parse_repeatedPrefix_treatedAsNoteText() {
        assertParseSuccess(parser, "1 note/a note/b",
                new AddNoteCommand(INDEX_FIRST_PERSON, new Note("a note/b")));
    }

    @Test
    public void parse_fullWidthDigitIndex_throwsParseException() {
        assertParseFailure(parser, "１ note/x",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddNoteCommand.MESSAGE_USAGE));
    }
}
