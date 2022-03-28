package de.presti.ree6.commands.exceptions;

import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;

import java.io.InvalidClassException;

public class CommandInitializerException extends InvalidClassException {

    public CommandInitializerException(Class<?> commandClass) {
        super(commandClass == null ? "Class is null!" : !commandClass.isInstance(ICommand.class) ? "Does not implement the ICommand Interface." : !commandClass.isAnnotationPresent(Command.class) ? "Command Annotation is not present." : commandClass.getAnnotation(Command.class).category() == null ? "It is not allowed to use NULL as Category!" : "Unknown Error!");
    }
}
