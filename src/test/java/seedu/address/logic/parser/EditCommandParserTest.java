package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.GUARDIAN_NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GUARDIAN_NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.GUARDIAN_PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GUARDIAN_PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_GUARDIAN_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_GUARDIAN_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_GUARDIAN_PHONE_MESSAGE;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_LEVEL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_MESSAGE;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_SCHOOL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.SCHOOL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SCHOOL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_EXAMS;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_PAID;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SCHOOL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_EXAMS;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_PAID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SCHOOL;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Band;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_schoolOnly_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withSchool("St Josephs Secondary School").build();
        assertParseSuccess(parser, "1 sch/  St Josephs Secondary School  ",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_schoolWithUnicode_failure() {
        assertParseFailure(parser, "1 sch/École Française",
                String.format(School.MESSAGE_INVALID_CHARACTER, "École Française", "É"));
        assertParseFailure(parser, "1 sch/李明学校",
                String.format(School.MESSAGE_INVALID_CHARACTER, "李明学校", "李"));
    }

    @Test
    public void parse_schoolWithOfficialPunctuation_success() {
        for (String school : List.of("Anglo-Chinese School (Independent)", "St. Joseph's Institution",
                "School of Science and Technology, Singapore")) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withSchool(school).build();
            assertParseSuccess(parser, "1 sch/" + school, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_nonAsciiName_failure() {
        assertParseFailure(parser, "1 n/Zoë Lim", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_subjectWithOfficialPunctuation_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withSubjects(new Subject("Science (Physics, Chemistry)", Band.G3)).build();
        assertParseSuccess(parser, "1 sub/Science (Physics, Chemistry) band/G3",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressSetOrRemoved_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddress("12 Example Road").build();
        assertParseSuccess(parser, "1 a/12 Example Road", new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withAddressRemoved().build();
        assertParseSuccess(parser, "1 a/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressWithJoinedPrefix_reportsCaseSensitivePrefix() {
        assertParseFailure(parser, "1 a/Blk 1/2P/91234567",
                String.format(Messages.MESSAGE_PREFIX_CASE_SENSITIVE, "P/", "p/"));
    }

    @Test
    public void parse_addressWithOrdinarySlashAndSeparatedPrefix_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Blk 1/2").withPhone(VALID_PHONE_AMY).build();
        assertParseSuccess(parser, "1 a/Blk 1/2 p/" + VALID_PHONE_AMY,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressWithSpaceBeforeSlash_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Blk 5 Ang Mo Kio Town /Central").build();
        assertParseSuccess(parser, "1 a/Blk 5 Ang Mo Kio Town /Central",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_addressWithJoinedLowercasePrefix_reportsMissingSpace() {
        assertParseFailure(parser, "1 a/Blk 1/2p/91234567",
                String.format(Messages.MESSAGE_MISSING_SPACE_BEFORE_PREFIX, "p/"));
    }

    @Test
    public void parse_blankAddressWithSpaces_removesAddress() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddressRemoved().build();
        assertParseSuccess(parser, "1 a/   ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_emailSetOrRemoved_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail("amy@example.com").build();
        assertParseSuccess(parser, "1 e/amy@example.com", new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withEmailRemoved().build();
        assertParseSuccess(parser, "1 e/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 e/   ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser, "1 e/amy", String.format(Email.MESSAGE_MISSING_AT, "amy"));
        assertParseFailure(parser, "1 e/amy@example.com e/bob@example.com",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
    }

    @Test
    public void parse_tagsReplacedOrCleared_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags(VALID_TAG_PAID, VALID_TAG_EXAMS).build();
        assertParseSuccess(parser, "1" + TAG_DESC_PAID + TAG_DESC_EXAMS,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withTags().build();
        assertParseSuccess(parser, "1 t/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 t/   ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidTag_failure() {
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/exam prep", String.format(Messages.MESSAGE_TAG_HAS_SPACE, "exam prep"));
        // an empty t/ only clears the tags when it is the only t/
        assertParseFailure(parser, "1 t/" + TAG_DESC_PAID, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/Paid" + TAG_DESC_PAID,
                String.format(Tag.MESSAGE_DUPLICATE_TAG, VALID_TAG_PAID));
    }

    @Test
    public void parse_invalidSchool_failure() {
        assertParseFailure(parser, "1 sch/  ", School.MESSAGE_BLANK);
        assertParseFailure(parser, "1 sch/School sch/Other School",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SCHOOL));
    }

    @Test
    public void parse_invalidSchoolValues_failure() {
        assertParseFailure(parser, "1 sch/School#1",
                String.format(School.MESSAGE_INVALID_CHARACTER, "School#1", "#"));
        assertParseFailure(parser, "1 sch/123", String.format(School.MESSAGE_MISSING_ASCII_LETTER, "123"));
        assertParseFailure(parser, "1 sch/École Française",
                String.format(School.MESSAGE_INVALID_CHARACTER, "École Française", "É"));
        assertParseFailure(parser, "1 sch/" + "a".repeat(101), String.format(School.MESSAGE_TOO_LONG, 101));
    }

    @Test
    public void parse_schoolWithSupportedDigitsAndPunctuation_success() {
        String[] schools = {
            "Victoria School 2",
            "Anglo-Chinese School (Independent)",
            "St. Joseph's Institution"
        };
        for (String school : schools) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withSchool(school).build();
            assertParseSuccess(parser, "1 sch/" + school, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_INDEX);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);

        // removed prefixes being parsed as preamble
        assertParseFailure(parser, "1 x/Clementi Road", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_integerTooBigForInt_throwsIndexError() {
        assertParseFailure(parser, "99999999999" + NAME_DESC_AMY, MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_prefixWithoutSpace_failure() {
        String hint = Messages.MESSAGE_PREFIX_WITHOUT_SPACE_HINT;
        assertParseFailure(parser, "1 n/John Doep/98765432",
                Name.MESSAGE_CONSTRAINTS + "\n" + hint);
        assertParseFailure(parser, "1 gn/Mary Tangp/91234567",
                Guardian.MESSAGE_NAME_CONSTRAINTS + "\n" + hint);
        assertParseFailure(parser, "1 sub/Englishband/G1",
                Subject.MESSAGE_CONSTRAINTS + "\n" + hint);

        // no space after gn/, so gp/ starts the guardian name
        assertParseFailure(parser, "1 gn/gp/",
                Guardian.MESSAGE_NAME_CONSTRAINTS + "\n" + hint);

        // no space before gn/, so it is read as part of the guardian phone
        String guardianPhoneMessage = String.format(Phone.MESSAGE_INVALID_CHARACTER,
                ParserUtil.GUARDIAN_PHONE_LABEL, "91234567gn/Mary Tan", "g");
        assertParseFailure(parser, "1 gp/91234567gn/Mary Tan",
                guardianPhoneMessage + "\n" + hint);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1 sub/English", Messages.MESSAGE_SUBJECT_BAND_PAIR);
        assertParseFailure(parser, "1 sub/English band/G4", Band.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 sub/Français band/G3", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 a/Rue de l'Église 5", String.format(Address.MESSAGE_INVALID_CHARACTER, "É"));
        assertParseFailure(parser, "1 band/G2", "Missing required parameter: sub/."); // band without subject
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, INVALID_PHONE_MESSAGE); // invalid phone
        assertParseFailure(parser, "1" + INVALID_SCHOOL_DESC, School.MESSAGE_BLANK); // invalid school

        // invalid phone followed by valid school
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + SCHOOL_DESC_AMY, INVALID_PHONE_MESSAGE);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_SCHOOL_DESC + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + SCHOOL_DESC_AMY + NAME_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withSchool(VALID_SCHOOL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + SCHOOL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withSchool(VALID_SCHOOL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        assertParseSuccess(parser, "1 sub/English band/g2", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withSubjects(new Subject("English", Band.G2)).build()));
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // school
        userInput = targetIndex.getOneBased() + SCHOOL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withSchool(VALID_SCHOOL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_levelOnly_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withLevel(VALID_LEVEL_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + LEVEL_DESC_AMY,
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_invalidLevel_failure() {
        assertParseFailure(parser, "1" + INVALID_LEVEL_DESC, Level.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 " + PREFIX_LEVEL + "S9", Level.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyPhone_removesPhone() {
        Index targetIndex = INDEX_FIRST_PERSON;

        // phone only
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhoneRemoved().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_PHONE,
                new EditCommand(targetIndex, descriptor));

        // phone removed together with another field
        descriptor = new EditPersonDescriptorBuilder().withPhoneRemoved().withName(VALID_NAME_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_PHONE + NAME_DESC_AMY,
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_formattedPhones_savesCleanedPhones() {
        Index targetIndex = INDEX_FIRST_PERSON;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("91234567").build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_PHONE + "9123 4567",
                new EditCommand(targetIndex, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withGuardianPhone("81234567").build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GUARDIAN_PHONE + "+65 8123 4567",
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_guardianFields_success() {
        Index targetIndex = INDEX_FIRST_PERSON;

        // guardian name only
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withGuardianName(VALID_GUARDIAN_NAME_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + GUARDIAN_NAME_DESC_AMY,
                new EditCommand(targetIndex, descriptor));

        // guardian phone only
        descriptor = new EditPersonDescriptorBuilder().withGuardianPhone(VALID_GUARDIAN_PHONE_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + GUARDIAN_PHONE_DESC_AMY,
                new EditCommand(targetIndex, descriptor));

        // both, with extra spaces
        descriptor = new EditPersonDescriptorBuilder().withGuardianName(VALID_GUARDIAN_NAME_AMY)
                .withGuardianPhone(VALID_GUARDIAN_PHONE_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GUARDIAN_NAME + "  "
                + VALID_GUARDIAN_NAME_AMY + "  " + PREFIX_GUARDIAN_PHONE + VALID_GUARDIAN_PHONE_AMY + "  ",
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_repeatedSpacesInTextFields_collapsesSpaces() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName("John Doe")
                .withSchool("Victoria School")
                .withSubjects(new Subject("Additional Mathematics", Band.G2))
                .withGuardianName("Mary Jane")
                .build();
        String userInput = "1 n/  John   Doe  sch/  Victoria   School  sub/  Additional   Mathematics  band/G2 "
                + "gn/  Mary   Jane  ";

        assertParseSuccess(parser, userInput, new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_emptyGuardian_removesGuardian() {
        Index targetIndex = INDEX_FIRST_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withGuardianRemoved().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GUARDIAN_NAME + " "
                + PREFIX_GUARDIAN_PHONE, new EditCommand(targetIndex, descriptor));

        // guardian removed together with another field
        descriptor = new EditPersonDescriptorBuilder().withGuardianRemoved().withName(VALID_NAME_AMY).build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GUARDIAN_PHONE + "  "
                + PREFIX_GUARDIAN_NAME + NAME_DESC_AMY, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_guardianPartlyEmpty_failure() {
        String removalMessage = EditCommand.MESSAGE_GUARDIAN_REMOVAL;
        assertParseFailure(parser, "1 " + PREFIX_GUARDIAN_NAME, removalMessage);
        assertParseFailure(parser, "1 " + PREFIX_GUARDIAN_PHONE + "  ", removalMessage);
        assertParseFailure(parser, "1 " + PREFIX_GUARDIAN_NAME + GUARDIAN_PHONE_DESC_AMY, removalMessage);
        assertParseFailure(parser, "1" + GUARDIAN_NAME_DESC_AMY + " " + PREFIX_GUARDIAN_PHONE, removalMessage);
    }

    @Test
    public void parse_invalidGuardian_failure() {
        assertParseFailure(parser, "1" + INVALID_GUARDIAN_NAME_DESC, Guardian.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_GUARDIAN_PHONE_DESC, INVALID_GUARDIAN_PHONE_MESSAGE);
        assertParseFailure(parser, "1" + GUARDIAN_NAME_DESC_AMY + GUARDIAN_NAME_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GUARDIAN_NAME));
        assertParseFailure(parser, "1" + GUARDIAN_PHONE_DESC_AMY + GUARDIAN_PHONE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GUARDIAN_PHONE));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + SCHOOL_DESC_AMY + PHONE_DESC_AMY + SCHOOL_DESC_AMY
                + PHONE_DESC_BOB + SCHOOL_DESC_BOB;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_SCHOOL));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_SCHOOL_DESC
                + INVALID_PHONE_DESC + INVALID_SCHOOL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_SCHOOL));
    }

    @Test
    public void parse_fullWidthDigitIndex_throwsParseException() {
        assertParseFailure(parser, "１" + PHONE_DESC_AMY, MESSAGE_INVALID_FORMAT);
    }
}
