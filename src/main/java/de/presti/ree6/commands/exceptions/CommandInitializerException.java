package de.presti.ree6.commands.exceptions;

import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.ICommand;

import java.io.InvalidClassException;

public class CommandInitializerException extends InvalidClassException {

    public CommandInitializerException(Class<?> commandClass) {
        super(commandClass == null ? "Class is null!" : !commandClass.isInstance(ICommand.class) ? "Does not implement the ICommand Interface." : !commandClass.isAnnotationPresent(Command.class) ? "Command Annotation is not present." : "Unknown Error!");
    }
}
