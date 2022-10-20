package de.presti.ree6.commands.exceptions;

import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * Exception class, used for errors while trying to initialize the Commands.
 */
public class CommandInitializerException extends ObjectStreamException {

    /**
     * Serial version ID.
     */
    @java.io.Serial
    private static final long serialVersionUID = -4333316296251054416L;

    /**
     * Name of the invalid class.
     *
     * @serial Name of the invalid class.
     */
    public final String classname;

    /**
     * Constructs an CommandInitializerException object.
     * @param commandClass the class.
     */
    public CommandInitializerException(Class<?> commandClass) {
        this(commandClass != null ? commandClass.getName() : "Null!", commandClass == null ? "Class is null!" : Arrays.stream(commandClass.getInterfaces()).noneMatch(classname -> classname.isInstance(ICommand.class)) ? "Does not implement the ICommand Interface." : !commandClass.isAnnotationPresent(Command.class) ? "Command Annotation is not present." : commandClass.getAnnotation(Command.class).category() == null ? "It is not allowed to use NULL as Category!" : "Unknown Error!");
    }

    /**
     * Constructs an CommandInitializerException object.
     *
     * @param cname   a String naming the invalid class.
     * @param reason  a String describing the reason for the exception.
     */
    public CommandInitializerException(String cname, String reason) {
        super(reason);
        classname = cname;
    }

    /**
     * Produce the message and include the classname, if present.
     */
    @Override
    public String getMessage() {
        if (classname == null)
            return super.getMessage();
        else
            return classname + "; " + super.getMessage();
    }
}
