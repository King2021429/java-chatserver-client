package com.server;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

//服务器端
public class ChatServer {

    //服务器端启动方法
    public void startServer() throws IOException {
        //1.创建selector选择器
        Selector selector = Selector.open();

        //2.创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3.为channel通道绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8000));
        //设置非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //4.把channel通道注册到selector选择器上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已经启动成功了");

        //5.循环，等待有新链接接入 while(true)
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
                //6.根据就绪状态，调用对应方法实现具体业务操作
                if (selectionKey.isAcceptable()){
                    acceptOperator(serverSocketChannel,selector);
                }
                if (selectionKey.isReadable()){
                    readOperator(selector,selectionKey);
                }
            }
        }
    }

    //处理可读状态操作
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
            castOtherClient(message,selector,socketChannel);
        }

    }

    //广播给其他客户端
    private void castOtherClient(String message, Selector selector, SocketChannel socketChannel) throws IOException {
        Set<SelectionKey> selectionKeySet = selector.keys();

        for(SelectionKey selectionKey : selectionKeySet){
            //获取里面每个channel
            Channel tarChannel = selectionKey.channel();
            if (tarChannel instanceof SocketChannel && tarChannel != socketChannel){
                ((SocketChannel) tarChannel).write(Charset.forName("UTF-8").encode(message));
            }
        }
    }

    //处理接入状态操作
    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        //1.接入状态 创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        //2.把socketChannel设置为非阻塞模式
        socketChannel.configureBlocking(false);

        //3.把channel注册到selector选择器上，并且监听可读状态
        socketChannel.register(selector,SelectionKey.OP_READ);

        //4.客户端回复信息
        socketChannel.write(Charset.forName("UTF-8")
                .encode("欢迎进入聊天室,请注意隐私安全"));
    }


    //启动主方法
    public static void main(String[] args) {
        try {
            new ChatServer().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
