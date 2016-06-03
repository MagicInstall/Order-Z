package com.magicinstall.library;

/**
 * CRC32 算法移植
 * <p>参见Objective C 源码, 原源码已同STM32 调通
 *
 * <p>实例方法系为大型数据(流)而设的,
 * 可以按顺序分开多次调用{@link .calculateCRCxx}方法,
 * 最后从{@link .calculateCRCxx}方法取得计算结果.
 *
 * <p>在计算另一个数据(流)之前,
 * 必须用{@link .resetxx}方法重新初始化.
 *
 * <p>死人JAVA 真系无力吐嘈... 右移居然会保留符号位!
 *
 * <p>Created by wing on 16/5/4.
 */
public class CRC32 {
    // CRC32 的权值
    private static final int POLYNOMIAL_CRC_32 = 0x04C11DB7;

    /**
     * 翻转8 位位序
     * @param value
     * @return
     */
    public static byte reversionBit8(byte value){
        // 死人JAVA右移居然会保留符号位!...哩度手动清除
        value = (byte) ((value & 0x55) << 1 | ((value & 0xAA) >> 1 & 0b01111111));
        value = (byte) ((value & 0x33) << 2 | ((value & 0xCC) >> 2 & 0b00111111));
        value = (byte) ((value & 0x0F) << 4 | ((value & 0xF0) >> 4 & 0b00001111));
        return value;
    }

    /**
     * 翻转32 位位序
     * @param value
     * @return
     */
    public static int reversionBit32(int value){
        // 死人JAVA右移居然会保留符号位!...哩度手动清除
        value = (value & 0x55555555) <<  1 | ((value & 0xAAAAAAAA) >>  1 & 0x7FFFFFFF);
        value = (value & 0x33333333) <<  2 | ((value & 0xCCCCCCCC) >>  2 & 0x3FFFFFFF);
        value = (value & 0x0F0F0F0F) <<  4 | ((value & 0xF0F0F0F0) >>  4 & 0x0FFFFFFF);
        value = (value & 0x00FF00FF) <<  8 | ((value & 0xFF00FF00) >>  8 & 0x00FFFFFF);
        value = (value & 0x0000FFFF) << 16 | ((value & 0xFFFF0000) >> 16 & 0x0000FFFF);
        return value;
    }

    /**
     * 以8位数据为单位计算CRC32 码
     * <p>类方法同实例方法共用的算法过程
     * @param result32 相当于实例方法的{@Link _crcResult32}变量.
     * @param data
     * @param length
     * @return
     */
    private static int _CRC32With8Bit(int result32, byte[] data, int offset, int length) {
        byte xbit, bits, data_tmp;

        for (int l = offset; l < length; l++) {
            data_tmp = reversionBit8( data[l]);

            xbit = (byte) (1 << 7);
            for (bits = 0; bits < 8; bits++)
            {
                if ((result32 & 0x80000000) != 0)
                {
                    result32 <<= 1;
                    result32 ^= POLYNOMIAL_CRC_32;
                }
                else result32 <<= 1;

                if ((data_tmp & xbit) != 0)
                {
                    result32 ^= POLYNOMIAL_CRC_32;
                }

                xbit >>= 1;
                xbit &= 0b01111111; // 死人JAVA右移居然会保留符号位!...哩度手动清除
            }

        }

        return result32;
    }

    /**
     * 以8位数据为单位计算CRC32 码
     * <p>类方法, 直接一次取得CRC32 码,
     * 小型数据可以直接用类方法.
     * @param data
     * @param offset
     * @param length
     * @return
     * @see #calculateCRC32With8Bit(byte[], int)
     */
    public static int classCalculateCRC32With8Bit(byte[] data, int offset, int length) {
        int result32 = 0xFFFFFFFF;

        result32 = _CRC32With8Bit(result32, data, offset, length);

        return reversionBit32(result32) ^ 0xffffffff;
    }
    public static int classCalculateCRC32With8Bit(byte[] data, int length) {
        return classCalculateCRC32With8Bit(data, 0, length);
    }

    /************************************************************************
     *                               实例方法                                *
     ************************************************************************/

    /**
     * 构造, 初始化
     */
    void CRC32(){
        reset32();
    }

    /**
     * 重置初值
     */
    public void reset32(){
        _crcResult32 = 0xFFFFFFFF;
    }

    // 计算结果
    private int _crcResult32;
    /**
     * 取得最后计算的结果(32位)
     * @return
     */
    public int getResult32() {
        return reversionBit32(_crcResult32) ^ 0xffffffff;
    }

    /**
     * 以32位数据为单位计算CRC32 码
     * <p>由于STM32 的CRC模块只能以32 位为单位对数据进行计算,
     * 所以传入的数据必须满足32 位长度,
     * 如果用8位(或其它长度)嘅缓冲区, 唔够长嘅部分要补足, 具体补咩要自行协定;
     * @param data
     * @param length
     * @return 返回当次的计算结果, 可以直接使用这个返回值,
     *         或者多次执行以后, 调用{@Link getResult32}取得结果.
     */
    public int calculateCRC32(int[] data, int length){
        int  xbit, data_tmp;
        byte bits;
        for (int l = 0; l < length; l++) {
            data_tmp = reversionBit32(data[l]);

            xbit = (int) (1 << 31);
            for (bits = 0; bits < 32; bits++)
            {
                if ((_crcResult32 & 0x80000000) != 0)
                {
                    _crcResult32 <<= 1;
                    _crcResult32 ^= POLYNOMIAL_CRC_32;
                }
                else _crcResult32 <<= 1;

                if ((data_tmp & xbit) != 0)
                {
                    _crcResult32 ^= POLYNOMIAL_CRC_32;
                }

                xbit >>= 1;
                xbit &= 0b01111111111111111111111111111111; // 死人JAVA右移居然会保留符号位!...哩度手动清除
            }

        }

        return reversionBit32(_crcResult32) ^ 0xffffffff;
    }

    /**
     * 以8位数据为单位计算CRC32 码
     * <p>有别于{@Link calculateCRC32WithData}方法,
     * 哩个方法系对任意长度(不满足32 位宽度)嘅数据进行计算,
     * 用于对文件等长度唔可以事先限定喺32 位嘅情况.
     * @param data
     * @param length
     * @return 返回当次的计算结果, 可以直接使用这个返回值,
     *         或者多次执行以后, 调用{@Link getResult32}取得结果.
     */
    public int calculateCRC32With8Bit(byte[] data, int length){
        _crcResult32 = _CRC32With8Bit(_crcResult32, data, 0, length);

        return reversionBit32(_crcResult32) ^ 0xffffffff;
    }
}
