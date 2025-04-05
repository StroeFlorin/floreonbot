package dev.stroe.floreonbot.command;

public class CommandWrapper implements Command {
    private final Command wrappedCommand;
    private final String commandName;
    
    public CommandWrapper(Command wrappedCommand, String commandName) {
        this.wrappedCommand = wrappedCommand;
        this.commandName = commandName;
    }
    
    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        wrappedCommand.execute(this.commandName, text, chatId, userId, messageId);
    }
    
    @Override
    public String getDescription() {
        if (wrappedCommand instanceof WeatherForecastCommand) {
            return ((WeatherForecastCommand) wrappedCommand).getDescriptionForCommand(commandName);
        }
        return wrappedCommand.getDescription();
    }
}
