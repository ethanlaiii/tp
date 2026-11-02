package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddNoteCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.EditNoteCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.predicate.AddressContainsTextPredicate;
import seedu.address.model.person.predicate.NameContainsKeywordsPredicate;
import seedu.address.model.person.predicate.StudentMatchesAllPredicate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private static final String NON_BREAKING_SPACE = " ";
    private static final String ZERO_WIDTH_SPACE = "​";

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertEquals(new ClearCommand(), parser.parseCommand(ClearCommand.COMMAND_WORD));
        assertEquals(new ClearCommand(), parser.parseCommand(ClearCommand.COMMAND_WORD + "   "));
    }

    @Test
    public void parseCommand_forceOption_returnsForcedClearCommand() throws Exception {
        assertEquals(new ClearCommand(true),
                parser.parseCommand(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION));
        assertEquals(new ClearCommand(true),
                parser.parseCommand(ClearCommand.COMMAND_WORD + "   " + ClearCommand.FORCE_OPTION + "   "));
    }

    @Test
    public void parseCommand_clearWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ClearCommand.COMMAND_WORD + " additional text"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ClearCommand.COMMAND_WORD + " " + ClearCommand.FORCE_OPTION
                        + " " + ClearCommand.FORCE_OPTION));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ClearCommand.COMMAND_WORD + " --force"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_deleteNote() throws Exception {
        DeleteNoteCommand command = (DeleteNoteCommand) parser.parseCommand(DeleteNoteCommand.COMMAND_WORD
                + " " + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_NID + "2");
        assertEquals(new DeleteNoteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)), command);
    }

    @Test
    public void parseCommand_editNote() throws Exception {
        EditNoteCommand command = (EditNoteCommand) parser.parseCommand(EditNoteCommand.COMMAND_WORD
                + " " + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_NID + "2 " + PREFIX_NOTE
                + "Improving in algebra");
        assertEquals(new EditNoteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2),
                new Note("Improving in algebra")), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + "   ") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_exitWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ExitCommand.COMMAND_WORD + " additional text"));
    }

    @Test
    public void parseCommand_filter() throws Exception {
        FilterCommand command = (FilterCommand) parser.parseCommand(
                FilterCommand.COMMAND_WORD + " " + PREFIX_NAME + "foo bar");
        StudentMatchesAllPredicate expectedPredicate = new StudentMatchesAllPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("foo bar"))));
        assertEquals(new FilterCommand(expectedPredicate), command);
    }

    @Test
    public void parseCommand_find_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("find n/avi"));
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + "   ") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_helpWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(HelpCommand.COMMAND_WORD + " additional text"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ListCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, () ->
                parser.parseCommand(ListCommand.COMMAND_WORD + " additional text"));
    }

    @Test
    public void parseCommand_view() throws Exception {
        ViewCommand command = (ViewCommand) parser.parseCommand(
                ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_nonBreakingSpaceBeforePrefix_readsPrefix() throws Exception {
        Person person = new PersonBuilder().build();
        String userInput = PersonUtil.getAddCommand(person)
                .replace(" " + PREFIX_PHONE, NON_BREAKING_SPACE + PREFIX_PHONE);
        assertEquals(new AddCommand(person), parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_zeroWidthSpaceInAddress_removed() throws Exception {
        Person person = new PersonBuilder().withAddress("Bedok North").build();
        String userInput = PersonUtil.getAddCommand(person).replace("Bedok", "Be" + ZERO_WIDTH_SPACE + "dok");
        assertEquals(new AddCommand(person), parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_noteOfNonBreakingSpaces_throwsParseException() {
        String userInput = AddNoteCommand.COMMAND_WORD + " 1 " + PREFIX_NOTE + NON_BREAKING_SPACE.repeat(3);
        assertThrows(ParseException.class, Note.MESSAGE_BLANK, () -> parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_addressOfNonBreakingSpace_throwsParseException() {
        Person person = new PersonBuilder().withoutAddress().build();
        String userInput = PersonUtil.getAddCommand(person) + " " + PREFIX_ADDRESS + NON_BREAKING_SPACE;
        assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_curlyQuotesInAddress_savedAsStraight() throws Exception {
        Person person = new PersonBuilder().withAddress("Blk 5 'Sunrise' \"Court\"").build();
        String userInput = PersonUtil.getAddCommand(person)
                .replace("'Sunrise' \"Court\"", "‘Sunrise’ “Court”");
        assertEquals(new AddCommand(person), parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_curlyApostropheInSchool_matchesStraightApostrophe() throws Exception {
        Person person = new PersonBuilder().withSchool("St. Joseph's Institution").build();
        String userInput = PersonUtil.getAddCommand(person).replace("Joseph's", "Joseph’s");
        assertEquals(new AddCommand(person), parser.parseCommand(userInput));
    }

    @Test
    public void parseCommand_longDashInPhone_readAsHyphen() throws Exception {
        Person person = new PersonBuilder().withPhone("91234567").build();
        String addCommand = PersonUtil.getAddCommand(person);
        assertEquals(new AddCommand(person), parser.parseCommand(addCommand.replace("91234567", "9123–4567")));
        assertEquals(new AddCommand(person), parser.parseCommand(addCommand.replace("91234567", "9123—4567")));
    }

    @Test
    public void parseCommand_filterWithLongDash_searchesForHyphen() throws Exception {
        FilterCommand command = (FilterCommand) parser.parseCommand(
                FilterCommand.COMMAND_WORD + " " + PREFIX_ADDRESS + "1–3");
        StudentMatchesAllPredicate expectedPredicate = new StudentMatchesAllPredicate(
                List.of(new AddressContainsTextPredicate("1-3")));
        assertEquals(new FilterCommand(expectedPredicate), command);
    }

    @Test
    public void parseCommand_uppercaseCommandWord_parsesSameAsLowercase() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        List<String> inputs = List.of(
                PersonUtil.getAddCommand(person),
                AddNoteCommand.COMMAND_WORD + " 1 " + PREFIX_NOTE + "hi",
                ClearCommand.COMMAND_WORD,
                DeleteCommand.COMMAND_WORD + " 1",
                DeleteNoteCommand.COMMAND_WORD + " 1 " + PREFIX_NID + "2",
                EditCommand.COMMAND_WORD + " 1 " + PersonUtil.getEditPersonDescriptorDetails(descriptor),
                EditNoteCommand.COMMAND_WORD + " 1 " + PREFIX_NID + "2 " + PREFIX_NOTE + "hi",
                FilterCommand.COMMAND_WORD + " " + PREFIX_NAME + "foo",
                ViewCommand.COMMAND_WORD + " 1");
        for (String input : inputs) {
            assertEquals(parser.parseCommand(input), parser.parseCommand(withUppercaseCommandWord(input)));
        }

        // These commands have no equals method, so only their type is checked.
        assertTrue(parser.parseCommand(withUppercaseCommandWord(ListCommand.COMMAND_WORD)) instanceof ListCommand);
        assertTrue(parser.parseCommand(withUppercaseCommandWord(HelpCommand.COMMAND_WORD)) instanceof HelpCommand);
        assertTrue(parser.parseCommand(withUppercaseCommandWord(ExitCommand.COMMAND_WORD)) instanceof ExitCommand);
    }

    @Test
    public void parseCommand_capitalizedCommandWord_returnsCommand() throws Exception {
        assertEquals(new AddNoteCommand(INDEX_FIRST_PERSON, new Note("hi")),
                parser.parseCommand("Addnote 1 " + PREFIX_NOTE + "hi"));
    }

    @Test
    public void parseCommand_mixedCaseCommandWord_returnsCommand() throws Exception {
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("dElEtE 1"));
    }

    @Test
    public void parseCommand_uppercaseCommandWord_keepsArgumentCase() throws Exception {
        assertEquals(new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Hi THERE")),
                parser.parseCommand("ADDNOTE 1 " + PREFIX_NOTE + "Hi THERE"));
    }

    @Test
    public void parseCommand_uppercaseCommandWordInvalidArgument_throwsCommandSpecificParseException() {
        assertThrows(ParseException.class, DeleteCommand.MESSAGE_INVALID_FORMAT, () ->
                parser.parseCommand("DELETE abc"));
    }

    @Test
    public void parseCommand_uppercaseUnknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("FIND n/avi"));
    }

    /**
     * Returns {@code input} with its command word in uppercase and its arguments unchanged.
     */
    private static String withUppercaseCommandWord(String input) {
        String[] parts = input.split(" ", 2);
        String commandWord = parts[0].toUpperCase(Locale.ROOT);
        return parts.length == 1 ? commandWord : commandWord + " " + parts[1];
    }
}
