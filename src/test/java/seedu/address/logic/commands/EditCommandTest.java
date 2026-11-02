package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GUARDIAN_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SCHOOL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.KEYWORD_MATCHING_MEIER;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Band;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.predicate.NameContainsKeywordsPredicate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_schoolOnly_preservesOtherFields() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withSchool("Victoria School").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withSchool("Victoria School").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_sameName_warnsAndKeepsModelUnchanged() {
        Person original = model.getFilteredPersonList().get(0);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(original.getName().toString()).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expectedMessage = String.format(EditCommand.MESSAGE_NO_CHANGES, original.getName(),
                original.getSchool(), original.getLevel());
        assertCommandSuccess(command, model,
                CommandResult.showStudentWarning(expectedMessage, original, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_sameNameAsAnotherStudentNoChanges_messageNamesSchoolAndLevel() {
        Person original = model.getFilteredPersonList().get(0);
        model.addPerson(new PersonBuilder(original).withSchool("Victoria School").withLevel("S3").build());
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(original.getName().toString()).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expectedMessage = "No changes made: Alice Pauline at Maplewood Secondary School (S1) "
                + "already has these details.";
        assertCommandSuccess(command, model,
                CommandResult.showStudentWarning(expectedMessage, original, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_removePhoneWhenNoPhone_warnsAndKeepsModelUnchanged() {
        Person original = model.getFilteredPersonList().get(0);
        Person withoutPhone = new PersonBuilder(original).withoutPhone().build();
        model.setPerson(original, withoutPhone);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhoneRemoved().build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expectedMessage = String.format(EditCommand.MESSAGE_NO_CHANGES, withoutPhone.getName(),
                withoutPhone.getSchool(), withoutPhone.getLevel());
        assertCommandSuccess(command, model,
                CommandResult.showStudentWarning(expectedMessage, withoutPhone, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_nameCaseChangedOnly_editsStudent() {
        Person original = model.getFilteredPersonList().get(0);
        String upperCaseName = original.getName().toString().toUpperCase();
        Person edited = new PersonBuilder(original).withName(upperCaseName).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(upperCaseName).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_schoolCaseOnlyChange_success() {
        Person original = model.getFilteredPersonList().get(0);
        String upperCaseSchool = original.getSchool().toString().toUpperCase();
        Person edited = new PersonBuilder(original).withSchool(upperCaseSchool).build();
        assertCaseOnlyEditSaved(original, edited,
                new EditPersonDescriptorBuilder().withSchool(upperCaseSchool).build());
    }

    @Test
    public void execute_subjectCaseOnlyChange_success() {
        Person original = model.getFilteredPersonList().get(0);
        Subject upperCaseSubject = new Subject("MATHEMATICS", Band.G1);
        Person edited = new PersonBuilder(original).withSubjects(upperCaseSubject).build();
        assertCaseOnlyEditSaved(original, edited,
                new EditPersonDescriptorBuilder().withSubjects(upperCaseSubject).build());
    }

    @Test
    public void execute_tagCaseOnlyChange_success() {
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person original = new PersonBuilder(firstPerson).withTags("paid").build();
        model.setPerson(firstPerson, original);
        Person edited = new PersonBuilder(original).withTags("PAID").build();
        assertCaseOnlyEditSaved(original, edited, new EditPersonDescriptorBuilder().withTags("PAID").build());
    }

    /**
     * Asserts that editing the first student, {@code original}, with {@code descriptor} saves {@code edited},
     * which differs from {@code original} only in capitalization.
     */
    private void assertCaseOnlyEditSaved(Person original, Person edited, EditPersonDescriptor descriptor) {
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
        // Person#equals ignores case in these fields, so check the saved text directly
        assertEquals(Messages.format(edited), Messages.format(model.getFilteredPersonList().get(0)));
    }

    @Test
    public void execute_addressSetAndRemoved_updatesStudent() {
        Person original = model.getFilteredPersonList().get(0);
        Person withAddress = new PersonBuilder(original).withAddress("12 Example Road").build();
        EditCommand setAddress = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withAddress("12 Example Road").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, withAddress);
        assertCommandSuccess(setAddress, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(withAddress)), withAddress,
                INDEX_FIRST_PERSON), expectedModel);

        EditCommand removeAddress = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withAddressRemoved().build());
        expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withAddress, original);
        assertCommandSuccess(removeAddress, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(original)), original, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_emailSetAndRemoved_updatesStudent() {
        Person original = model.getFilteredPersonList().get(0);
        Person withEmail = new PersonBuilder(original).withEmail("alice@example.com").build();
        EditCommand setEmail = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("alice@example.com").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, withEmail);
        assertCommandSuccess(setEmail, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(withEmail)), withEmail,
                INDEX_FIRST_PERSON), expectedModel);

        // removing the email keeps every other field
        EditCommand removeEmail = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmailRemoved().build());
        expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withEmail, original);
        assertCommandSuccess(removeEmail, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(original)), original, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_tagsReplacedAndCleared_updatesStudent() {
        Person original = model.getFilteredPersonList().get(0);
        Person withTags = new PersonBuilder(original).withTags("paid", "exams").build();
        EditCommand replaceTags = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withTags("paid", "exams").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, withTags);
        assertCommandSuccess(replaceTags, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(withTags)), withTags,
                INDEX_FIRST_PERSON), expectedModel);

        // clearing the tags keeps every other field
        EditCommand clearTags = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withTags().build());
        expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withTags, original);
        assertCommandSuccess(clearTags, model, CommandResult.showStudent(String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(original)), original, INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_personWithNotes_keepsNotes() {
        Person original = model.getFilteredPersonList().get(0);
        Person personWithNotes = new PersonBuilder(original)
                .withNotes("Weak in algebra", "WA2 Math: 36/50").build();
        model.setPerson(original, personWithNotes);
        Person edited = new PersonBuilder(personWithNotes).withPhone(VALID_PHONE_BOB).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithNotes, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_levelOnly_preservesOtherFields() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withLevel("S5").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withLevel("S5").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_phoneRemoved_removesOnlyPhone() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withoutPhone().build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhoneRemoved().build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_nonSingaporePhone_successWithWarning() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withPhone("+447911123456").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("+447911123456").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        CommandResult expectedResult = CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON)
                .withWarning(String.format(Messages.MESSAGE_NOT_SINGAPORE_PHONE, "phone +447911123456"));
        assertCommandSuccess(command, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_nonSingaporeGuardianPhone_successWithWarning() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original)
                .withGuardian(VALID_GUARDIAN_NAME_AMY, "+60123456789").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withGuardianName(VALID_GUARDIAN_NAME_AMY).withGuardianPhone("+60123456789").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        CommandResult expectedResult = CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON)
                .withWarning(String.format(Messages.MESSAGE_NOT_SINGAPORE_PHONE,
                        "guardian phone +60123456789"));
        assertCommandSuccess(command, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_bothPhonesNotSingapore_successWithOneWarning() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withPhone("+447911123456")
                .withGuardian(VALID_GUARDIAN_NAME_AMY, "+60123456789").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone("+447911123456").withGuardianName(VALID_GUARDIAN_NAME_AMY)
                .withGuardianPhone("+60123456789").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        CommandResult expectedResult = CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON)
                .withWarning(
                String.format(Messages.MESSAGE_NOT_SINGAPORE_PHONES,
                        "phone +447911123456 and guardian phone +60123456789"));
        assertCommandSuccess(command, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_otherFieldWithExistingNonSingaporePhone_noWarning() {
        Person original = model.getFilteredPersonList().get(0);
        Person withOverseasPhone = new PersonBuilder(original).withPhone("+447911123456").build();
        model.setPerson(original, withOverseasPhone);
        Person edited = new PersonBuilder(withOverseasPhone).withSchool("Victoria School").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withSchool("Victoria School").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withOverseasPhone, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_emailWithoutDomainDot_successWithWarning() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withEmail("john@gmail").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmail("john@gmail").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        CommandResult expectedResult = CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON)
                .withWarning(String.format(Messages.MESSAGE_EMAIL_NO_DOMAIN_DOT, "john@gmail"));
        assertCommandSuccess(command, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_otherFieldWithExistingEmailWithoutDomainDot_noWarning() {
        Person withDotlessEmail = giveFirstPersonEmail("john@gmail");
        Person edited = new PersonBuilder(withDotlessEmail).withSchool("Victoria School").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withSchool("Victoria School").build());
        assertEditSuccessWithoutWarning(command, withDotlessEmail, edited);
    }

    @Test
    public void execute_removeEmailWithoutDomainDot_noWarning() {
        Person withDotlessEmail = giveFirstPersonEmail("john@gmail");
        Person edited = new PersonBuilder(withDotlessEmail).withoutEmail().build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withEmailRemoved().build());
        assertEditSuccessWithoutWarning(command, withDotlessEmail, edited);
    }

    /**
     * Replaces the first student in {@code model} with a copy that has {@code email}, and returns the copy.
     */
    private Person giveFirstPersonEmail(String email) {
        Person original = model.getFilteredPersonList().get(0);
        Person withEmail = new PersonBuilder(original).withEmail(email).build();
        model.setPerson(original, withEmail);
        return withEmail;
    }

    /**
     * Checks that {@code command} replaces {@code original} with {@code edited} and shows no warning.
     */
    private void assertEditSuccessWithoutWarning(EditCommand command, Person original, Person edited) {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_bothGuardianFieldsNoGuardian_addsGuardian() {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original)
                .withGuardian(VALID_GUARDIAN_NAME_AMY, VALID_GUARDIAN_PHONE_AMY).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withGuardianName(VALID_GUARDIAN_NAME_AMY)
                .withGuardianPhone(VALID_GUARDIAN_PHONE_AMY).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_oneGuardianFieldNoGuardian_failure() {
        EditCommand nameOnly = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianName(VALID_GUARDIAN_NAME_AMY).build());
        assertCommandFailure(nameOnly, model, EditCommand.MESSAGE_GUARDIAN_INCOMPLETE);

        EditCommand phoneOnly = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianPhone(VALID_GUARDIAN_PHONE_AMY).build());
        assertCommandFailure(phoneOnly, model, EditCommand.MESSAGE_GUARDIAN_INCOMPLETE);
    }

    @Test
    public void execute_oneGuardianFieldExistingGuardian_updatesOnlyThatField() {
        Person withGuardian = addGuardianToFirstPerson();

        Person edited = new PersonBuilder(withGuardian)
                .withGuardian(VALID_GUARDIAN_NAME_BOB, VALID_GUARDIAN_PHONE_AMY).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianName(VALID_GUARDIAN_NAME_BOB).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withGuardian, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);

        Person editedAgain = new PersonBuilder(edited)
                .withGuardian(VALID_GUARDIAN_NAME_BOB, VALID_GUARDIAN_PHONE_BOB).build();
        command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianPhone(VALID_GUARDIAN_PHONE_BOB).build());
        expectedModel.setPerson(edited, editedAgain);
        expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedAgain));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, editedAgain, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_guardianRemoved_removesOnlyGuardian() {
        Person withGuardian = addGuardianToFirstPerson();
        Person edited = new PersonBuilder(withGuardian).withoutGuardian().build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianRemoved().build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withGuardian, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_guardianRemovedNoGuardian_failure() {
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianRemoved().build());
        assertCommandFailure(command, model, EditCommand.MESSAGE_NO_GUARDIAN_TO_REMOVE);
    }

    @Test
    public void execute_otherFieldsAndGuardianRemovedNoGuardian_failure() {
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).withGuardianRemoved().build());
        assertCommandFailure(command, model, EditCommand.MESSAGE_NO_GUARDIAN_TO_REMOVE);
    }

    @Test
    public void execute_otherFieldExistingGuardian_keepsGuardian() {
        Person withGuardian = addGuardianToFirstPerson();
        Person edited = new PersonBuilder(withGuardian).withSchool(VALID_SCHOOL_BOB).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withSchool(VALID_SCHOOL_BOB).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withGuardian, edited);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(edited));
        assertCommandSuccess(command, model,
                CommandResult.showStudent(expectedMessage, edited, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().withSubjects(new Subject("English", Band.G2)).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withSchool(VALID_SCHOOL_BOB).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withSchool(VALID_SCHOOL_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model,
                CommandResult.showStudent(expectedMessage, editedPerson, indexLastPerson), expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_warnsNoChanges() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_NO_CHANGES, editedPerson.getName(),
                editedPerson.getSchool(), editedPerson.getLevel());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model,
                CommandResult.showStudentWarning(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_filteredList_keepsFilter() {
        NameContainsKeywordsPredicate meierPredicate =
                new NameContainsKeywordsPredicate(List.of(KEYWORD_MATCHING_MEIER));
        model.updateFilteredPersonList(meierPredicate);
        Person editedPerson = new PersonBuilder(BENSON).withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON, editedPerson);
        expectedModel.updateFilteredPersonList(meierPredicate);

        assertCommandSuccess(editCommand, model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedPerson, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_editedPersonNoLongerMatchesFilter_staysAtSameIndex() {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(KEYWORD_MATCHING_MEIER)));
        Person editedPerson = new PersonBuilder(BENSON).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson))
                + "\n" + String.format(EditCommand.MESSAGE_NO_LONGER_MATCHES, VALID_NAME_BOB,
                        BENSON.getSchool(), BENSON.getLevel());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON, editedPerson);
        expectedModel.updateFilteredPersonList(
                person -> person.equals(editedPerson) || person.equals(DANIEL));

        assertCommandSuccess(editCommand, model,
                CommandResult.showStudent(expectedMessage, editedPerson, INDEX_FIRST_PERSON), expectedModel);
        assertEquals(List.of(editedPerson, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_editedPersonNoLongerMatchesFilter_messageNamesSchoolAndLevel() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(KEYWORD_MATCHING_MEIER)));
        CommandResult result = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build())
                .execute(model);

        String[] lines = result.getFeedbackToUser().split("\n");
        assertEquals("Bob Choo at Northvale Primary School (S2) no longer matches your last search, "
                + "but stays in the list until you enter list or filter.", lines[lines.length - 1]);
    }

    @Test
    public void execute_noLongerMatchesFilterThenAddNote_staysAtSameIndex() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(KEYWORD_MATCHING_MEIER)));
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build())
                .execute(model);
        new AddNoteCommand(INDEX_FIRST_PERSON, new Note("Weak in algebra")).execute(model);

        Person editedPerson = new PersonBuilder(BENSON).withName(VALID_NAME_BOB)
                .withNotes("Weak in algebra").build();
        assertEquals(List.of(editedPerson, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_noLongerMatchesFilterAndNonSingaporePhone_showsNoteThenWarning() {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(KEYWORD_MATCHING_MEIER)));
        Person editedPerson = new PersonBuilder(BENSON).withName(VALID_NAME_BOB)
                .withPhone("+447911123456").build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_BOB).withPhone("+447911123456").build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson))
                + "\n" + String.format(EditCommand.MESSAGE_NO_LONGER_MATCHES, VALID_NAME_BOB,
                        BENSON.getSchool(), BENSON.getLevel());
        CommandResult expectedResult = CommandResult.showStudent(
                expectedMessage, editedPerson, INDEX_FIRST_PERSON)
                .withWarning(String.format(Messages.MESSAGE_NOT_SINGAPORE_PHONE, "phone +447911123456"));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON, editedPerson);
        expectedModel.updateFilteredPersonList(
                person -> person.equals(editedPerson) || person.equals(DANIEL));

        assertCommandSuccess(editCommand, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, String.format(EditCommand.MESSAGE_DUPLICATE_PERSON,
                firstPerson.getName(), firstPerson.getSchool(), firstPerson.getLevel()));
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, String.format(EditCommand.MESSAGE_DUPLICATE_PERSON,
                personInList.getName(), personInList.getSchool(), personInList.getLevel()));
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    /**
     * Replaces the first person in {@code model} with a copy that has a guardian, and returns the copy.
     */
    private Person addGuardianToFirstPerson() {
        Person original = model.getFilteredPersonList().get(0);
        Person withGuardian = new PersonBuilder(original)
                .withGuardian(VALID_GUARDIAN_NAME_AMY, VALID_GUARDIAN_PHONE_AMY).build();
        model.setPerson(original, withGuardian);
        return withGuardian;
    }

}
