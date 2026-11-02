package seedu.address.logic;

import java.util.Optional;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ConfirmationCommand;

/**
 * Manages a pending command that requires the user to confirm or cancel it.
 */
public class ConfirmationManager {
    public static final String CONFIRM_WORD = "CONFIRM";
    public static final String MESSAGE_CONFIRMATION_CANCELLATION =
            "Any other non-blank input cancels this request and runs as a command.";
    public static final String MESSAGE_CONFIRMATION_SUFFIX = System.lineSeparator()
            + "Type CONFIRM (uppercase) to proceed."
            + System.lineSeparator() + MESSAGE_CONFIRMATION_CANCELLATION;
    public static final String MESSAGE_CANCELLED_FORMAT = "The earlier '%s' command has been cancelled.";
    public static final String MESSAGE_NOTHING_TO_CONFIRM =
            "There is no command waiting for confirmation. Nothing was changed.";

    private ConfirmationCommand pendingCommand;

    /**
     * Returns whether {@code input} confirms a pending command: exactly {@value #CONFIRM_WORD},
     * ignoring leading and trailing spaces.
     */
    public static boolean isConfirmInput(String input) {
        return input.trim().equals(CONFIRM_WORD);
    }

    /**
     * Returns whether a command is waiting for confirmation.
     */
    public boolean hasPendingConfirmation() {
        return pendingCommand != null;
    }

    /**
     * Creates an amber confirmation prompt and remembers its command.
     */
    public CommandResult requestConfirmation(ConfirmationCommand command, String message) {
        pendingCommand = command;
        String instruction = command.getConfirmationInstruction();
        String suffix = instruction == null ? MESSAGE_CONFIRMATION_SUFFIX
                : System.lineSeparator() + instruction + System.lineSeparator()
                        + MESSAGE_CONFIRMATION_CANCELLATION;
        return CommandResult.warning(message + suffix);
    }

    /**
     * Clears and returns the pending command, if one exists.
     */
    public Optional<ConfirmationCommand> clearPending() {
        ConfirmationCommand confirmedCommand = pendingCommand;
        pendingCommand = null;
        return Optional.ofNullable(confirmedCommand);
    }

    /**
     * Returns a notice identifying the cancelled command.
     */
    public String getCancellationNotice(ConfirmationCommand command) {
        return String.format(MESSAGE_CANCELLED_FORMAT, command.getCommandWord());
    }
}
