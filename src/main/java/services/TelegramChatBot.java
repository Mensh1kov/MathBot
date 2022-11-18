package services;

import bot.Bot;
import bot.api.BotReply;
import bot.api.ChatUpdate;
import components.KeyboardFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramChatBot extends TelegramLongPollingBot {
    private final String BOT_TOKEN;
    private final String BOT_NAME;
    private final Bot BOT;
    private final KeyboardFactory keyboardFactory;

    public TelegramChatBot(String BOT_NAME, String BOT_TOKEN, Bot BOT, KeyboardFactory keyboardFactory) {
        this.BOT_NAME = BOT_NAME;
        this.BOT_TOKEN = BOT_TOKEN;
        this.BOT = BOT;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String text = "";
        String userId = "";
        String chatId = "";
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            userId = callbackQuery.getFrom().getId().toString();
            chatId = callbackQuery.getMessage().getChatId().toString();
            text = callbackQuery.getData();
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            userId = message.getFrom().getId().toString();
            chatId = message.getChatId().toString();
            text = message.getText();
        }

        ChatUpdate chatUpdate = new ChatUpdate(userId, chatId);
        chatUpdate.setText(text);
        sendMessage(BOT.reply(chatUpdate));

    }

    public void sendMessage(BotReply botReply) {
        SendMessage outMessage = new SendMessage();
        outMessage.setChatId(botReply.getChatId());
        outMessage.setText(botReply.getText());
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardFactory.buildInLineKeyboard(botReply.getInLineKeyboard());
        ReplyKeyboard replyKeyboard = keyboardFactory.buildKeyboard(botReply.getKeyboard());
        if (inlineKeyboardMarkup == null) {
            if (replyKeyboard == null) {
                ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                keyboardRemove.setRemoveKeyboard(true);
                outMessage.setReplyMarkup(keyboardRemove);
            } else {
                outMessage.setReplyMarkup(replyKeyboard);
            }
        } else {
            outMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            execute(outMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}