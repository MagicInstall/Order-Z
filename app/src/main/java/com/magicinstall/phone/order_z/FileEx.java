package com.magicinstall.phone.order_z;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 文件/文件夹操作库
 *
 * Created by wing on 16/4/27.
 */
public class FileEx {
    private static final String TAG = FileEx.class.toString();

    /**
     * 快速复制文件
     * <p>
     * 哩个方法直接覆盖已存在嘅文件!
     * @param srcFile 源文件, 必须传入绝对路径!
     * @param destFile 目标文件, 必须传入绝对路径!
     * @return 实际复制的字节数，如果文件、目录不存在、文件为null或者发生IO异常，返回-1
     */
    public static long copyFile(File srcFile, File destFile) throws IOException {
        if (!srcFile.exists()) {
            Log.e(TAG, srcFile + " (Source) does not exist!");
            return -1;
        }

        if(!srcFile.renameTo(srcFile)){
            Log.e(TAG, srcFile + " (Source) is being used by another process!");
            return -1;
        }

        if (!srcFile.isFile()) {
            Log.e(TAG, srcFile + " (Source) not a file!");
            return -1;
        }

        if (!destFile.getParentFile().exists()) {
            Log.e(TAG, "Destination directory does not exist");
            return -1;
        }

        // 开始复制
        FileChannel fcin = new FileInputStream(srcFile).getChannel();
//        FileChannel fcout = new FileOutputStream(new File(destFile, newFileName)).getChannel();
        FileChannel fcout = new FileOutputStream(destFile).getChannel(); // 直接覆盖
        long size = fcin.size();
        fcin.transferTo(0, fcin.size(), fcout);
        fcin.close();
        fcout.close();

        return size;
    }

    /**
     * 文件夹复制
     * @param srcDir 必须传入绝对路径!
     * @param destDir 必须传入绝对路径!
     * @param recursive true:递归子目录
     * @return 返回已复制的文件数, 不包括文件夹数
     */
    public static long copyDirectory(File srcDir, File destDir, boolean recursive) throws IOException{
        /* 交由#copyFile()检查
        // 源对象不存在就直接退出
        if (!srcDir.exists()) return 0;
        if (!srcDir.isAbsolute() || !destDir.isAbsolute()) return 0;
        */

        // 如果源对象系文件就复制, 退出
        if (srcDir.isFile()) {
            String dest_path = destDir + "/" + srcDir.getName();
            if (copyFile(srcDir, new File(dest_path)) > -1) {
                Log.v(TAG, srcDir + " copy to " + dest_path);
            }
            return 1;
        }

        long copy_count = 0;
        if (srcDir.isDirectory()) {
            // 检查目标文件夹是否存在
            if (!destDir.exists()) {
                // 新建并检查
                if(!destDir.mkdir()){
                    // 去到哩度就系创建失败!
                    Log.e(TAG, destDir + " mkdir failed!");
                    return 0;
                }
                Log.v(TAG, "mkdir:" + destDir);
            }

            // 开始历遍
            File[] childFile = srcDir.listFiles(); // 当前文件夹下所有对象
            if (childFile == null || childFile.length < 1) return 0;

            File dest_child = destDir;
            for (File src_child : childFile) {

                // 如果子对象是文件夹的处理
                if (src_child.isDirectory()) {
                    if (recursive){
                        // 目标文件夹前进一级
                        dest_child = new File(destDir + "/" + src_child.getName());
                        // 然后后面进入递归
                    }
                    else continue; // 如果唔需要递归文件夹, 就跳过
                }

                // 进入递归
                copy_count += copyDirectory(src_child, dest_child, recursive);
            }
        }

        return copy_count;
    }

    /**
     * 递归删除文件夹
     * @param directory 亦可以传入单一个文件, 咁就只删除一个文件
     */
    public static void deleteDirectory(File directory) {
        if (directory.exists() == false) {
            return;
        }

        // 如果唔系文件夹就直接删除, 退出
        if (directory.isFile()) {
            directory.delete();
            return;
        }

        if (directory.isDirectory()) {
            File[] childFile = directory.listFiles();
            // 如果无子目录, 就删咗当前文件夹, 退出
            if (childFile == null || childFile.length == 0) {
                directory.delete();
                return;
            }

            // 开始进入递归
            for (File f : childFile) {
                deleteDirectory(f);
            }
            directory.delete();
        }


    }

}

