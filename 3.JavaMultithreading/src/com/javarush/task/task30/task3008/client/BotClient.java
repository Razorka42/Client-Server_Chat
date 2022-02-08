package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();

    }

    public class BotSocketThread extends Client.SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (message.contains(":")) {
                String[] messageParts = message.split(": ");
                String userName = messageParts[0];
                String text = messageParts[1];
                String pattern = "Информация для " + userName + ": ";
                Calendar calendar = new GregorianCalendar();
                if (text.equals("дата")) {
                    sendTextMessage(pattern + new SimpleDateFormat("d.MM.YYYY").format(calendar.getTime()));
                }
                if (text.equals("день")) {
                    sendTextMessage(pattern + new SimpleDateFormat("d").format(calendar.getTime()));
                }
                if (text.equals("месяц")) {
                    sendTextMessage(pattern + new SimpleDateFormat("MMMM").format(calendar.getTime()));
                }
                if (text.equals("год")) {
                    sendTextMessage(pattern + new SimpleDateFormat("YYYY").format(calendar.getTime()));
                }
                if (text.equals("время")) {
                    sendTextMessage(pattern + new SimpleDateFormat("H:mm:ss").format(calendar.getTime()));
                }
                if (text.equals("час")) {
                    sendTextMessage(pattern + new SimpleDateFormat("H").format(calendar.getTime()));
                }
                if (text.equals("минуты")) {
                    sendTextMessage(pattern + new SimpleDateFormat("m").format(calendar.getTime()));
                }
                if (text.equals("секунды")) {
                    sendTextMessage(pattern + new SimpleDateFormat("s").format(calendar.getTime()));
                }

            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return new String("date_bot_" + (int) (Math.random() * 100));
    }
}
