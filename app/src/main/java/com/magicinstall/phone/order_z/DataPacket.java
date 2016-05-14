package com.magicinstall.phone.order_z;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 各种网络/各种进程之间转递数据用的包
 * 包格式:
 *      CRC         (uint32)
 *      ID          (uint32)  (全局的包标识)
 *      此包总长     (uint32)  (不包括CRC, 包括数据头)
 *      此包索引     (uint32)
 *      完整包总长   (uint32)  (不包括CRC, 包括数据头)
 *      分包总数     (uint32)
 *      第一项的偏移 (uint32)
 *      [... 扩展数据头 ...]
 *
 *      [第一项的长度 (uint32) 第一项的内容     ...]  [除了第一个分包以外,
 *      [第二项...                           ...]    此处直接接上一个包的未完部分]
 *      [第...                              ...]   [第N+1项...            ...]
 *      [第N项...                           ...]
 *
 * Created by wing on 16/5/13.
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
     */
    public void make(byte[] extend, RSA rsa) {
    }

    /**
     * 生成报文
     * <p>不需要加密的包, 使用的拆包算法不同
     * <p>生成的结果使用packet系列的方法取得.
     * @param extend 数据头的扩展数据
     */
    public void make(byte[] extend) {
        if (mItemNodeHead == null) return;

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
            Log.w(TAG, "每包的最大长度值设置过小, 还不够放数据头!");
            return;
        }

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
        // 以项为循环单位, 一边读一边分包
        moveToFirstItem();
        do {
            try {
                output_stream.write(Hash.int2byte32(getCurrentItem().data.length)); // 写入此项的长度
                output_stream.write(getCurrentItem().data); // 写入此项的内容
            } catch (IOException e) {
                Log.e(TAG, "make() 写入数据流错误");
            }

            //

            // 判断够一个包未, 使用循环系因为有可能有某个项的长度跨几个包
            while (output_stream.size() >= mLimit) {
                this_out = output_stream.toByteArray();
//                try {
//                    output_stream.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "make() 数据流关闭错误");
//                }

                // 新建一个包
                this_packet = pushPacket(new byte[mLimit]);
                packet_cnt ++;
                all_packet_len += mLimit;
                // 写入数据
                System.arraycopy(this_out, 0, this_packet.data, 0, mLimit);



                // 将后半部写入数据流
                output_stream = new ByteArrayOutputStream(head_length + this_out.length - mLimit);
                try {
                    output_stream.write(new byte[head_length]); // 先写入数据头占位
                } catch (IOException e) {
                    Log.e(TAG, "make() 写入数据流错误");
                }
                output_stream.write(this_out, mLimit, this_out.length - mLimit); // 写入后半部

            }
        } while (moveNextItem() != null);

        // 写最后一个包
        this_out = output_stream.toByteArray();
//        try {
//            output_stream.close();
//        } catch (IOException e) {
//            Log.e(TAG, "make() 数据流关闭错误");
//        }
        output_stream = null;

        // 新建一个包
        this_packet = pushPacket(this_out);
        packet_cnt ++;
        all_packet_len += this_out.length;
        // 写入数据
//        System.arraycopy(this_out, 0, this_packet.data, 0, this_out.length);

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
                    .setExtend(extend)
                    .setCRC()/*CRC要最后先计*/;

            idx ++;
        } while (moveNextPacket() != null);

        // 全局的包标识递增
        mPacketID ++;
    }
    /** @see .make()*/
    public void make() {
        make(null, null);
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
     * @return 返回此包的ID
     */
    public int resolve(byte[] extend) {
        if (mPacketNodeHead == null) return -1;

        clearItems();

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
//            // 先取出第一个项的长度字节地址
            read_offset = getCurrentPacket().getFirstItemOffset();
//            this_item_length = Hash.byte32ToInt(getCurrentPacket().data, read_offset);
//            read_offset += 4;

            packet_data_leave_len = getCurrentPacket().data.length - read_offset;

            // 循环判断哩个包读完未
            while (packet_data_leave_len > 0) {
                // 读出项的长度
                this_item_length = Hash.byte32ToInt(getCurrentPacket().data, read_offset);
                read_offset += 4;
                packet_data_leave_len -= 4;
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

                    if (write_offset >= this_item_data.length) break; // 主要用哩度退出

                    moveNextPacket();
                    // 取出分包数据开头地址
                    read_offset = getCurrentPacket().getFirstItemOffset();
                    packet_data_leave_len = getCurrentPacket().data.length - read_offset;

                } /*while(write_offset < this_item_data.length);*/


                pushItem(this_item_data);
            }


        } while (moveNextPacket() != null);

        clearPackets();

        return id;
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
//        PacketNode node = new PacketNode(data);
//        // 插入第一个项
//        if (mPacketNodeHead == null) {
//            mPacketNodeHead = node;
//            mPacketNodeLast = node;
//            mPacketNodeCursor = node;
//        }
//        // 插入后面的项
//        else {
//            mPacketNodeLast.next = node;
//            mPacketNodeLast = node;
//        }
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
    public void moveToFirstItem() {
        mItemNodeCursor = mItemNodeHead;
    }

    /**
     * 将游标移至链表头
     */
    public void moveToFirstPacket() {
        mPacketNodeCursor = mPacketNodeHead;
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
        private static final int          CRC_OFFSET =  0; // 此包的校验码
        private static final int           ID_OFFSET =  4; // 完整包的ID
        private static final int       LENGTH_OFFSET =  8; // 此包总长
        private static final int        INDEX_OFFSET = 12; // 此包索引
        private static final int   ALL_LENGTH_OFFSET = 16; // 完整包总长
        private static final int PACKET_COUNT_OFFSET = 20; // 分包总数
        private static final int   FIRST_ITEM_OFFSET = 24; // 第一项的偏移
        private static final int       EXTEND_OFFSET = 28; // 扩展数据(如果有)偏移

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


}
