package ru.xander.telebot.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import ru.xander.telebot.dto.Request;
import ru.xander.telebot.entity.Banya;
import ru.xander.telebot.sender.Sender;
import ru.xander.telebot.service.BanyaService;
import ru.xander.telebot.service.SettingService;

import java.util.Comparator;
import java.util.Optional;

import static ru.xander.telebot.dto.SettingName.STICKER_PICTURE_SET;
import static ru.xander.telebot.dto.SettingName.TEXT_PICTURE_SET;

/**
 * @author Alexander Shakhov
 */
@Component
public class SetPictureAction implements Action {
    private final SettingService settingService;
    private final BanyaService banyaService;

    @Autowired
    public SetPictureAction(SettingService settingService, BanyaService banyaService) {
        this.settingService = settingService;
        this.banyaService = banyaService;
    }

    @Override
    public void execute(Request request, Sender sender) {
        if (isSetPikcha(request.getCaption())) {
            if (!settingService.checkPermission(request)) {
                sender.sendText(request.getChatId(), "Хуй тебе!", request.getMessageId());
                return;
            }

            String contentId = getContentId(request.getMessage());
            if (contentId == null) {
                sender.sendText(request.getChatId(), "чот хуйня какая-то получается((");
                return;
            }

            Banya banya = banyaService.getBanya(request);
            if (banya == null) {
                sender.sendText(request.getChatId(), "Банька-хуянька!");
                return;
            }
            banya.setPicture(contentId);
            banya.setChatName(request.getChatTitle());
            banyaService.save(banya);

            String textPictureSet = settingService.getString(TEXT_PICTURE_SET);
            String stickerPictureSet = settingService.getString(STICKER_PICTURE_SET);
            sender.sendText(request.getChatId(), textPictureSet);
            sender.sendSticker(request.getChatId(), stickerPictureSet);
        }
    }

    private boolean isSetPikcha(String caption) {
        if (StringUtils.isEmpty(caption)) {
            return false;
        }
        return caption.trim().toLowerCase().startsWith("/пикча");
    }

    private String getContentId(Message message) {
        if (message.getPhoto() != null) {
            Optional<PhotoSize> photo = message.getPhoto().stream()
                    .max(Comparator.comparingInt(PhotoSize::getFileSize));
            if (photo.isPresent()) {
                return "p:" + photo.get().getFileId();
            }
        }
        if (message.getVideo() != null) {
            Video video = message.getVideo();
            return "v:" + video.getFileId();
        }
        if (message.getDocument() != null) {
            return "d:" + message.getDocument().getFileId();
        }
        return null;
    }

}
