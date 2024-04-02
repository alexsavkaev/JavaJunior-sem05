package ru.gb.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientManager implements Runnable {
    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    private final Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String name;
    public final static ArrayList<ClientManager> clients = new ArrayList<>();
    public ClientManager(Socket socket) {
        this.socket = socket;
        try {

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился");
            broadcastMessage("Server: " + name + " подключился");
        }
        catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
}
    private void broadcastMessage(String message) {
        if (message.contains("@")) {
            // Выделяем отправителя из сообщения
            String sender = Arrays.stream(message.split(":", 2))
                    .findFirst()
                    .orElse("");
            // Собственно сообщение
            String recipientMessage = Arrays.stream(message.split(":", 2))
                    .skip(1)
                    .map(String::trim)
                    .findFirst()
                    .orElse("");
            // Разделяем сообщение на получателя и само сообщение
            String[] recipientInfo = recipientMessage.split(" ", 2);
            // Имя получателя
            String recipientName = recipientInfo[0].substring(1);
            // Само сообщение
            recipientMessage = recipientInfo[1];
            for (ClientManager client : clients) {
                if (client.name.equals(recipientName)) { // Проверяем, является ли текущий клиент получателем
                    try {
                        // Отправляем сообщение только одному получателю, выделяя его цветом
                        client.bufferedWriter.write("\u001B[36m"+sender + ": " + recipientMessage + "\u001B[0m");
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    } catch (IOException e) {
                        closeEverything(socket, bufferedWriter, bufferedReader);
                    }
                    return; // Если получатель найден, выходимаем из цикла
                }
            }
        } else {
            // Отправляем сообщение всем клиентам
            for (ClientManager client : clients) {
                if (!client.equals(this)) {
                    try {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    } catch (IOException e) {
                        closeEverything(socket, bufferedWriter, bufferedReader);
                    }
                }
            }
        }
    }
private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClient();
        try {
            if(bufferedReader != null) bufferedReader.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
}
private void removeClient() {
        clients.remove(this);
    System.out.println(name + " отключился");
    broadcastMessage("Server: " + name + " отключился");
}
}
