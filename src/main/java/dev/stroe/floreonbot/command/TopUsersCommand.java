package dev.stroe.floreonbot.command;

import org.springframework.stereotype.Component;

import dev.stroe.floreonbot.service.DailySummarySchedulerService;

@Component
public class TopUsersCommand implements Command {
    DailySummarySchedulerService dailySummarySchedulerService;

    public TopUsersCommand(DailySummarySchedulerService dailySummarySchedulerService) {
        this.dailySummarySchedulerService = dailySummarySchedulerService;
    }

    @Override
    public void execute(String text, Long chatId, Long userId, Long messageId) {
        dailySummarySchedulerService.sendDailySummary();
    }

    @Override
    public String getDescription() {
        return "Show the top users from today.";
    }

}
