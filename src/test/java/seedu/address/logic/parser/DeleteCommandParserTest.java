package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_forceOption_returnsForcedDeleteCommand() {
        assertParseSuccess(parser, "-f 1", new DeleteCommand(INDEX_FIRST_PERSON, true));
    }

    @Test
    public void parse_forceOptionWrongPositionOrCase_throwsParseException() {
        assertParseFailure(parser, "1 -f", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-F 1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_validArgsWithWhitespace_returnsDeleteCommand() {
        assertParseSuccess(parser, "   1   ", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_nonPositiveIndex_throwsIndexError() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-f 0", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-f -1", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_nonWholeNumber_throwsParseException() {
        assertParseFailure(parser, "3.5", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "abc", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "Chloe Tan", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraArgs_throwsParseException() {
        assertParseFailure(parser, "3 extra", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_integerTooBigForInt_throwsIndexError() {
        assertParseFailure(parser, "99999999999", MESSAGE_INVALID_INDEX);
    }
}
