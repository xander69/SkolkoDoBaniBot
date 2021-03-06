package ru.xander.telebot.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.xander.telebot.dto.MessageMode;
import ru.xander.telebot.dto.Request;
import ru.xander.telebot.sender.Sender;
import ru.xander.telebot.service.SettingService;
import ru.xander.telebot.util.Utils;

import static ru.xander.telebot.dto.SettingName.TEXT_UNKNOWN;

/**
 * @author Alexander Shakhov
 */
@Component
public class UnknownAction implements Action {

    private final String[] hmmm = {
            "CAADAgADFwADaJpdDFNUjHti9TpLAg",
            "CAADAgADGQADaJpdDLufX1bbsCKrAg",
            "CAADAgADKwADaJpdDL8YdHZnzgEPAg",
            "CAADAgADCwADaJpdDAauf5mESMGmAg",
            "CAADAgADDwADaJpdDLbQl_ySNkJNAg",
            "CAADAgADHwADaJpdDDUrtxkEhXY8Ag"
    };

    private final String[] good = {
            "CAADAgADzwMAAjq5FQIejrqP77nexAI",
            "CAADAgAD3wADyUpYAAEG1OtxPo4pJQI",
            "CAADAgAD3gADyUpYAAGuQIx2rL9TWwI",
            "CAADAgADGAAD5NdGDj8TYTfHnZ7gAg",
            "CAADAgADOAEAAhZ8aAP0b0MaIxsr8QI",
            "CAADBQADewMAAukKyANR7TNPzLj7awI",
            "CAADAgADdAADmS9LCmoQzL8h59vAAg",
            "CAADAgADMAEAAuL27Qaa1y_1mTBI-gI",
            "CAADBQAD0gMAAukKyAMDDEiMpvCB6wI",
            "CAADBQADiAMAAukKyAMBNuxRfO7SkQI",
            "CAADBAADKwADmDVxAmIdrKk5BjVdAg"
    };

    private final SettingService settingService;

    @Autowired
    public UnknownAction(SettingService settingService) {
        this.settingService = settingService;
    }

    @Override
    public void execute(Request request, Sender sender) {
        if (request.isBotChat()) {
            return;
        }
        if (request.isSuperUser()) {
            sender.sendSticker(request.getChatId(), "CAADAgADGAAD5NdGDj8TYTfHnZ7gAg");
        } else {
            String textUnknown = settingService.getString(TEXT_UNKNOWN);
            if (Utils.isHappyBirthDay()) {
                if (request.isReplyToBot()) {
                    if (Utils.randomBoolean()) {
                        sender.sendSticker(request.getChatId(), Utils.randomArray(good));
                    } else {
                        sender.sendSticker(request.getChatId(), Utils.randomArray(good), request.getMessageId());
                    }
                } else {
                    if (Utils.randomBoolean()) {
                        sender.sendSticker(request.getChatId(), Utils.randomArray(hmmm));
                    } else {
                        sender.sendSticker(request.getChatId(), Utils.randomArray(hmmm), request.getMessageId());
                    }
                }
            } else if (Utils.randomBoolean()) {
                if (Utils.randomBoolean()) {
                    sender.sendText(request.getChatId(), textUnknown);
                } else {
                    sender.sendText(request.getChatId(), textUnknown, request.getMessageId());
                }
            } else {
                String userMention = Utils.randomUserMention(request);
                if (Utils.randomBoolean()) {
                    sender.sendText(request.getChatId(), userMention + " " + textUnknown, MessageMode.MARKDOWN);
                } else {
                    sender.sendText(request.getChatId(), userMention + " " + textUnknown, request.getMessageId(), MessageMode.MARKDOWN);
                }
            }
        }
    }
}
