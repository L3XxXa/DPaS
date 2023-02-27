import com.sun.java.accessibility.util.Translator;
import com.sun.jdi.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private final int port;
    private final String translator;
    private final String ipToTranslate;
    private int connectionsNum;
    private final Selector selector;
    public Server(String port, String translator, String ipToTranslate) throws IOException {
        this.port = Integer.parseInt(port);
        this.translator = translator;
        this.ipToTranslate = ipToTranslate;
        this.selector = Selector.open();
        this.connectionsNum = 0;
        acceptConnections(this.port);
    }
    private void acceptConnections(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        serverSocketChannel.socket().bind(inetSocketAddress);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0){
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext()){
                SelectionKey selectionKey = iter.next();
                iter.remove();
                if (selectionKey.isAcceptable()){
                    accept(selectionKey);
                } else if (selectionKey.isReadable()) {
                    System.out.println("Could read");
                    ((Translator)selectionKey.attachment()).read();
                } else if (selectionKey.isWritable()) {
                    System.out.println("Could write");
                    if (((Translator) selectionKey.attachment()).write()) {

                    }
                }
            }
        }

    }
    private void accept(SelectionKey selectionKey) {
        ServerSocketChannel nextServerSocketChannel = (ServerSocketChannel)selectionKey.channel();
        SocketChannel socketChannel;
        try {
            socketChannel = nextServerSocketChannel.accept();
            System.out.println("New connection from " + socketChannel.socket());
            socketChannel.configureBlocking(false);
            SelectionKey clientSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ);

            InetSocketAddress inetSocketAddress = new InetSocketAddress(translator, Integer.parseInt(ipToTranslate));
            SocketChannel clientToServerCon = SocketChannel.open(inetSocketAddress);

            clientToServerCon.configureBlocking(false);
            SelectionKey serverSelectionKey = clientToServerCon.register(selector, SelectionKey.OP_READ);


            ByteBuffer toClientBuffer = ByteBuffer.allocate(1024);
            ByteBuffer fromClientBuffer = ByteBuffer.allocate(1024);

            Translator serverTrans = new Translator(serverSelectionKey, null, fromClientBuffer, toClientBuffer);
            Translator clientTrans = new Translator(clientSelectionKey, serverTrans, toClientBuffer, fromClientBuffer);
            serverTrans.otherTranslator = clientTrans;

            serverSelectionKey.attach(serverTrans);
            clientSelectionKey.attach(clientTrans);

            connectionsNum++;
            System.out.println("Got acceptable key, now: " + connectionsNum + " connections");
        } catch (IOException e) {
            System.err.println("Unable to accept channel");
            e.printStackTrace();
            selectionKey.cancel();
        }
    }

    private class Translator{
        private final SocketChannel socketChannel;
        private final ByteBuffer readFrom, writeTo;
        private final SelectionKey key;
        private Translator otherTranslator;
        public Translator(SelectionKey key, Translator otherTranslator, ByteBuffer readFrom, ByteBuffer writeTo){
            this.socketChannel = (SocketChannel) key.channel();
            this.readFrom = readFrom;
            this.writeTo = writeTo;
            this.key = key;
            this.otherTranslator = otherTranslator;
        }
        public boolean write() throws IOException {
            writeTo.flip();
            socketChannel.write(writeTo);
            return writeTo.remaining() == 0;
        }
        public void read() throws IOException {
            readFrom.clear();
            if(socketChannel.read(readFrom) == -1){
                key.channel().close();
                key.cancel();
                otherTranslator.key.channel().close();
                otherTranslator.key.cancel();
                connectionsNum--;
                System.out.println("One connection closed, now: " + connectionsNum + " connections");
                return;
            }
            if(!otherTranslator.write()) {
                System.out.println("Не получилось нормально записать :(");
                otherTranslator.key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
        }
    }
}
