package com.magicinstall.library;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

/**
 * 哩个类主要负责报文的拆包解包处理,
 * 以及RSA 加密解密.
 *
 * Created by wing on 16/5/25.
 */
public abstract class ClientSocket {
    private static final String TAG = "ClientSocket";

    private HashMap<Number/*ID*/, DataPacket> mPacketMap = new HashMap<>();

    private RSA mRsa = null;

    //    private Socket mSocket = null;
    private Socket mTcpSocket = null;
    public Socket getSocket() {
        return mTcpSocket;
    }

    public boolean isConnected(){
        if (mTcpSocket == null) return false;
        return mTcpSocket.isConnected();
    }

    /** 登陆令牌 */
    public UUID Token = null;

    @Override
    public int hashCode() {
        return mTcpSocket.hashCode();
    }

    /**
     * 客户端使用的构造
     * <P>使用IP 地址及端口构造, 以及发起TCP连接,
     * 并打开一个新线程接收服务端报文.
     * @param ip
     * @param port
     * @param rsa
     */
    public ClientSocket(final String ip, final int port, RSA rsa) {
        mRsa = rsa;

        // 在新线程发起连接
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(3); // 偏低的优先级
//                // 等Service 启动
//                try { sleep(1000);
//                } catch (InterruptedException e1) {
//                    Log.e(TAG, "sleep 失败,当前线程已被中断!");
//                }
                // 不停连接服务端
                int interval = 500/*半秒*/;
                do {
                    Log.i(TAG, "正在连接服务端...");
                    try {
                        mTcpSocket = new Socket(ip, port);
                    } catch (IOException e) {
                        Log.w(TAG, "请求连接服务端失败, " + (float)interval / 1000 + "秒后重试...");

//                        if (!mTcpSocket.isClosed()) {
//                            try { mTcpSocket.close();
//                            } catch (IOException e1) { Log.w(TAG, "Socket 已关闭");
//                            }
//                        }
                        mTcpSocket = null;

                        try { sleep(interval);
                        } catch (InterruptedException e1) {
                            Log.e(TAG, "sleep 失败,当前线程已被中断!");
                        }

                        // 逐渐增加发起连接的间隔, 以节约能源
                        if (interval < 10000/*10秒*/) interval += 1000/*1秒*/;
                    }
                } while (mTcpSocket == null);

                Log.v(TAG, "已连接, 服务端IP:" + mTcpSocket.getRemoteSocketAddress().toString());
                onConnected(ClientSocket.this);

                // TODO: 取得RSA 实例

                // 进入接收循环
                startReadLoop();
            }
        }.start();
    }

    /**
     * 服务端使用的构造
     * <p>使用从{ServerSocket} 取得的{Socket}实例构造,
     * 并打开一个新线程接收客户端报文.
     * @param socket
     * @param rsa
     */
    public ClientSocket(Socket socket, RSA rsa) {
//        mPacketMap = new HashMap<>();
        mTcpSocket = socket;
        mRsa       = rsa;

        // 在新线程监听输入流
        new Thread() {
            @Override
            public void run() {
                // 进入接收循环
                startReadLoop();
            }
        }.start();

        Log.v(TAG, "已连接, 客户端IP:" + socket.getRemoteSocketAddress().toString());
    }



    /**
     * 进入接收循环.
     * <p>哩个方法冇检测Socket 状态的逻辑, 小心使用!
     */
    private void startReadLoop(){
        Thread.currentThread().setPriority(4); // 稍低的优先级
        Thread.currentThread().setName(mTcpSocket.getRemoteSocketAddress().toString());

        InputStream inputStream;
        try {
            inputStream = mTcpSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "无法取得Socket 的输入流!");
            return;
        }

        // 不断循环等待数据
        byte input_buffer[] = new byte[Define.PACKET_LIMIT_LENGTH];
        int  input_length = 0;
        try {
            // 等接收
            while ((input_length = inputStream.read(input_buffer)) != -1) {
                Log.v(TAG, "收到" + input_length + "byte");
                // 复制一份并触发事件
                final byte data[] = new byte[input_length];
                System.arraycopy(input_buffer, 0, data, 0, input_length);

                addPacket(data);
            }
        } catch (IOException e) {
            Log.e(TAG, "输入流读取错误!");
        }

        // 必需close 一次, 然后下面触发{onClose}事件.
        try {
            mTcpSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Socket 已关闭");
        }

        // 结束
        this.onClose(this);
//                mClientList.remove(this);
    }

    /**
     * 作为客户端, 与服务端连接成功的事件.
     * <p>哩个事件唔喺主线程执行.
     */
    public void onConnected(ClientSocket client){}

    /**
     * Socket 关闭事件.
     * <p>触发哩个事件嘅时候, 唔系Socket 已经关闭, 只系隐性调用过close 方法.
     * TODO: 目前Socket 类冇办法取得onClose 事件.
     */
    public void onClose(ClientSocket client){}

    /**
     * 处理收到的报文
     * @param data
     */
    private void addPacket(final byte data[]) {
        if (data == null || data.length < 1) {
            Log.w(TAG, "收到一个空的包!");
            return;
        }

        if (!DataPacket.verifyPacketCRC(data)) {
            Log.e(TAG, "CRC 校验失败!"); // TCP 连接一般唔会出错(?)
            return;
        }

        final int id = DataPacket.getID(data);
        DataPacket packet = null; // 哩个指针会传递到后面线程开始前
        // ID匹配到有未收完的分包
        if (mPacketMap.containsKey(id)) {
            packet = mPacketMap.get(id);
            packet.pushPacket(data);

            // 分包未收齐就退出, 等待下一个分包
            if (packet.getPacketCount() != DataPacket.getPacketCount(data))  return;

            // 收齐就继续处理
            // 从待收齐的报文列表中去除
            mPacketMap.remove(id);

            // 传递到下面的线程过程
//            complete_packet = packet;

//                Log.v(TAG, "收齐一个报文");
//
//                // TODO: 喺同一个新线程处理报文
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Thread.currentThread().setPriority(3); // 偏低的优先级
//
//
//
//                        // TODO:判断是否需要解密
//
//                        // 解包
//                        byte extend[] = new byte[Define.EXTEND_LIMIT_LENGTH];
//                        packet.resolve(extend);
//                        // 触发报文组合完成事件
//                        DataPacket reply_packet = onReceivedCompletePacket(packet, extend);
//                        // 回复
//                        send(reply_packet);
//                    }
//                }.start();

        }
        // ID没有能匹配的未收完分包
        else {
            packet = new DataPacket();
            packet.pushPacket(data);

            // 报文有分包的情况, 将分包压入待收齐的报文列表后退出, 等待下一个分包
            if (DataPacket.getPacketCount(data) > 1) {
                mPacketMap.put(id, packet);
                return;
            }

            // 冇分包就继续下边的处理
        }

        Log.v(TAG, "收齐一个报文");

        // TODO: 喺同一个新线程处理报文
        final DataPacket complete_packet = packet; // 用一个常量指针指向报文的实例传递到新线程
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(3); // 偏低的优先级

                // 从待收齐的报文列表中去除
                mPacketMap.remove(id);

                // TODO:判断是否需要解密

                // 解包
                byte extend[] = new byte[Define.EXTEND_LIMIT_LENGTH];
                complete_packet.resolve(extend);
                complete_packet.remoteAddress = mTcpSocket.getInetAddress();
                // 触发报文组合完成事件
                DataPacket reply_packet = onReceivedCompletePacket(complete_packet, extend);
                // 回复
                send(reply_packet);
            }
        }.start();
    }

    /**
     * 收到一个完整的报文事件
     * @see {SocketManager.onReceivedPacket()}事件.
     */
    public abstract DataPacket onReceivedCompletePacket(DataPacket packet, byte[] extend);

    /**
     * 发送报文
     * @param packet
     * @return true 发送成功
     */
    public boolean send(DataPacket packet) {
        if (packet == null/* || packet.moveToFirstPacket() == null*/) {
            Log.w(TAG, "报文是空指针"/*"空的发送内容"*/);
            return false;
        }
        if (mTcpSocket == null || mTcpSocket.isClosed()) {
            Log.w(TAG, "该连接已关闭");
            return false ;
        }

        OutputStream output_stream;
        try {
            output_stream = mTcpSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "创建输出流错误, 或Socket 无效!");
            return false;
        }
        // 发送报文
        packet.moveToFirstPacket();
        try {
            do {
                output_stream.write(packet.getCurrentPacket().data);
                output_stream.flush();
            } while (packet.moveNextPacket() != null);
        } catch (IOException e) {
            Log.e(TAG, "输出流写入错误, 或无法发送报文!");
            return false;
        }
        return true;
    }
}