package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Deletes a note, identified by its NID, from the person identified using their displayed index.
 */
public class DeleteNoteCommand extends Command implements ConfirmationCommand {

    public static final String COMMAND_WORD = "deletenote";
    public static final String FORCE_OPTION = "-f";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the note numbered NID from the student at INDEX.\n"
            + "Parameters: [" + FORCE_OPTION + "] INDEX (must be a positive integer) "
            + "nid/NID (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 nid/2 (asks for confirmation)\n"
            + COMMAND_WORD + " " + FORCE_OPTION + " 1 nid/2 (deletes immediately)";

    public static final String MESSAGE_SUCCESS = "Deleted note %1$d from %2$s at %3$s (%4$s): %5$s";
    public static final String MESSAGE_CONFIRMATION =
            "This will delete note %1$d from %2$s at %3$s (%4$s).";
    public static final String MESSAGE_CONFIRMATION_INSTRUCTION = "Type CONFIRM (uppercase) to delete this note.";
    public static final String MESSAGE_NO_NOTES = "%1$s at %2$s (%3$s) has no notes to delete.";
    public static final String MESSAGE_NID_NOT_FOUND =
            "NID %1$d does not exist. %2$s at %3$s (%4$s) has only %5$d %6$s.";

    private final Index targetIndex;
    private final Index targetNid;
    private final boolean isForced;

    /**
     * Creates a {@code DeleteNoteCommand} to delete note {@code targetNid} of the person at
     * {@code targetIndex}.
     */
    public DeleteNoteCommand(Index targetIndex, Index targetNid) {
        this(targetIndex, targetNid, false);
    }

    /**
     * Creates a {@code DeleteNoteCommand} that bypasses confirmation when {@code isForced} is true.
     */
    public DeleteNoteCommand(Index targetIndex, Index targetNid, boolean isForced) {
        this.targetIndex = targetIndex;
        this.targetNid = targetNid;
        this.isForced = isForced;
    }

    @Override
    public boolean isForced() {
        return isForced;
    }

    @Override
    public ConfirmationCommand withoutForce() {
        return new DeleteNoteCommand(targetIndex, targetNid);
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
        NoteCommandUtil.verifyNoteExists(person, targetNid, MESSAGE_NO_NOTES);
        return String.format(MESSAGE_CONFIRMATION, targetNid.getOneBased(), person.getName(), person.getSchool(),
                person.getLevel());
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person person = getTargetPerson(model);
        NoteCommandUtil.verifyNoteExists(person, targetNid, MESSAGE_NO_NOTES, MESSAGE_NID_NOT_FOUND);
        List<Note> notes = new ArrayList<>(person.getNotes());
        Note deletedNote = notes.remove(targetNid.getZeroBased());
        Person editedPerson = person.copyWithNotes(notes);

        String successMessage = String.format(MESSAGE_SUCCESS, targetNid.getOneBased(),
                editedPerson.getName(), editedPerson.getSchool(), editedPerson.getLevel(), deletedNote);
        String feedback = NoteCommandUtil.setPersonKeepingIndex(model, person, editedPerson, successMessage);
        return CommandResult.showStudent(feedback, editedPerson, targetIndex);
    }

    private Person getTargetPerson(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(targetIndex.getZeroBased());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeleteNoteCommand otherDeleteNoteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteNoteCommand.targetIndex)
                && targetNid.equals(otherDeleteNoteCommand.targetNid)
                && isForced == otherDeleteNoteCommand.isForced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetNid", targetNid)
                .add("isForced", isForced)
                .toString();
    }
}
