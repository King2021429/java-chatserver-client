package com.client;

import java.io.IOException;

public class BClient {
    public static void main(String[] args) throws IOException {
        new ChatClient().startClient("jack");
    }
}
