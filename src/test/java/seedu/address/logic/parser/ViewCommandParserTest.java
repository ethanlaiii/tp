package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "abc", expectedMessage);
        assertParseFailure(parser, "1 extra", expectedMessage);
    }

    @Test
    public void parse_nonPositiveIndex_throwsIndexError() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_integerTooBigForInt_throwsIndexError() {
        assertParseFailure(parser, "99999999999", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_fullWidthDigitIndex_throwsParseException() {
        assertParseFailure(parser, "２",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
    }
}
