package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка");
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Сообщение не было отправлено");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        Handler(Socket socket) {
            this.socket = socket;
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                Message sendMessage = new Message(MessageType.NAME_REQUEST);
                connection.send(sendMessage);
                Message receiveMessage = connection.receive();
                if (receiveMessage.getType().equals(MessageType.USER_NAME)) {
                    if (!receiveMessage.getData().equals("") && !connectionMap.containsKey(receiveMessage.getData())) {
                        String userName = receiveMessage.getData();
                        connectionMap.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return userName;
                    }
                }
            }


        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message receiveMessage = connection.receive();
                if (receiveMessage.getType() == MessageType.TEXT) {
                    String messageForUsers = userName + ": " + receiveMessage.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, messageForUsers));
                } else {
                    ConsoleHelper.writeMessage("Произошла ошибка");
                }
            }
        }

        @Override
        public void run() {
            try {
                SocketAddress socketAddress = socket.getRemoteSocketAddress();
                ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом" + socketAddress.toString());
                Connection connection = new Connection(socket);
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                if (connectionMap.containsKey(userName)) {
                    connectionMap.remove(userName);
                }
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

