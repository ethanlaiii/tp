package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command implements ConfirmationCommand {

    public static final String COMMAND_WORD = "delete";
    public static final String FORCE_OPTION = "-f";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the student identified by the index number used in the displayed student list.\n"
            + "Parameters: [" + FORCE_OPTION + "] INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 (asks for confirmation)\n"
            + COMMAND_WORD + " " + FORCE_OPTION + " 1 (deletes immediately)";

    public static final String MESSAGE_DELETE_STUDENT_SUCCESS = "Deleted student: %1$s at %2$s (%3$s).";
    public static final String MESSAGE_CONFIRMATION =
            "This will delete %1$s at %2$s (%3$s) and all their notes.";
    public static final String MESSAGE_CONFIRMATION_INSTRUCTION =
            "Type CONFIRM (uppercase) to delete this student and all their notes.";
    public static final String MESSAGE_FILTERED_LIST_EMPTY =
            "The filtered list is now empty, so all remaining students are shown.";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid format. Usage: " + COMMAND_WORD
            + " [-f] INDEX";

    public static final String MESSAGE_INVALID_STUDENT_INDEX = Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

    private final Index targetIndex;
    private final boolean isForced;

    /**
     * Creates a DeleteCommand that asks for confirmation.
     */
    public DeleteCommand(Index targetIndex) {
        this(targetIndex, false);
    }

    /**
     * Creates a DeleteCommand that bypasses confirmation when {@code isForced} is true.
     */
    public DeleteCommand(Index targetIndex, boolean isForced) {
        this.targetIndex = targetIndex;
        this.isForced = isForced;
    }

    @Override
    public boolean isForced() {
        return isForced;
    }

    @Override
    public ConfirmationCommand withoutForce() {
        return new DeleteCommand(targetIndex);
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
        Person person = getTargetPerson(model);
        return String.format(MESSAGE_CONFIRMATION, person.getName(), person.getSchool(), person.getLevel());
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToDelete = getTargetPerson(model);
        model.deletePerson(personToDelete);
        String message = String.format(MESSAGE_DELETE_STUDENT_SUCCESS, personToDelete.getName(),
                personToDelete.getSchool(), personToDelete.getLevel());
        boolean hasRemainingStudents = !model.getAddressBook().getPersonList().isEmpty();
        if (hasRemainingStudents && model.getFilteredPersonList().isEmpty()) {
            model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
            message += System.lineSeparator() + MESSAGE_FILTERED_LIST_EMPTY;
        }
        return CommandResult.showList(message);
    }

    private Person getTargetPerson(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_STUDENT_INDEX);
        }
        return lastShownList.get(targetIndex.getZeroBased());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex) && isForced == otherDeleteCommand.isForced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("isForced", isForced)
                .toString();
    }
}
