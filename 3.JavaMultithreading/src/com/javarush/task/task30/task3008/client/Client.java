package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {

        public void run(){
            String serverAddress = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(serverAddress, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message receiveMessage = connection.receive();
                if (receiveMessage.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (receiveMessage.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message receiveMessage = connection.receive();
                if (receiveMessage.getType() == MessageType.TEXT) {
                    processIncomingMessage(receiveMessage.getData());
                } else if (receiveMessage.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(receiveMessage.getData());
                } else if (receiveMessage.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(receiveMessage.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера");
        String serverAddress = ConsoleHelper.readString();
        return serverAddress;
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите адрес порта");
        int port = ConsoleHelper.readInt();
        return port;
    }

    protected String getUserName() {
        System.out.println("Введите имя");
        String userName = ConsoleHelper.readString();
        return userName;
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {

        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка во время отправки сообщения");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Произошла ошибка");
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (message.equals("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
        }
    }
}
