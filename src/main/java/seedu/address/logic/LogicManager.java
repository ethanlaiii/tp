package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ConfirmationCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final ConfirmationManager confirmationManager = new ConfirmationManager();

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        if (confirmationManager.hasPendingConfirmation()) {
            ConfirmationCommand pendingCommand = confirmationManager.clearPending().orElseThrow();
            if (ConfirmationManager.isConfirmInput(commandText)) {
                ConfirmationCommand confirmedCommand = pendingCommand;
                return executeAndSave(confirmedCommand.execute(model));
            }
            String cancellationNotice = confirmationManager.getCancellationNotice(pendingCommand);
            try {
                Command command = addressBookParser.parseCommand(commandText);
                CommandResult result = executeCommand(command);
                return isRepeatOf(pendingCommand, command)
                        ? result
                        : result.withCancellationNotice(cancellationNotice);
            } catch (CommandException e) {
                throw new CommandException(cancellationNotice + System.lineSeparator() + e.getMessage(), e);
            } catch (ParseException e) {
                throw new ParseException(cancellationNotice + System.lineSeparator() + e.getMessage(), e);
            }
        }

        if (ConfirmationManager.isConfirmInput(commandText)) {
            logger.info("CONFIRM entered with no command waiting for confirmation");
            throw new ParseException(ConfirmationManager.MESSAGE_NOTHING_TO_CONFIRM);
        }

        Command command = addressBookParser.parseCommand(commandText);
        return executeCommand(command);
    }

    private CommandResult executeCommand(Command command) throws CommandException, ParseException {
        if (command instanceof ConfirmationCommand confirmableCommand) {
            String confirmationMessage = confirmableCommand.getConfirmationMessage(model);
            if (!confirmableCommand.isForced() && confirmationMessage != null) {
                return confirmationManager.requestConfirmation(confirmableCommand, confirmationMessage);
            }
            if (confirmableCommand.isForced()) {
                return executeAndSave(confirmableCommand.execute(model));
            }
        }

        return executeAndSave(command.execute(model));
    }

    /**
     * Returns whether {@code command} repeats {@code pendingCommand}, either exactly or in its forced form,
     * so it replaces the pending request instead of cancelling it.
     */
    private static boolean isRepeatOf(ConfirmationCommand pendingCommand, Command command) {
        return command instanceof ConfirmationCommand newCommand
                && pendingCommand.equals(newCommand.withoutForce());
    }

    private CommandResult executeAndSave(CommandResult commandResult) throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
