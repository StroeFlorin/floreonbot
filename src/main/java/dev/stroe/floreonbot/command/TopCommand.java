package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.service.DailySummarySchedulerService;

@Component
public class TopCommand implements Command{
private final DailySummarySchedulerService dailySummarySchedulerService;

    public TopCommand (DailySummarySchedulerService dailySummarySchedulerService) {
        this.dailySummarySchedulerService = dailySummarySchedulerService;
    }

    @Override
    public void execute(String commandName, String text, Long chatId, Long userId, Long messageId) {
        dailySummarySchedulerService.sendDailySummary();
    }

    @Override
    public String getDescription() {
        return "Get the top 3 most active chatters of the day.";
    }

}
