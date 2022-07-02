package com.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ClientThread implements Runnable{
    private Selector selector;
    public ClientThread(Selector selector){
        this.selector = selector;
    }
    @Override
    public void run() {
        try{
            for(;;){
                //获取channel数量
                int readChannels = selector.select();
                if(readChannels == 0){
                    continue;
                }
                //获取可用channel
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();


                    if (selectionKey.isReadable()){
                        readOperator(selector,selectionKey);
                    }
                }
            }
        }catch (Exception e){

        }
    }
    private void readOperator(Selector selector, SelectionKey selectionKey) throws IOException {
        //1. 从SelectionKey中获取到已经就绪的通道
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        //2. 创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //3. 循环读取客户端消息
        int readLength = socketChannel.read(byteBuffer);
        String message = "";
        if(readLength > 0){
            //切换读模式
            byteBuffer.flip();

            //读取内容
            message += Charset.forName("UTF-8").decode(byteBuffer);

        }

        //4. 将channel再次注册到选择器，监听可读状态
        socketChannel.register(selector,SelectionKey.OP_READ);

        //5. 把客户端发送消息，广播到其他客户端
        if (message.length() > 0){
            //广播给其他客户端
            System.out.println(message);

        }

    }

}
