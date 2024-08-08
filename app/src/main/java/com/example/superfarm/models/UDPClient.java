package com.example.superfarm.models;

import android.util.Log;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private int port = 5005;
    private InetAddress address;
    private DatagramSocket socket;

    public UDPClient() throws Exception {
        this.address = InetAddress.getByName("udpserver.bu.ac.th");
        this.socket = new DatagramSocket();
    }

    public String sendGetCommand(String id) throws Exception {
        String command = "GET," + id;
        byte[] buffer = command.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);

        // Prepare buffer for response
        byte[] responseBuffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(responsePacket);

        return new String(responsePacket.getData(), 0, responsePacket.getLength());
    }

    public void sendSetCommand(String id, String data) throws Exception {
        String command = "SET," + id + "," + data;
        byte[] buffer = command.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }

}
