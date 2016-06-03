package com.magicinstall.phone.order_z.test;

import com.magicinstall.library.DataPacket;
import com.magicinstall.library.Hash;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * Created by wing on 16/5/14.
 */
public class DataPacketTest {

    @Test
    public void testMake() throws Exception {
        System.out.print("WTF\n");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
        DataPacket sendPacket = new DataPacket();
        sendPacket.setLimit(50);
        for (int i = 0; i < 10; i++) {
            outputStream.write(new byte[]{0,1,2,3,4,5,6,7,8,9});
            sendPacket.pushItem(outputStream.toByteArray());
        }
        sendPacket.make(new byte[]{0,1,2,3,4,5,6,7,8,9});

        // 打印
        sendPacket.moveToFirstPacket();
        do {
            System.out.print("VerifyCRC " + (sendPacket.getCurrentPacket().verifyCRC() ? "OK  " : "fail"));
            System.out.print(" P:" + Hash.bytes2Hex(sendPacket.getCurrentPacket().data) + "\n");
        }while (sendPacket.moveNextPacket() != null);

        DataPacket getPacket = new DataPacket();
        sendPacket.moveToFirstPacket();
        do {
            getPacket.pushPacket(sendPacket.getCurrentPacket().data);
        } while (sendPacket.moveNextPacket() != null);

        byte ex[] = new byte[10];
        int id = getPacket.resolve(ex);
        System.out.print("ID:" + id +" Ex:" + Hash.bytes2Hex(ex) + "\n");
        do {
            System.out.print("I:" + Hash.bytes2Hex(getPacket.getCurrentItem().data) + "\n");
        } while (getPacket.moveNextItem() != null);
    }
}