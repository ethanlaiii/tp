package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SCHOOL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command implements ConfirmationCommand {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student to TuteeBook3.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + PREFIX_SCHOOL + "SCHOOL "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + PREFIX_LEVEL + "LEVEL "
            + PREFIX_SUBJECT + "SUBJECT " + PREFIX_BAND + "BAND "
            + "[" + PREFIX_SUBJECT + "SUBJECT " + PREFIX_BAND + "BAND]... "
            + "[" + PREFIX_GUARDIAN_NAME + "GUARDIAN_NAME " + PREFIX_GUARDIAN_PHONE + "GUARDIAN_PHONE] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_SCHOOL + "Victoria School "
            + PREFIX_ADDRESS + "123 Bedok North Road "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_LEVEL + "S3 "
            + PREFIX_SUBJECT + "Mathematics " + PREFIX_BAND + "G3 "
            + PREFIX_GUARDIAN_NAME + "Mary Doe "
            + PREFIX_GUARDIAN_PHONE + "91234567 "
            + PREFIX_TAG + "exams";

    public static final String MESSAGE_SUCCESS = "New student added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A student named %1$s at %2$s, level %3$s already exists. If this is a different student, "
                    + "enter a distinguishing name, for example Chloe Tan 2.";
    public static final String MESSAGE_CONFIRMATION =
            "Student not added yet: %1$s is also recorded at %2$s.";

    public static final String MESSAGE_CONFIRMATION_INSTRUCTION = "Type CONFIRM (uppercase) to add anyway.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public boolean isForced() {
        return false;
    }

    @Override
    public String getCommandWord() {
        return COMMAND_WORD;
    }

    @Override
    public String getConfirmationInstruction() {
        return MESSAGE_CONFIRMATION_INSTRUCTION;
    }

    @Override
    public String getConfirmationMessage(Model model) throws CommandException {
        requireNonNull(model);
        ensureNoExactDuplicate(model, toAdd);
        List<Person> sameNamePersons = model.getAddressBook().getPersonList().stream()
                .filter(toAdd::hasSameName)
                .toList();
        if (sameNamePersons.isEmpty()) {
            return null;
        }
        String existingStudents = formatMatchingStudents(sameNamePersons);
        return String.format(MESSAGE_CONFIRMATION, toAdd.getName(), existingStudents);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        ensureNoExactDuplicate(model, toAdd);

        model.addPerson(toAdd);
        Index addedPersonIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        CommandResult result = CommandResult.showStudent(
                String.format(MESSAGE_SUCCESS, Messages.format(toAdd)), toAdd, addedPersonIndex);
        return result.withWarnings(
                Messages.getNonSingaporePhoneWarning(toAdd.getPhone(), toAdd.getGuardian().map(Guardian::getPhone)),
                Messages.getEmailNoDomainDotWarning(toAdd.getEmail()));
    }

    private static void ensureNoExactDuplicate(Model model, Person person) throws CommandException {
        boolean hasDuplicate = model.getAddressBook().getPersonList().stream()
                .anyMatch(person::hasSameStudentKey);
        if (hasDuplicate) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, person.getName(),
                    person.getSchool(), person.getLevel()));
        }
    }

    private static String formatMatchingStudents(List<Person> persons) {
        return persons.stream()
                .map(person -> person.getSchool() + " (" + person.getLevel() + ")")
                .distinct()
                .collect(Collectors.joining("; "));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
