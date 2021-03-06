package ru.xander.telebot.action;

import org.springframework.stereotype.Component;
import ru.xander.telebot.dto.Request;
import ru.xander.telebot.sender.Sender;

/**
 * @author Alexander Shakhov
 */
@Component
public class AdminStickerAction implements Action {
    @Override
    public void execute(Request request, Sender sender) {
        if (request.isBotChat()) {
            sender.sendText(request.getBotChatId(), "/ss_" + request.getStickerId());
        }
    }
}
