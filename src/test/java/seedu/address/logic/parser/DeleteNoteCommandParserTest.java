package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NID;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_NID;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteNoteCommand;

public class DeleteNoteCommandParserTest {

    private final DeleteNoteCommandParser parser = new DeleteNoteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteNoteCommand() {
        DeleteNoteCommand expectedCommand = new DeleteNoteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2));

        assertParseSuccess(parser, "1 nid/2", expectedCommand);
        assertParseSuccess(parser, "  1   nid/  2  ", expectedCommand);
    }

    @Test
    public void parse_forceOption_returnsForcedDeleteNoteCommand() {
        assertParseSuccess(parser, "-f 1 nid/2",
                new DeleteNoteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2), true));
    }

    @Test
    public void parse_forceOptionWrongPositionOrCase_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 -f nid/2", expectedMessage);
        assertParseFailure(parser, "-F 1 nid/2", expectedMessage);
    }

    @Test
    public void parse_forceOptionAfterNid_throwsCommandFormatError() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 nid/1 -f", expectedMessage);
        assertParseFailure(parser, "  1 nid/1   -f  ", expectedMessage);
        assertParseFailure(parser, "1 nid/-f", expectedMessage);
        assertParseFailure(parser, "-f 1 nid/1 -f", expectedMessage);

        // reported before an invalid index
        assertParseFailure(parser, "0 nid/1 -f", expectedMessage);

        // -f after an earlier nid/ is also caught when nid/ is repeated
        assertParseFailure(parser, "1 nid/1 -f nid/2", expectedMessage);
    }

    @Test
    public void parse_maxIntNid_returnsDeleteNoteCommand() {
        assertParseSuccess(parser, "1 nid/2147483647",
                new DeleteNoteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingIndexOrPrefix_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " nid/2", expectedMessage);
        assertParseFailure(parser, " 1", expectedMessage);

        // NID given without its prefix
        assertParseFailure(parser, " 1 2", expectedMessage);

        // missing space between index and prefix
        assertParseFailure(parser, " 1nid/2", expectedMessage);

        // addnote's note text prefix instead of nid/
        assertParseFailure(parser, " 1 note/2", expectedMessage);

        // prefixes are case-sensitive
        assertParseFailure(parser, " 1 NID/2", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, " 0 nid/1", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " -1 nid/1", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 99999999999 nid/1", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " -f 0 nid/1", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_nonIntegerIndex_throwsCommandFormatError() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " abc nid/1", expectedMessage);
        assertParseFailure(parser, " -f abc nid/1", expectedMessage);

        // non-integer index is reported before the empty NID
        assertParseFailure(parser, " abc nid/", expectedMessage);
    }

    @Test
    public void parse_extraTextBeforePrefix_throwsCommandFormatError() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1 abc nid/2", expectedMessage);
        assertParseFailure(parser, " 1 2 nid/3", expectedMessage);
    }

    @Test
    public void parse_invalidNid_throwsParseException() {
        assertParseFailure(parser, " 1 nid/0", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/-1", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/abc", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/99999999999", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/1.5", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/+1", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/2 abc", MESSAGE_INVALID_NID);
    }

    @Test
    public void parse_emptyNid_throwsParseException() {
        assertParseFailure(parser, " 1 nid/", MESSAGE_INVALID_NID);
        assertParseFailure(parser, " 1 nid/   ", MESSAGE_INVALID_NID);
    }

    @Test
    public void parse_invalidIndexAndInvalidNid_throwsIndexError() {
        assertParseFailure(parser, " 0 nid/0", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 0 nid/1 nid/2", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NID);

        assertParseFailure(parser, " 1 nid/1 nid/2", expectedMessage);
        assertParseFailure(parser, " 1 nid/1 nid/1", expectedMessage);

        // repeated prefix is reported before the invalid NID
        assertParseFailure(parser, " 1 nid/abc nid/2", expectedMessage);
    }

    @Test
    public void parse_fullWidthDigitNid_throwsParseException() {
        assertParseFailure(parser, " 1 nid/１", MESSAGE_INVALID_NID);
    }
}
