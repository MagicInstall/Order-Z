package com.magicinstall.library;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wing on 16/5/25.
 */
public abstract class SocketManager {
    private static final String TAG = "SocketManager";

    private boolean mListening = false;

    /** 客户端列表, 主要用于群发 */
    private List<ClientSocket> mClientList = new ArrayList<>();

    /**
     * TCP 停止监听端口
     */
    public void stopListen() {
        mListening = false;
    }

    /**
     * TCP 开始监听端口
     * @param port
     */
    public void startListen(final int port) {
        if (mListening == true) {
            Log.w(TAG, "重复调用监听方法, 本次调用将忽略, 多个端口监听需在新实例中进行.");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(3); // 偏低的优先级
                mListening = true;
                ServerSocket server_socket;
                try {
                    server_socket = new ServerSocket(port);
                    server_socket.setSoTimeout(60000); // 设置一个超时时间令下面循环有机会用stop 方法退出
                } catch (SocketException e) {
                    Log.e(TAG, "ServerSocket 超时设置错误!");
                    return;
                } catch (IOException e) {
                    Log.e(TAG, "ServerSocket 创建失败!");
                    return;
                }

                Socket client_socket;
                while (mListening) {
                    try {
                        client_socket = server_socket.accept();
                    }
                    // 超时会进入下一次的while 判定, 利于中止线程
                    catch (InterruptedIOException e) {
                        Log.v(TAG, "服务端连接监听器超时...");
                        continue;
                    } catch (IOException e) {
                        Log.e(TAG, "服务端接受连接出错!");
                        continue;
                    }
                    Log.v(TAG, client_socket.getRemoteSocketAddress() + "请求连接");

                    // TODO: RSA

                    // 新建并插入到客户端列表
                    ClientSocket client = new ClientSocket(client_socket, null) {
                        @Override
                        public void onClose(ClientSocket client_socket) {
                            Log.i(TAG, "客户端" +
                                    client_socket.getSocket().getRemoteSocketAddress().toString() +
                                            "连接已关闭");
                            mClientList.remove(client_socket);
                        }

                        /**
                         * 收到一个完整的报文事件
                         */
                        @Override
                        public DataPacket onReceivedCompletePacket(DataPacket packet, byte[] extend) {
                            return onReceivedPacket(packet, extend);
                        }
                    };
                    mClientList.add(client);
                }
                mListening = false;
            }
        }.start();
    }

    /**
     * TCP 收到报文事件
     * <p>哩个事件唔喺主线程执行
     * @param packet 已经解好包的packet,
     *               从packet 的item 相关方法读取内容.
     * @param extend packet 的扩展数据,
     *               从哩个数据解释命令等.
     * @return 返回一个包含回复内容的报文.
     */
    public abstract DataPacket onReceivedPacket(DataPacket packet, byte[] extend);

//    /**
//     * 回复客户端
//     * @param client
//     * @param packet
//     */
//    private void replyToClient(Socket client, DataPacket packet){
//        if (client == null || client.isClosed()) {
//            Log.w(TAG, "客户端连接已关闭");
//            return;
//        }
//        if (packet == null || packet.getCurrentItem() == null) {
//            Log.w(TAG, "空的报文");
//            return;
//        }
//
//        OutputStream output_stream;
//        try {
//            output_stream = client.getOutputStream();
//        } catch (IOException e) {
//            Log.e(TAG, "创建输出流错误, 或Socket 无效!");
//            return;
//        }
//        // 发送报文
//        packet.moveToFirstPacket();
//        try {
//            do {
//                output_stream.write(packet.getCurrentPacket().data);
//                output_stream.flush();
//            } while (packet.moveNextPacket() != null);
//        } catch (IOException e) {
//            Log.e(TAG, "输出流写入错误, 或无法发送报文!");
//            return;
//        }
//    }

    /**
     * TCP 群发
     * @param packet
     * @return
     */
    public int Batch(DataPacket packet){
        int success = 0;
        for (ClientSocket client: mClientList) {
            if (client.send(packet)) {
                success++;
            } else {
                Log.w(TAG,
                        "群发至" + client.getSocket().getRemoteSocketAddress().toString() + "失败");
            }
        }
        return success;
    }


//    /************************************************************************
//     *                                客户端类                               *
//     ************************************************************************/
//    /**
//     * 哩个类主要负责报文的拆包解包处理,
//     * 以及RSA 加密解密.
//     */
//    private abstract class Client {
//        private static final String TAG = "Client";
//
//        private HashMap<Number, DataPacket> mPacketMap;
//
//        private RSA    mRsa    = null;
//
//        private Socket mSocket = null;
//        public Socket getSocket() {
//            return mSocket;
//        }
//
//        /**
//         * 构造并开一个新线程接收客户端报文
//         * @param socket
//         * @param rsa
//         */
//        public Client(Socket socket, RSA rsa) {
//            mPacketMap = new HashMap<>();
//            mSocket = socket;
//
//            mRsa    = rsa;
//
//            new Thread() {
//                @Override
//                public void run() {
//                    Thread.currentThread().setPriority(4); // 稍低的优先级
//                    Thread.currentThread().setName(mSocket.getRemoteSocketAddress().toString());
//
//                    InputStream inputStream;
//                    try {
//                        inputStream = mSocket.getInputStream();
//                    } catch (IOException e) {
//                        Log.e(TAG, "无法取得Socket 的输入流!");
//                        return;
//                    }
//
//                    // 不断循环等待数据
//                    byte input_buffer[] = new byte[Define.PACKET_LIMIT_LENGTH];
//                    int  input_length = 0;
//                    try {
//                        // 等接收
//                        while ((input_length = inputStream.read(input_buffer)) != -1) {
//                            // 复制一份并触发事件
//                            final byte data[] = new byte[input_length];
//                            System.arraycopy(input_buffer, 0, data, 0, input_length);
//
//                            addPacket(data);
//                        }
//                    } catch (IOException e) {
//                        Log.e(TAG, "输入流读取错误!");
//                    }
//
//                    // 关
//                    try {
//                        mSocket.close();
//                    } catch (IOException e) {
//                        Log.e(TAG, "Socket 已关闭");
//                    }
//
//                    // 结束
//                    onClose();
////                    mClientList.remove(this);
//                }
//            }.start();
//
//            Log.v(TAG, "已连接上" + socket.getRemoteSocketAddress().toString());
//        }
//
//        public abstract void onClose();
//
//        /**
//         * 处理收到的报文
//         * @param data
//         */
//        private void addPacket(final byte data[]) {
//            if (data == null || data.length < 1) {
//                Log.w(TAG, "收到一个空的包!");
//                return;
//            }
//
//            if (!DataPacket.verifyPacketCRC(data)) {
//                Log.e(TAG, "CRC 校验失败!"); // TCP 连接一般唔会出错(?)
//                return;
//            }
//
//
//            final int id = DataPacket.getID(data);
//            // ID匹配到有未收完的分包
//            if (mPacketMap.containsKey(id)) {
//                final DataPacket packet = mPacketMap.get(id);
//                packet.pushPacket(data);
//
//                // 判断分包是否已经收齐
//                if (packet.getPacketCount() == DataPacket.getPacketCount(data)) {
//                    Log.v(TAG, "收齐一个报文");
//
//                    // TODO: 喺同一个新线程处理报文
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            Thread.currentThread().setPriority(3); // 偏低的优先级
//
//                            // 从待收齐的报文列表中去除
//                            mPacketMap.remove(id);
//
//                            // TODO:判断是否需要解密
//
//                            // 解包
//                            byte extend[] = new byte[Define.EXTEND_LIMIT_LENGTH];
//                            packet.resolve(extend, null);
//                            // 触发报文组合完成事件
//                            DataPacket reply_packet = onReceivedCompletePacket(packet, extend);
//                            // 回复
//                            send(reply_packet);
//                        }
//                    }.start();
//                }
//            }
//            // ID没有能匹配的未收完分包
//            else {
//                DataPacket packet = new DataPacket();
//                packet.pushPacket(data);
//                mPacketMap.put(id, packet);
//            }
//
//        }
//
//        /**
//         * 收到一个完整的报文事件
//         * @see {SocketManager.onReceivedPacket()}事件.
//         */
//        public abstract DataPacket onReceivedCompletePacket(DataPacket packet, byte[] extend);
//
//        /**
//         * 发送报文
//         * @param packet
//         * @return true 发送成功
//         */
//        public boolean send(DataPacket packet) {
//            if (packet == null || packet.getCurrentPacket() == null) {
//                Log.w(TAG, "空的发送内容");
//                return false;
//            }
//            if (mSocket == null || mSocket.isClosed()) {
//                Log.w(TAG, "客户端连接已关闭");
//                return false ;
//            }
//
//            OutputStream output_stream;
//            try {
//                output_stream = mSocket.getOutputStream();
//            } catch (IOException e) {
//                Log.e(TAG, "创建输出流错误, 或Socket 无效!");
//                return false;
//            }
//            // 发送报文
//            packet.moveToFirstPacket();
//            try {
//                do {
//                    output_stream.write(packet.getCurrentPacket().data);
//                    output_stream.flush();
//                } while (packet.moveNextPacket() != null);
//            } catch (IOException e) {
//                Log.e(TAG, "输出流写入错误, 或无法发送报文!");
//                return false;
//            }
//            return true;
//        }
//    }

}
