package com.magicinstall.library;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 各种网络/各种进程之间转递数据用的包
 * <p>各模块继承哩个类, 子类实现具体数据的处理.
 * <p>包格式:
 * <p>     CRC         (uint32)
 * <p>     ID          (uint32)  (全局的包标识)
 * <p>     此包总长     (uint32)  (不包括CRC, 包括数据头)
 * <p>     此包索引     (uint32)
 * <p>     完整包总长   (uint32)  (不包括CRC, 包括数据头)
 * <p>     分包总数     (uint32)
 * <p>     第一项的偏移 (uint32)
 * <p>     [... 扩展数据头 ...]
 * <p>
 * <p>     [第一项的长度 (uint32) 第一项的内容     ...]  [除了第一个分包以外,
 * <p>     [第二项...                           ...]    此处直接接上一个包的未完部分]
 * <p>     [第...                              ...]   [第N+1项...            ...]
 * <p>     [第N项...                           ...]
 *
 * <p>Created by wing on 16/5/13.
 */
public class DataPacket {
    private static final String TAG = "DataPacket";
    public static final int SYSTEM_LIMIT_LENGTH = 500000; // 系统机能限制长度 TODO: 目前各种传言, 选一个折中的

    protected Node mItemNodeHead = null; // 链表头
    protected Node mItemNodeLast = null; // 链表尾
    protected Node mItemNodeCursor = null; // 链表读取指针

    protected PacketNode mPacketNodeHead = null; // 链表头
    protected PacketNode mPacketNodeLast = null; // 链表尾
    protected PacketNode mPacketNodeCursor = null; // 链表读取指针

    /**
     * 全局的包标识
     * <p>每个完整包的标识都是相对递增;
     * 同一个完整包的每个分包, 此标识都是相同的.
     */
    protected static int mPacketID = 0;

    /** 接收到此包时, 将socket 的远程地址赋予该值 */
    public InetAddress remoteAddress = null;

    private int mLimit = SYSTEM_LIMIT_LENGTH; // 默认值
    /**
     * 设置每包的最大长度
     * <p>哩个方法唔会刷新已经生成的数据
     * 只会对设置以后才生成的数据起作用,
     * 如果改变哩个值, 请自行处理好逻辑.
     * @param limit 传入 -1表示不限长度;
     */
    public void setLimit(int limit) {
        mLimit = limit;
    }
    /** @see .setLimit() */
    public int getLimit(){
        return mLimit;
    }

    private RSA mRsa = null;


    private ArrayList<byte[]> mItems = null;
    /**
     * 取得所有项的内容
     */
    public ArrayList<byte[]> getItems() {
        return mItems;
    }

    /**
     * 构造
     */
    public DataPacket() {}
    /**
     * 网络Send 包用的构造
     * <p>需要将内容分包的时候使用此构造.
     * @param limit 指示每包的最大长度.
     * @param rsa 传入RSA的话, 生成的报文将被加密,
     *            传入null 则不加密.
     */
    public DataPacket(int limit, RSA rsa) {
        mLimit = limit;
        mRsa = rsa;
    }

    /**
     * 网络收包用的构造
     * @param datas
     * @param rsa 传入RSA的话, 会先进行解密,
     *            传入null 则不加密.
     */
    public DataPacket(List<byte[]> datas, RSA rsa) {
        mRsa = rsa;
    }

    /**
     * 生成报文
     * <p>需要加密的包, 使用的拆包算法不同
     * <p>生成的结果使用packet系列的方法取得.
     * @param extend 数据头的扩展数据
     * @param rsa 传入RSA的话, 会先进行加密,
     *            传入null 则不加密.
     */
    public void make(byte[] extend, RSA rsa) {
//        if (mItemNodeHead == null) return;

        clearPackets();

        // 先计算数据头的长度
        final int head_length =
                4/*CRC         (uint32)*/ +
                4/*ID          (uint32)*/ +
                4/*此包总长     (uint32)*/ +
                4/*此包索引     (uint32)*/ +
                4/*完整包总长   (uint32)*/ +
                4/*分包总数     (uint32)*/ +
                4/*第一项的偏移 (uint32)*/ +
                ((extend != null) ? extend.length : 0);

        if (mLimit < (head_length + 4/*第一个项的长度值*/)) {
            Log.e(TAG, "每包的最大长度值设置过小, 还不够放数据头!");
            return;
        }

        int limit = mLimit; // 不加密的话使用预设的上限

        // 先测试加密后的长度有冇超长
        if (rsa != null) {
            final int block_size = rsa.getEncryptBlockSize();
            if (block_size < 1) {
                Log.e(TAG, "取得加密块长度错误");
                return;
            }
            byte test_data[] = new byte[block_size];

            // 测试最小值
            limit = rsa.encrypt(test_data).length;
            if (mLimit < limit + 4/*CRC*/) {
                Log.e(TAG, "每包的最大长度值设置过小, 还不够放一个加密块!");
                return;
            }

            // 测试最大值
            int block_count = ((mLimit - 4/*CRC*/) / block_size) + 1;
            do {
                test_data = new byte[block_count * block_size];
                limit = rsa.encrypt(test_data).length + 4/*CRC*/;
                if ((limit + 4/*CRC*/) < mLimit) break;

                block_count --;
            } while ((limit + 4/*CRC*/) > mLimit);

            // 得到加密前的长度限制, 使用哩个长度, 即使后面加密后亦不会超长
            limit = block_count * block_size + 4/*CRC*/;
        }


        // 先处理第一个分包的数据头
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream(head_length);
        try {
            output_stream.write(new byte[head_length]); // 先写入数据头占位
        } catch (IOException e) {
            Log.e(TAG, "make() 写入数据流错误");
        }

        Node this_packet = null;
        byte this_out[] = null;
        int all_packet_len = 0;
        int packet_cnt = 0;

        if (moveToFirstItem()) {
            // 以项为循环单位, 一边读一边分包
            do {
                try {
                    if (getCurrentItem().data != null) {
                        output_stream.write(Hash.int2byte32(getCurrentItem().data.length)); // 写入此项的长度
                        output_stream.write(getCurrentItem().data); // 写入此项的内容
                    }
                    // 空的项只写入0长度
                    else
                        output_stream.write(new byte[]{0, 0, 0, 0});

                } catch (IOException e) {
                    Log.e(TAG, "make() 写入数据流错误");
                }

                // 判断够一个包未, 使用循环系因为有可能有某个项的长度跨几个包
                while (output_stream.size() >= limit) {
                    this_out = output_stream.toByteArray();
//                try {
//                    output_stream.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "make() 数据流关闭错误");
//                }

                    // 新建一个包
                    this_packet = pushPacket(new byte[limit]);
                    packet_cnt++;
                    all_packet_len += limit;
                    // 写入数据
                    System.arraycopy(this_out, 0, this_packet.data, 0, limit);

                    // 将后半部写入数据流
                    output_stream = new ByteArrayOutputStream(head_length + this_out.length - limit);
                    try {
                        output_stream.write(new byte[head_length]); // 先写入数据头占位
                    } catch (IOException e) {
                        Log.e(TAG, "make() 写入数据流错误");
                    }
                    output_stream.write(this_out, limit, this_out.length - limit); // 写入后半部

                }
            } while (moveNextItem() != null);
        }

        // 写最后一个包
        this_out = output_stream.toByteArray();

        // 插入一个包, 并写入数据
        pushPacket(this_out);

        // 统计
        packet_cnt ++;
        all_packet_len += this_out.length;

        // 清空项链表释放内存
        clearItems();

        // 全部包拆完之后, 最后再逐个包填写数据头
        int idx = 0;
        moveToFirstPacket();
        do {
            getCurrentPacket()
                    .setID(mPacketID)
                    .setLength()
                    .setIndex(idx)
                    .setAllLength(all_packet_len)
                    .setPacketCount(packet_cnt)
                    .setFirstItemOffset(head_length)
                    .setExtend(extend);

            // 加密, 从第一项的指针开始加密
            if (rsa != null) {
                output_stream = new ByteArrayOutputStream(PacketNode.EXTEND_OFFSET);
                try {
                    // 写入第一项的指针之前的数据头
                    output_stream.write(
                            getCurrentPacket().data, 0, PacketNode.FIRST_ITEM_OFFSET);
                    // 写入加密后的数据
                    output_stream.write(
                            rsa.encrypt(
                                    getCurrentPacket().data,
                                    PacketNode.FIRST_ITEM_OFFSET/*从第一项的指针开始加密*/,
                                    getCurrentPacket().data.length - PacketNode.FIRST_ITEM_OFFSET/*同上*/));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte rsa_buffer[] = output_stream.toByteArray();
                // 重新检查一次有冇超长
                if (rsa_buffer.length > mLimit) {
                    Log.w(TAG, "加密后的数据超过长度限制, 上限:" + mLimit + " 数据长度:" + rsa_buffer.length);
                }
                getCurrentPacket().data = rsa_buffer;
            }

            // CRC要最后先计
            getCurrentPacket().setCRC();

            idx ++;
        } while (moveNextPacket() != null);

        // 全局的包标识递增
        mPacketID ++;
    }

    /**
     * 生成报文
     * <p>不需要加密的包, 使用的拆包算法不同
     * <p>生成的结果使用packet系列的方法取得.
     * @param extend 数据头的扩展数据
     */
    public void make(byte[] extend) {
        make(extend, null);
    }
    /** @see .make()*/
    public void make() {
        make(null, null);
    }

    /** @see .resolve()*/
    public void resolve() {
        resolve(null, null);
    }

    /**
     * 拆包生成每个项
     * <P>拆完之后在Item 链表取得每个项的数据.
     * 为节省内存, 原有的Item 数据将在拆包时被清除.
     * <p>注意: 此方法是假定所有分包已正常插入,
     *         因此在pushPacket 之前,
     *         应该自行实现判断每个包的CRC/ID/索引号/分包总数 等,
     *         令到每个分包是正确的并按顺序插入,
     *         否则拆包过程将会出错.
     *
     * @param extend 传入已分配的缓冲, 从哩个参数返回扩展数据;
     *               传入null 则无视扩展数据, 而由于拆包之后,
     *               包的数据将清除, 因此以后也取不到扩展数据!
     * @param rsa 传入RSA的话, 会先进行解密,
     *            传入null 则不解密.
     * @return 返回此包的ID
     */
    public int resolve(byte[] extend, RSA rsa) {
        if (mPacketNodeHead == null) return -1;

        // 先行解密
        if (rsa != null) {
            ByteArrayOutputStream decrypt_Stream;
            moveToFirstPacket();
            do {
                decrypt_Stream = new ByteArrayOutputStream(PacketNode.EXTEND_OFFSET);
                try {
                    // 写入第一项的指针之前的数据头
                    decrypt_Stream.write(
                            getCurrentPacket().data, 0, PacketNode.FIRST_ITEM_OFFSET);
                    decrypt_Stream.write(
                            // 解密
                            rsa.decrypt(
                                    getCurrentPacket().data,
                                    PacketNode.FIRST_ITEM_OFFSET/*从第一项的指针开始加密*/,
                                    getCurrentPacket().data.length - PacketNode.FIRST_ITEM_OFFSET/*同上*/)
                    );
                } catch (IOException e) {
                    Log.e(TAG, "字节流写入错误"); return -1;
                }
                getCurrentPacket().data = decrypt_Stream.toByteArray();
            } while (moveNextPacket() != null);

            decrypt_Stream = null;
        }

        // 解密之后的拆包同非加密的一样
        return resolve(extend);
    }

    /**
     * 拆包生成每个项
     * <P>拆完之后在Item 链表取得每个项的数据.
     * 为节省内存, 原有的Item 数据将在拆包时被清除.
     * <p>注意: 此方法是假定所有分包已正常插入,
     *         因此在pushPacket 之前,
     *         应该自行实现判断每个包的CRC/ID/索引号/分包总数 等,
     *         令到每个分包正确插入,
     *         否则拆包过程将会出错.
     *
     * @param extend 传入已分配的缓冲, 从哩个参数返回扩展数据;
     *               传入null 则无视扩展数据, 而由于拆包之后,
     *               包的数据将清除, 因此以后也取不到扩展数据!
     * @return 返回此包的ID
     */
    public int resolve(byte[] extend) {
        if (mPacketNodeHead == null) return -1;

        clearItems();
        sortPacket(); // 分包按索引号排序

//        // 先计算数据头的长度
//        final int head_length =
//                4/*CRC         (uint32)*/ +
//                4/*ID          (uint32)*/ +
//                4/*此包总长     (uint32)*/ +
//                4/*此包索引     (uint32)*/ +
//                4/*完整包总长   (uint32)*/ +
//                4/*分包总数     (uint32)*/ +
//                4/*第一项的偏移 (uint32)*/ +
//                ((extend != null) ? extend.length : 0);

        // 只取第一个包的数据头
        moveToFirstPacket();
        int id     = getCurrentPacket().getID();
        int length = getCurrentPacket().getAllLength();
        int count  = getCurrentPacket().getPacketCount();
        int write_length = 0;
        // 返回扩展数据
        if (extend != null) {
            byte ex[] = getCurrentPacket().getExtend();
            write_length = (ex.length > extend.length) ? extend.length : ex.length;
            System.arraycopy(ex, 0, extend, 0, write_length);
        }

        int read_offset = 0;
        int packet_data_leave_len = 0;
        int this_item_length = 0;
        int write_offset = 0;

        // 项的内容
        byte this_item_data[] = null;
        // 以包为单位循环读出Item
        do {
            // 先取出第一个项的长度字节地址
            read_offset = getCurrentPacket().getFirstItemOffset();

            packet_data_leave_len = getCurrentPacket().data.length - read_offset;

            // 循环判断哩个包读完未
            while (packet_data_leave_len > 0) {
                // 项长度的字节刚好卡在两个包中间
                // 将下一个包从第一项偏移地址拆开, 将当前包的剩余字节插入
                if (packet_data_leave_len < 4) {
                    byte leave_data[] = new byte[packet_data_leave_len];
                    System.arraycopy(
                            getCurrentPacket().data, getCurrentPacket().data.length - packet_data_leave_len,
                            leave_data, 0,
                            packet_data_leave_len);

                    // 指向下一个分包
                    if (moveNextPacket() == null) {
                        Log.e(TAG, "resolve() 读到空的分包!");
                        return -1;
                    }

                    ByteArrayOutputStream rebuild_Stream =
                            new ByteArrayOutputStream(
                                    getCurrentPacket().data.length + packet_data_leave_len );
                    int first_item_offset = getCurrentPacket().getFirstItemOffset();
                    // 先写入分包的数据头
                    rebuild_Stream.write(getCurrentPacket().data, 0, first_item_offset);
                    // 插入上一个包的剩余字节
                    rebuild_Stream.write(leave_data, 0, leave_data.length);
                    // 写后面的数据
                    rebuild_Stream.write(
                            getCurrentPacket().data,
                            first_item_offset,
                            getCurrentPacket().data.length - first_item_offset);

                    // 替换分包, 并设置初始量
                    getCurrentPacket().data = rebuild_Stream.toByteArray();
                    read_offset = getCurrentPacket().getFirstItemOffset();
                    packet_data_leave_len = getCurrentPacket().data.length - read_offset;
                }

                /*-- 哩度先正式开始读分包 --*/
                // 读出项的长度
                this_item_length = Hash.byte32ToInt(getCurrentPacket().data, read_offset);
                read_offset += 4;
                packet_data_leave_len -= 4;

                // 0长度就直接插一个空的项
                if (this_item_length == 0) {
                    pushItem((byte[]) null);
                    continue;
                }

                // 分配新项的缓冲区
                this_item_data = new byte[this_item_length];
                write_offset = 0;

                // 循环至读完一个项
                while (true) {
                    // 超过当前包的长度
                    if ((this_item_data.length - write_offset) > packet_data_leave_len)
                        // 先读完当前包
                        write_length = packet_data_leave_len;
                    // 当前包够晒长
                    else
                        // 写完此项的剩余部分
                        write_length = this_item_data.length - write_offset;

                    System.arraycopy(
                            getCurrentPacket().data, read_offset,
                            this_item_data, write_offset,
                            write_length);

                    write_offset += write_length;
                    read_offset += write_length;
                    packet_data_leave_len -= write_length;

                    // 偏移值大于等于项的长度, 即是当前项已经读完
                    if (write_offset >= this_item_data.length) break; // 主要用哩度退出

                    // 指向下一个分包
                    if (moveNextPacket() == null) {
                        Log.e(TAG, "resolve() 读到空的分包!");
                        return -1;
                    }
                    // 取出分包数据开头地址
                    read_offset = getCurrentPacket().getFirstItemOffset();
                    packet_data_leave_len = getCurrentPacket().data.length - read_offset;
                }

                pushItem(this_item_data);
            }


        } while (moveNextPacket() != null);

        clearPackets();

        return id;
    }

    /**
     * 对收齐的分包按索引号进行排序
     * <p>一般分包都是按顺序压入的, 只有个别会因为线程的关系后退了几个位置,
     * 而且索引号一定是连续的(从0开始),
     * 基于哩个前提, 此算法过程是逐次比较期待的索引号,
     * 当发现分包的索引号不是期待索引号, 就向下遍历链表,
     * 找到符合的分包后, 就将分包从链表拆除, 然后插入到对应位置,
     * 由于上面的前提, 一般不会遍历到整个链表, 所以速度不会太慢.
     * <p>哩个方法假设链表不是空的, 而且所有分包都已经压入.
     */
    private void sortPacket(){
        PacketNode expected_packet = null;

        // 先排好第一个包
        if (mPacketNodeHead.getIndex() != 0) {
            PacketNode cursor = mPacketNodeHead;
            // 遍历至期待的包的前一个包
            while (((PacketNode)(cursor.next)).getIndex() != 0) {
                cursor = (PacketNode) cursor.next;
            }
            // 从链表移除期待包
            expected_packet = (PacketNode) cursor.next;
            cursor.next = cursor.next.next;
            // 将期待包移到链表头
            expected_packet.next = mPacketNodeHead.next;
            mPacketNodeHead = expected_packet;
        }

        int expected_index = 1;
        moveToFirstPacket();
        while (getCurrentPacket().next != null){
            // 当前包的下一个包不是期待的包
            if (((PacketNode)(getCurrentPacket().next)).getIndex() != expected_index) {
                PacketNode cursor = (PacketNode)(getCurrentPacket().next);
                // 遍历至期待的包
                while (((PacketNode)(cursor.next)).getIndex() != expected_index) {
                    cursor = (PacketNode) cursor.next;
                }
                // 从链表移除期待包
                expected_packet = (PacketNode) cursor.next;
                cursor.next = cursor.next.next;
                // 将期待包插入到当前位置
                expected_packet.next = getCurrentPacket().next;
                getCurrentPacket().next = expected_packet;
            }
            expected_index ++;
            moveNextPacket();
        }
    }

    /************************************************************************
     *                             链表基本操作                               *
     ************************************************************************/

    /**
     * 向链表插入一项
     * @param data
     * @return
     */
    public Node pushItem(byte[] data){
        return pushItem(new Node(data));
    }
    public Node pushItem(String data){
        if (data == null) return pushItem(new Node());

        return pushItem(new Node(data.getBytes()));
    }
    public Node pushItem(int data){
        return pushItem(new Node(Hash.int2byte32(data)));
    }
    public Node pushItem(Node node) {
        // 插入第一个项
        if (mItemNodeHead == null) {
            mItemNodeHead = node;
            mItemNodeLast = node;
            mItemNodeCursor = node;
        }
        // 插入后面的项
        else {
            mItemNodeLast.next = node;
            mItemNodeLast = node;
        }
        return node;
    }
    /**
     * 向链表插入一个包
     * @param data
     * @return
     */
    public PacketNode pushPacket(byte[] data){
        return pushPacket(new PacketNode(data));
    }
    public PacketNode pushPacket(PacketNode node) {
        // 插入第一个项
        if (mPacketNodeHead == null) {
            mPacketNodeHead = node;
            mPacketNodeLast = node;
            mPacketNodeCursor = node;
        }
        // 插入后面的项
        else {
            mPacketNodeLast.next = node;
            mPacketNodeLast = node;
        }
        return node;
    }

    /**
     * 清空链表
     */
    public void clearItems() {
        mItemNodeCursor = mItemNodeHead;
        while (mItemNodeCursor != null) {
            mItemNodeHead = mItemNodeCursor.next;
            mItemNodeCursor.data = null;
            mItemNodeCursor.next = null;
            mItemNodeCursor = mItemNodeHead;
        }
        mItemNodeLast = null;
    }
    /**
     * 清空链表
     */
    public void clearPackets() {
        mPacketNodeCursor = mPacketNodeHead;
        while (mPacketNodeCursor != null) {
            mPacketNodeHead = (PacketNode) mPacketNodeCursor.next;
            mPacketNodeCursor.data = null;
            mPacketNodeCursor.next = null;
            mPacketNodeCursor = mPacketNodeHead;
        }
        mPacketNodeLast = null;
    }

    /**
     * 将游标移至链表头
     */
    public boolean moveToFirstItem() {
        mItemNodeCursor = mItemNodeHead;
        return (mItemNodeCursor != null);
    }

    /**
     * 将游标移至链表头
     */
    public PacketNode moveToFirstPacket() {
        mPacketNodeCursor = mPacketNodeHead;
        return mPacketNodeCursor;
    }

    /**
     * 取得当前项
     * @return
     */
    public Node getCurrentItem() {
        return mItemNodeCursor;
    }
    /**
     * 取得当前包
     * @return
     */
    public PacketNode getCurrentPacket() {
        return mPacketNodeCursor;
    }

    /**
     * 下移
     * @return
     */
    public Node moveNextItem() {
        if (mItemNodeCursor == null) return null;
        mItemNodeCursor = mItemNodeCursor.next;
        if (mItemNodeCursor == null) return null;
        return mItemNodeCursor;
    }
    /**
     * 下移
     * @return
     */
    public Node moveNextPacket() {
        if (mPacketNodeCursor == null) return null;
        mPacketNodeCursor = (PacketNode) mPacketNodeCursor.next;
        if (mPacketNodeCursor == null) return null;
        return mPacketNodeCursor;
    }

    public int getPacketCount(){
        // 用一个新的指针, 不影响实例的指针
        Node cursor = mPacketNodeHead;
        int count = 0;
        while (cursor != null) {
            count ++;
            cursor = cursor.next;
        }
        return count;
    }



    /**
     * 连接整个完整包各项内容(不包括数据头)的节点
     */
    public class Node {
        public Node(){}
        public Node(byte[] data){
            this.data = data;
        }

        public byte[] data = null;
        public Node next = null;
    }

    /**
     * 分包的节点
     */
    public class PacketNode extends Node {
        public static final int          CRC_OFFSET =  0; // 此包的校验码
        public static final int           ID_OFFSET =  4; // 完整包的ID
        public static final int       LENGTH_OFFSET =  8; // 此包总长
        public static final int        INDEX_OFFSET = 12; // 此包索引
        public static final int   ALL_LENGTH_OFFSET = 16; // 完整包总长
        public static final int PACKET_COUNT_OFFSET = 20; // 分包总数
        public static final int   FIRST_ITEM_OFFSET = 24; // 第一项的偏移
        public static final int       EXTEND_OFFSET = 28; // 扩展数据(如果有)偏移

        public PacketNode(byte[]data) {
            super(data);
        }

        /** 打包解包时用的临时变量 */
//        public int index = 0, offset = 0, length = 0;


        public PacketNode setCRC() {
            byte crc[] = Hash.int2byte32(
                    CRC32.classCalculateCRC32With8Bit(
                            data, ID_OFFSET/*第二个值的偏移, 下同*/, data.length - ID_OFFSET
                    ));
            data[CRC_OFFSET + 0] = crc[0];
            data[CRC_OFFSET + 1] = crc[1];
            data[CRC_OFFSET + 2] = crc[2];
            data[CRC_OFFSET + 3] = crc[3];
            return this;
        }
        public int getCRC() {
            return Hash.byte32ToInt(data, CRC_OFFSET);
        }

        /**
         * 自己校验CRC
         * @return true: 校验正确
         */
        public boolean verifyCRC() {
            return (CRC32.classCalculateCRC32With8Bit(
                    data, ID_OFFSET/*第二个值的偏移, 下同*/, data.length - ID_OFFSET)) ==
                    getCRC();
        }

        public PacketNode setID(int id) {
            byte _id[] = Hash.int2byte32(id);
            data[ID_OFFSET + 0] = _id[0];
            data[ID_OFFSET + 1] = _id[1];
            data[ID_OFFSET + 2] = _id[2];
            data[ID_OFFSET + 3] = _id[3];
            return this;
        }
        public int getID() {
            return Hash.byte32ToInt(data, ID_OFFSET);
        }

        public PacketNode setLength() {
            byte len[] = Hash.int2byte32(data.length);
            data[LENGTH_OFFSET + 0] = len[0];
            data[LENGTH_OFFSET + 1] = len[1];
            data[LENGTH_OFFSET + 2] = len[2];
            data[LENGTH_OFFSET + 3] = len[3];
            return this;
        }
        public int getLength() {
            return Hash.byte32ToInt(data, LENGTH_OFFSET);
        }


        public PacketNode setIndex(int index) {
            byte idx[] = Hash.int2byte32(index);
            data[INDEX_OFFSET + 0] = idx[0];
            data[INDEX_OFFSET + 1] = idx[1];
            data[INDEX_OFFSET + 2] = idx[2];
            data[INDEX_OFFSET + 3] = idx[3];
            return this;
        }
        public int getIndex() {
            return Hash.byte32ToInt(data, INDEX_OFFSET);
        }

        public PacketNode setAllLength(int length) {
            byte len[] = Hash.int2byte32(length);
            data[ALL_LENGTH_OFFSET + 0] = len[0];
            data[ALL_LENGTH_OFFSET + 1] = len[1];
            data[ALL_LENGTH_OFFSET + 2] = len[2];
            data[ALL_LENGTH_OFFSET + 3] = len[3];
            return this;
        }
        public int getAllLength() {
            return Hash.byte32ToInt(data, ALL_LENGTH_OFFSET);
        }

        public PacketNode setPacketCount(int count) {
            byte cnt[] = Hash.int2byte32(count);
            data[PACKET_COUNT_OFFSET + 0] = cnt[0];
            data[PACKET_COUNT_OFFSET + 1] = cnt[1];
            data[PACKET_COUNT_OFFSET + 2] = cnt[2];
            data[PACKET_COUNT_OFFSET + 3] = cnt[3];
            return this;
        }
        public int getPacketCount() {
            return Hash.byte32ToInt(data, PACKET_COUNT_OFFSET);
        }

        public PacketNode setFirstItemOffset(int offset) {
            byte off[] = Hash.int2byte32(offset);
            data[FIRST_ITEM_OFFSET + 0] = off[0];
            data[FIRST_ITEM_OFFSET + 1] = off[1];
            data[FIRST_ITEM_OFFSET + 2] = off[2];
            data[FIRST_ITEM_OFFSET + 3] = off[3];
            return this;
        }
        public int getFirstItemOffset() {
            return Hash.byte32ToInt(data, FIRST_ITEM_OFFSET);
        }


        public PacketNode setExtend(byte[] extend) {
            int length = getFirstItemOffset() - EXTEND_OFFSET;
            if (length > 0)
                System.arraycopy(extend, 0, data, EXTEND_OFFSET, length);
            return this;
        }
        public byte[] getExtend() {
            int length = getFirstItemOffset() - EXTEND_OFFSET;
            if (length < 1) return null;

            byte extend[] = new byte[length];
            System.arraycopy(data, EXTEND_OFFSET, extend, 0, length);
            return extend;
        }


    }

    /**
     * 校验二进制分包的CRC
     * @param data
     * @return
     */
    public static boolean verifyPacketCRC(byte data[]) {
        return (CRC32.classCalculateCRC32With8Bit(
                data,
                PacketNode.ID_OFFSET/*第二个值的偏移, 下同*/,
                data.length - PacketNode.ID_OFFSET))
                ==
                Hash.byte32ToInt(data, PacketNode.CRC_OFFSET);
    }

    /**
     * 取得报文id
     * @param data
     * @return
     */
    public static int getID(byte data[]) {
        return Hash.byte32ToInt(data, PacketNode.ID_OFFSET);
    }

    /**
     * 取得分包总数
     * @param data
     * @return
     */
    public static int getPacketCount(byte data[]) {
        return Hash.byte32ToInt(data, PacketNode.PACKET_COUNT_OFFSET);
    }

    /**
     * 取得分包索引号
     */
    public static int getPacketIndex(byte data[]) {
        return Hash.byte32ToInt(data, PacketNode.INDEX_OFFSET);
    }
}
