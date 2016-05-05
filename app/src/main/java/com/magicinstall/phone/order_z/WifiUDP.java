package com.magicinstall.phone.order_z;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 哩个类主要用于内网客户端自动发现服务端
 * <
 *
 * Created by wing on 16/4/24.
 */
public class WifiUDP {
    private static final String TAG = "WifiUDP";

//    private static final String mGroupIP = "224.0.0.1"; // 多播地址;

    private MulticastSocket mMulticastSocket; // 广播
    private DatagramSocket  mDatagramSocket;  // P2P

    private Context mContext;

    /**
     * 构造
     * @param context
     * @throws UnknownHostException
     */
    public WifiUDP(Context context, String groupIP) throws UnknownHostException {
        mGroupAddress = InetAddress.getByName(groupIP);

        mContext = context;
    }

    private int mBroadcastTTL = 1; // 默认值
    /**
     * 广播报文的生存期
     * <p>报文的TTL每经过一个网关/路由, 就会减一,
     * 当减至0的时候仲未到达目的地, 报文就会被网关/路由吃掉;
     * <p>有个不确定情况: 交换机一般系唔会减TTL的, 但.唔.系.绝.对...
     *
     * @param ttl 一般的内网只需要1(默认值), 外网要试过先知...
     */
    public void setBroadcastTTL(int ttl) {
        mBroadcastTTL = ttl;
    }
    /** @see .setBroadcastTTL */
    public int getBroadcastTTL() {
        return mBroadcastTTL;
    }

    private boolean mListening = false;
    /**
     * 中止接收线程的循环体
     * <p>哩个方法只系中止while 循环, 并不能中止receive 的阻塞,
     * @see .setTimeout
     */
    public void stopReceive() {
        mListening = true;
        // 由线程负责退出广播组, 同埋将Socket 引用变null
    }

    private int mTimeout = 1000; // 默认1秒
    /**
     * 设置Socket 收发的超时时间
     * <p>由于Socket 必须在另一线程收发报文,
     * 关键系receive 有好大机会会一直无限阻塞,
     * 导致监听无办法中止,所以要合理设置一个超时值,
     * 令监听线程有机会退出.
     * @param timeout 单位是毫秒, 传入小于1 的值将被忽略.
     */
    public void setTimeout(int timeout) {
        if (timeout > 0) mTimeout = timeout;
    }
    /** @see .setTimeout */
    public int getTimeout() {
        return mTimeout;
    }

    private InetAddress mGroupAddress = null;
    /**
     * 设置广播组地址
     * <p> 在构造函数指定, 期间不允许修改,
     * 避免不必要的麻烦.
     * @param ip
     */
    @Deprecated
    public void setGroupIP(String ip) {}
    /** @see .setGroupIP */
    public String getGroupIP() {
        Log.v(TAG, mGroupAddress.toString());
        String[] strArr = mGroupAddress.toString().split("/");
        return strArr[1];
    }


//    private int mPort = 23333; //默认值
//    /**
//     * 设置UDP 端口号
//     * <p> 在构造函数指定, 期间不允许修改,
//     * 避免不必要的麻烦.
//     * @param port
//     */
//    @Deprecated
//    public void setPort(int port) {
////        if (port > -1) mPort = port;
//    }
//    /** @see .getPort */
//    public int getPort() {
//        return mPort;
//    }

    /**
     * [作为服务端]
     * 通过监听UDP 广播, 发现客户端的连接请求,
     * 并向客户端发出本机IP;
     * <p>哩个方法会在另一个线程while 循环, 如要中止, 就要调用{@Link }
     * @param broadcastPort 等待客户端广播探测包的端口号 0 - 65535
     * @param unicastPort 向客户发送本机IP的端口号 0 - 65535
     * @return true: 运行过程顺利(不包括收发过程), false: 应该提醒用户确认Wifi 状态!
     */
    public boolean serverWaitClientAndSendIP(int broadcastPort, int unicastPort){
        if (mListening) return false;

        // 初始化socket
        if (!initSocket(broadcastPort, unicastPort)) return false;

        // 开始监听广播
        return serverStartListenUDP(broadcastPort, unicastPort);
    }

    /**
     * {@Link serverWaitClientAndSendIP} 方法专用Part1,
     * 太长无办法...
     *
     * <p>打开新线程开始监听连接请求
     *
     * @param broadcastPort 广播端口号,
     *                      哩度冇用到, 初始化嗰时已经设置好广播端口
     * @param unicastPort 单播端口号
     * @return true: 运行过程顺利(不包括收发过程)
     */
    private boolean serverStartListenUDP(int broadcastPort, final int unicastPort) {
        new Thread(){
            @Override
            public void run() {
                mListening = true;

                byte receive_bufffer[] = new byte[003]; // TODO: 试验buffer 嘅长度
                final DatagramPacket receive_packet = new DatagramPacket(
                        receive_bufffer, receive_bufffer.length); //, mGroupAddress, port);

                String Client_ip;

                while (mListening) {

                    try {
                        mMulticastSocket.receive(receive_packet);
                    }
                    // 超时会进入下一次的while 判定, 利于中止线程
                    catch (InterruptedIOException e) {
                        Log.v(TAG, "Server listener timeout");
                        continue;
                    }
                    // 收到的包有问题就会抛出哩个异常
                    catch (IOException e) {
                        e.printStackTrace();
//                        mListening = false; // 令循环结束
                        continue;
                    }
                    serverGotClientPacketAndSend(receive_packet, unicastPort);
//                    Client_ip = new String(
//                            receive_bufffer,
//                            receive_packet.getOffset(),
//                            receive_packet.getLength()); // TODO: 改成解密
//
//                    // TODO: 判断报文内容的有效性
//
//                    Log.i(TAG, "Server got packet" +
//                            "(addr:" + receive_packet.getAddress().toString() +
//                            " port:" + receive_packet.getPort() +
//                            " len:" + receive_packet.getLength() +
//                            " off:" +  receive_packet.getOffset() + ")" +
//                            ":" + Client_ip + "(" + Client_ip.length() + "bytes)");
//
//                    // 新线程 发送本机地址, 随后客户端将会使用哩个地址向本机发起TCP连接
//                    String local_ip = getWifiIpAddress();
//                    if (local_ip == null) return;
//                    byte send_bufffer[] = "Yeah!".getBytes();
//
//                    send(mDatagramSocket, send_bufffer, receive_packet.getAddress(), unicastPort);
                }

                // 关闭单播端口
                mDatagramSocket.close();
                mDatagramSocket = null;

                // 退出广播组
                try {
                    mMulticastSocket.leaveGroup(mGroupAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMulticastSocket = null;
                mListening       = false;
                Log.w(TAG, "Server is going to stop the listen thread!");
            }
        }.start();
        
        return true;
    }


    /**
     * {@Link serverWaitClientAndSendIP} 方法专用Part1,
     * 太长无办法...
     *
     * <p>打开新线程解包, 防止监听线程阻塞
     *
     * @param packet
     * @param unicastPort 单播端口号 0 - 65535
     */
    private void serverGotClientPacketAndSend(final DatagramPacket packet, final int unicastPort){
        new Thread(){
            @Override
            public void run() {
                String i = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength()); // TODO: 改成解密

                // TODO: 判断报文内容的有效性

                Log.i(TAG, "Server got packet" +
                        "(addr:" + packet.getAddress().toString() +
                        " port:" + packet.getPort() +
                        " len:" + packet.getLength() +
                        " off:" +  packet.getOffset() + ")" +
                        ":" + i + "(" + i.length() + "bytes)");

                // 新线程 发送本机地址, 随后客户端将会使用哩个地址向本机发起TCP连接
                String local_ip = getWifiIpAddress();
                if (local_ip == null) return;
                byte send_bufffer[] = "Yeah!".getBytes();

                send(mDatagramSocket, send_bufffer, packet.getAddress(), unicastPort);
            }
        }.start();
    }


    /**
     * [作为客户端]
     * 通过UDP广播通知所有在线服务器
     * <p>当收到服务端应答, 将触发{@Link onFoundServer}事件
     * @param broadcastPort 向服务端发送探测包的端口号 0 - 65535
     * @param unicastPort 等待服务端应答的端口号 0 - 65535
     * @param interval 设置发送探测包的时间间隔
     * @return true: 运行过程顺利(不包括收发过程), false: 应该提醒用户确认Wifi 状态!
     */
    public boolean getAndConnetionServers(int broadcastPort, int unicastPort, int interval){
        if (mListening) return false;

        // 初始化socket
        if (!initSocket(broadcastPort, unicastPort)) return false;

        // 在新线程等待Server 送来IP
        if (!clientStartListenUDP(unicastPort)) return false;


        // 在新线程广播探测包
        return clientSentDetectorToServer(broadcastPort, interval);
//        String local_ip = getWifiIpAddress();
//        if (local_ip == null) return false;
//        send(local_ip.getBytes());
//        return true;
    }

    /**
     * Client监听广播
     * @param unicastPort 单播端口号 0 - 65535
     * @return
     */
    private boolean clientStartListenUDP(final int unicastPort) {
        new Thread(){
            @Override
            public void run() {
                mListening = true;

                byte receive_bufffer[] = new byte[003]; // TODO: 试验buffer 嘅长度
                final DatagramPacket receive_packet = new DatagramPacket(
                        receive_bufffer, receive_bufffer.length); //, mGroupAddress, port);

                String Client_ip;

                while (mListening) {

                    try {
//                        mMulticastSocket.receive(receive_packet);
                        mDatagramSocket.receive(receive_packet);
                    }
                    // 超时会进入下一次的while 判定, 利于中止线程
                    catch (InterruptedIOException e) {
                        Log.v(TAG, "Client listener timeout");
                        continue;
                    }
                    // 收到的包有问题就会抛出哩个异常
                    catch (IOException e) {
                        e.printStackTrace();
//                        mListening = false; // 令循环结束
                        continue;
                    }

                    Client_ip = new String(
                            receive_bufffer,
                            receive_packet.getOffset(),
                            receive_packet.getLength());


                    Log.i(TAG, "Client got packet" +
                            "(addr:" + receive_packet.getAddress().toString() +
                            " port:" + receive_packet.getPort() +
                            " len:" + receive_packet.getLength() +
                            " off:" +  receive_packet.getOffset() + ")" +
                            ":" + Client_ip + "(" + Client_ip.length() + "bytes)");

                    // TODO: 解密
                    // TODO: 判断报文内容的有效性


                    final DatagramPacket event_packet = receive_packet; // 线程间传递所需的final 指针

                    // 在主线程触发事件
                    Handler handler = new Handler(mContext.getMainLooper());
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
                            // 调用事件
                            onFoundServer(event_packet);
                        }
                    });
                }

//                // 退出广播组
//                if (mMulticastSocket != null) {
//                    try {
//                        mMulticastSocket.leaveGroup(mGroupAddress);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    mMulticastSocket = null;
//                }
                mDatagramSocket.close();
                mDatagramSocket = null;
                mListening = false;
                Log.w(TAG, "Client is going to stop the listen thread!");
            }
        }.start();

        return true;
    }

    /**
     * @Link getAndConnetionServers 方法专用Part2,
     * 太长无办法...
     *
     * 发起UDP广播通知服务端
     *
     * @return
     */
    private boolean clientSentDetectorToServer(final int broadcastPort, final int interval) {
        new Thread(){
            @Override
            public void run() {
                // TODO:
                byte[] data = ("Hello!").getBytes();

                while (mListening) {
                    send(mMulticastSocket, data, broadcastPort);

                    try {
                        sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 退出广播组
                if (mMulticastSocket != null) {
                    try {
                        mMulticastSocket.leaveGroup(mGroupAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMulticastSocket = null;
                }
                mListening = false;
            }
        }.start();
        return true;
    }

    /**
     * 客户端收到一个服务端的应答的事件
     * <p>哩个事件已经被放入主线程运行
     */
    public void onFoundServer(DatagramPacket packet){}

    /**
     * 服务端同客户端使用同样的Socket 初始化过程
     *
     * @param broadcastPort 广播端口号 0 - 65535
     * @param unicastPort 单播端口号 0 - 65535
     * @return true: 初始化过程顺利
     */
    private boolean initSocket(int broadcastPort, int unicastPort) {
        // 广播Socket
        try {
            mMulticastSocket = new MulticastSocket(broadcastPort);
            mMulticastSocket.setSoTimeout(mTimeout); // 令另一线程中的receive 不会一直阻塞在同一个地方
            mMulticastSocket.setTimeToLive(mBroadcastTTL);
            mMulticastSocket.joinGroup(mGroupAddress); // 哩句要放最后, 方便异常处理
        }
        // setSoTimeout 会引起哩个异常
        catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        // new MulticastSocket / setTimeToLive / joinGroup 会引起哩个异常
        catch (IOException e) {
            e.printStackTrace();

            // setTimeToLive 异常 同埋
            // join 唔入嘅情况:
            if (mMulticastSocket != null) mMulticastSocket = null;

            return false;
        }

        // 单播Socket
        try {
            mDatagramSocket = new DatagramSocket(unicastPort);
            mDatagramSocket.setSoTimeout(mTimeout);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 发送多播报文
     * @param socket
     * @param data 字节数据
     * @param length 数据长度, 传入-1表示使用data 的size.
     * @param address 接收方地址
     * @param port 端口号 0 - 65535
     */
    private void send(
            final DatagramSocket socket,
            final byte[] data, final int length,
            final InetAddress address, final int port) {
        // TODO: 改成可以喺同一个新线程里边运行
        new Thread() {
            @Override
            public void run() {
                int send_len = length;
                if (send_len < 0) send_len = data.length;

                // TODO: 加密data[]
                // TODO: 由于data是final, 加密后的数据要用另一个变量保存

                // 开始发送
                DatagramPacket send_packet =
                        new DatagramPacket(
                                data, send_len,
                                address, port
                        );

                try {
                    socket.send(send_packet);
                    Log.d(TAG, "Packet has been sent");
                }
                // send 会引起哩个异常
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /** @see .send */
    private void send(DatagramSocket socket, byte[] data, int port){
        send(socket, data, -1, mGroupAddress, port);
    }
    /** @see .send */
    private void send(DatagramSocket socket, byte[] data, InetAddress address, int port){
        send(socket, data, -1, address, port);
    }

    /**
     * 取得本机Wifi 地址
     * @return 返回 ***.***.***.*** 格式的字符串
     */
    public String getWifiIpAddress() {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager)(mContext.getSystemService(Context.WIFI_SERVICE));
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
//        String ip = intToIp(ipAddress);
//        EditText et = (EditText)findViewById(R.id.EditText01);
//        et.setText(ip);

        if (ipAddress == 0){
            Log.w(TAG, "Unable to get Wifi IP address!");
            return null;
        }

        return          (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8 ) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                ((ipAddress >> 24) & 0xFF) ;
    }








    //客户端发送数据实现：
    protected void connectServerWithUDPSocket() {

        try {
            //创建DatagramSocket对象并指定一个端口号，注意，如果客户端需要接收服务器的返回数据,
            //还需要使用这个端口号来receive，所以一定要记住
            mDatagramSocket = new DatagramSocket(1985);
            //使用InetAddress(Inet4Address).getByName把IP地址转换为网络地址
            InetAddress serverAddress = InetAddress.getByName("192.168.1.32");
            //Inet4Address serverAddress = (Inet4Address) Inet4Address.getByName("192.168.1.32");
            String str = "[2143213;21343fjks;213]";//设置要发送的报文
            byte data[] = str.getBytes();//把字符串str字符串转换为字节数组
            //创建一个DatagramPacket对象，用于发送数据。
            //参数一：要发送的数据  参数二：数据的长度  参数三：服务端的网络地址  参数四：服务器端端口号
            DatagramPacket packet = new DatagramPacket(data, data.length ,serverAddress ,10025);
            mDatagramSocket.send(packet);//把数据发送到服务端。
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //客户端接收服务器返回的数据：
    public void ReceiveServerSocketData() {
        DatagramSocket socket;
        try {
            //实例化的端口号要和发送时的socket一致，否则收不到data
            socket = new DatagramSocket(1985);
            byte data[] = new byte[4 * 1024];
            //参数一:要接受的data 参数二：data的长度
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            //把接收到的data转换为String字符串
            String result = new String(packet.getData(), packet.getOffset(),
                    packet.getLength());
            socket.close();//不使用了记得要关闭
            System.out.println("the number of reveived Socket is  :" + "udpData:" + result);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 服务器接收客户端实现：
    public void ServerReceviedByUdp(){
        //创建一个DatagramSocket对象，并指定监听端口。（UDP使用DatagramSocket）
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(10025);
            //创建一个byte类型的数组，用于存放接收到得数据
            byte data[] = new byte[4*1024];
            //创建一个DatagramPacket对象，并指定DatagramPacket对象的大小
            DatagramPacket packet = new DatagramPacket(data,data.length);
            //读取接收到得数据
            socket.receive(packet);
            //把客户端发送的数据转换为字符串。
            //使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长
            String result = new String(packet.getData(),packet.getOffset() ,packet.getLength());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
