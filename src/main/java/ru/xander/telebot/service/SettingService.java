package ru.xander.telebot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.xander.telebot.dto.Request;
import ru.xander.telebot.dto.SettingName;
import ru.xander.telebot.entity.Setting;
import ru.xander.telebot.repository.SettingRepo;
import ru.xander.telebot.util.BotException;
import ru.xander.telebot.util.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static ru.xander.telebot.dto.SettingName.ACTIVE_CHAT_ID;
import static ru.xander.telebot.dto.SettingName.ADMIN_ID;

/**
 * @author Alexander Shakhov
 */
@Service
public class SettingService {
    private final SettingRepo settingRepo;

    @Autowired
    public SettingService(SettingRepo settingRepo) {
        this.settingRepo = settingRepo;
    }

    public List<Setting> getAll() {
        return settingRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public String getString(SettingName settingName) {
        Setting setting = settingRepo.findByName(settingName);
        if (setting == null) {
            return null;
        }
        return setting.getValue();
    }

    public Boolean getBoolean(SettingName settingName) {
        String value = getString(settingName);
        return Boolean.parseBoolean(value);
    }

    public Integer getInt(SettingName settingName) {
        String value = getString(settingName);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public Long getLong(SettingName settingName) {
        String value = getString(settingName);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    public LocalDate getLocalDate(SettingName settingName) {
        String value = getString(settingName);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Utils.parseLocalDate(value, "yyyy-MM-dd");
    }

    public void setLocalDate(SettingName settingName, LocalDate value) {
        if (value == null) {
            saveParam(settingName.name(), null);
        } else {
            saveParam(settingName.name(), value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }

    public <T> T getJson(SettingName settingName, Class<T> clazz) {
        String value = getString(settingName);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Utils.parseJson(value, clazz);
    }

    public Long getActiveChatId() {
        Setting activeChatId = settingRepo.findByName(ACTIVE_CHAT_ID);
        if (activeChatId == null) {
            throw new BotException("Установи ACTIVE_CHAT_ID!!!");
        }
        return Long.parseLong(activeChatId.getValue());
    }

    public boolean checkPermission(Request request) {
        if (request.isSuperUser()) {
            return true;
        }
        Setting adminId = settingRepo.findByName(ADMIN_ID);
        if (adminId == null) {
            return false;
        }
        return Objects.equals(request.getUserId(), Integer.parseInt(adminId.getValue()));
    }

    public void saveParam(String paramName, String paramValue) {
        SettingName settingName = SettingName.valueOf(paramName);
        Setting setting = settingRepo.findByName(settingName);
        if (setting == null) {
            throw new BotException("Параметра с именем " + paramName + " не существует!");
        }
        setting.setValue(paramValue);
        settingRepo.save(setting);
    }
}
