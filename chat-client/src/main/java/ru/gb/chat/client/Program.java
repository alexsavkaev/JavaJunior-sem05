package ru.gb.chat.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Введите свое имя: ");
        String name = sc.nextLine();
        try (Socket socket = new Socket("localhost", 1400)){
        Client client = new Client(socket, name);
        InetAddress inetAddress = socket.getInetAddress();
        System.out.println("Соединение установлено с " + inetAddress.getHostAddress());
        String remoteIp = inetAddress.getHostAddress();
        System.out.println("Удаленный адрес: " + remoteIp);
        System.out.println("Порт: " + socket.getLocalPort());
        client.listenForMessage();
        client.sendMessage();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
