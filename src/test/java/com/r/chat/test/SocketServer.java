package com.r.chat.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketServer {
    public static void main(String[] args) {
        Map<String, Socket> socketMap = new HashMap<String, Socket>();

        try (
                ServerSocket serverSocket = new ServerSocket(5000);
        ) {
            System.out.println("server started");
            while (true) {
                try {
                    // 连接client
                    Socket socket = serverSocket.accept();
                    System.out.println("connect with socket: " + socket.getRemoteSocketAddress());
                    // 保存客户端信息
                    String socketName = socket.getRemoteSocketAddress().toString();
                    socketMap.put(socketName, socket);
                    new Thread(() -> {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            while (true) {
                                // 读取信息
                                String message = bufferedReader.readLine();
                                System.out.println("get message from " + socket.getRemoteSocketAddress() + ": " + message);
                                // 给除了这个socket之外的客户端广播
                                socketMap.forEach((k, v) -> {
                                    if (!k.equals(socketName)) {
                                        try {
                                            OutputStream outputStream = v.getOutputStream();
                                            PrintStream printStream = new PrintStream(outputStream);
                                            // 带上标识
                                            printStream.println("message from " + socketName + ": " + message);
                                            printStream.flush();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                            }
                        } catch (IOException e) {
                            System.out.println("socket " + socketName + " error and closed");
                        }
                    }
                    ).start();
                } catch (RuntimeException e) {
                    System.out.println("server error and closed");
                    break;
                }
            }
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
}
