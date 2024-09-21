package com.r.chat.test;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
    public static void main(String[] args) {
        try (
                // 连接server
                Socket socket = new Socket("localhost", 5000);
        ) {
            System.out.println("connected");
            // 读
            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            new Thread(() -> {
                while (true) {
                    try {
                        String input = br.readLine();
                        System.out.println(input);
                    } catch (IOException e) {
                        System.out.println("error while reading from server");
                        return;
                    }
                }
            }).start();
            // 写
            OutputStream out = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(out);
            while (true) {
                // 获取输入
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                // 输出
                pw.println(input);
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("connection error");
        }
    }
}
