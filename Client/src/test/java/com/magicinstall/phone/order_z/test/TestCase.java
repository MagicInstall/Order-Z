package com.magicinstall.phone.order_z.test;

import com.magicinstall.library.CRC32;
import com.magicinstall.library.Hash;

import org.junit.Test;

/**
 * Created by wing on 16/5/4.
 */
//@RunWith(RobolectricTestRunner.class)
//@Config(constants = BuildConfig.class)
public class TestCase {

    @Test
    public void testClassCalculateCRC32With8Bit() throws Exception {
        String text =
                "~!@#$%^&*()_+`1234567890-=" +
                "QWERTYUIOP{}|qwertyuiop[]\\" +
                "ASDFGHJKL:\"asdfghjkl;'" +
                "ZXCVBNM<>?zxcvbnm,./'";
        byte[] data = text.getBytes();

        CRC32 mCRC32 = new CRC32();
        mCRC32.reset32();
        mCRC32.calculateCRC32With8Bit(data, data.length);

        int result =
//                CRC32.classCalculateCRC32With8Bit(data, data.length);
                mCRC32.getResult32();

        String crc32 = Integer.toHexString(result);

        String sha256 = Hash.bytes2Hex(Hash.getSHA256(data));


    }


}