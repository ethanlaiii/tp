package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command implements ConfirmationCommand {

    public static final String COMMAND_WORD = "clear";
    public static final String FORCE_OPTION = "-f";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Clears all students from TuteeBook3 after confirmation, unless " + FORCE_OPTION
            + " is provided.\n"
            + "Parameters: [" + FORCE_OPTION + "]\n"
            + "Examples:\n"
            + COMMAND_WORD + " (asks for confirmation)\n"
            + COMMAND_WORD + " " + FORCE_OPTION + " (clears immediately without confirmation)";

    public static final String MESSAGE_CONFIRMATION = "This will delete all students and notes.";
    public static final String MESSAGE_CONFIRMATION_INSTRUCTION =
            "Type CONFIRM (uppercase) to clear all students and notes.";
    public static final String MESSAGE_EMPTY = "TuteeBook3 is already empty.";
    public static final String MESSAGE_SUCCESS = "TuteeBook3 has been cleared!";

    private final boolean isForced;

    /**
     * Creates a {@code ClearCommand} that requires confirmation.
     */
    public ClearCommand() {
        this(false);
    }

    /**
     * Creates a {@code ClearCommand} that bypasses confirmation when {@code isForced} is true.
     */
    public ClearCommand(boolean isForced) {
        this.isForced = isForced;
    }

    @Override
    public boolean isForced() {
        return isForced;
    }

    @Override
    public ConfirmationCommand withoutForce() {
        return new ClearCommand();
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
    public String getConfirmationMessage(Model model) {
        requireNonNull(model);
        return model.getAddressBook().getPersonList().isEmpty() ? null : MESSAGE_CONFIRMATION;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (model.getAddressBook().getPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        model.setAddressBook(new AddressBook());
        return CommandResult.showList(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ClearCommand otherClearCommand)) {
            return false;
        }

        return isForced == otherClearCommand.isForced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isForced", isForced)
                .toString();
    }
}
