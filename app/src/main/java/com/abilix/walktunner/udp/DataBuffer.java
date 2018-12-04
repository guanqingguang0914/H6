package com.abilix.walktunner.udp;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hel on 2017/8/2.
 */

public class DataBuffer {

    public static String filePath = "";
    public static String binName = null;
    public static long binFileLen = 0;
    public static String serverIP = null;
    public static boolean hasclient = false;
    public static boolean StopMusic = true;
    public static boolean stopSction = true;
    public final static String DATA_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";
    public static List<String> fileList = new ArrayList<String>();

    public static void getFileList(File path, List<String> fileList) {// 获取指定路径下的文件信息
        if (!path.exists()) {
            try {
                path.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (path.isDirectory()) {// 如果是文件夹的话
            // 返回文件夹中有的数据
            File[] files = path.listFiles();
            // 先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files)
                return;
            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList);
            }
        } else {// 如果是文件的话直接加入
            // 进行文件的处理
            String filePath = path.getAbsolutePath();
            // 文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            // fileName=getFileNameNoEx(fileName);
            if (!fileList.contains(fileName))
                fileList.add(fileName);// list
        }
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

}
