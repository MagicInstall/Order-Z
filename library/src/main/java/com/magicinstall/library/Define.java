package com.magicinstall.library;

/**
 * 整套App 共用的一些定义值
 * Created by wing on 16/5/25.
 */
public class Define {
    public static final String UDP_BROADCAST_GROUP = "224.0.0.1";
    public static final int UDP_BROADCAST_PORT = 13130;
    public static final int UDP_UNICAST_PORT = 13139;
    public static final int RSA_KEY_LENGTH = 1024/*bit*/;
    public static final int TCP_PORT = 13139;


    /************************************************************************
     *                          报文/数据库查询用                             *
     ************************************************************************/
    public static final int PACKET_LIMIT_LENGTH =
            DataPacket.PacketNode.FIRST_ITEM_OFFSET/*第一项的偏移*/ +
            RSA_KEY_LENGTH / 8 * 20;

    /** 预设扩展数据的长度,
     *  方便各种计算*/
    public static final int EXTEND_LIMIT_LENGTH = 2/*byte*/;

    /** Activity 在扩展数据中的偏移地址,
     *  客户端记低喺边个Activity 请求哩个命令的,
     *  服务端要原封不动返回哩个字节*/
    public static final int EXTEND_ACTIVITY_OFFSET = 1;

    /** 命令字节在报文的扩展数据中的偏移地址,
     *  代表服务端需执行的查询指令,
     *  服务端要原封不动返回哩个字节 */
    public static final int EXTEND_COMMAND_OFFSET = 0;
    /************************************************************************
     *                         客户端/服务端共用命令                          *
     *                           共用命令从200 起                            *
     ************************************************************************/

    /** CRC校验失败, 需要重发 */
    //public static final int EXTEND_COMMAND_CRC_ERROR = 201;
    /************************************************************************
     *                             客户端命令                                *
     *                     除特殊命令外, 客户端命令从100 起                    *
     ************************************************************************/
    /** 客户端自定SQL 语句,
     * 使用哩个命令要提供超级管理员帐号 */
    public static final int EXTEND_COMMAND_SQL = 0xFF;

    /** 取得企业列表命令*/
    public static final int EXTEND_COMMAND_GET_COMPANY_LIST = 111;
    /************************************************************************
     *                             服务端命令                                *
     ************************************************************************/
    /** 新增企业命令*/
    public static final int EXTEND_COMMAND_NEW_COMPANY = 011;

    /************************************************************************
     *                              数据库字段                                *
     ************************************************************************/

    public static final String DATABASE_COMPANY_ID       = "ID";
    public static final String DATABASE_COMPANY_NAME     = "Name";
    public static final String DATABASE_COMPANY_SUBNAME  = "SubName";
    public static final String DATABASE_COMPANY_INPARENT = "InParent";
    public static final String DATABASE_COMPANY_OWNER    = "DatabaseOwner";
    public static final String DATABASE_COMPANY_PASSWORD = "PasswordMD5";
    public static final String DATABASE_COMPANY_DELETED  = "Deleted";
}
