package com.abilix.walktunner.udp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * Created by hel on 2017/8/2.
 */

public class FileUtils {
    public final static String TAG = FileUtils.class.getSimpleName();
    public final static String DATA_PATH = Environment.getExternalStorageDirectory() + "/Download/";// 下载的文件存储路径
    public final static String BinFile_PATH = "/mnt/sdcard/Download/";
    public static final String UPGRADE_FILE = DATA_PATH + "UpdateFile";

    public static void saveFile(String data, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(data.getBytes());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveFile(byte[] data, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                // LogMgr.d("file not exist ,create new file");
                // File dir = new File(file.getParent());
                // dir.mkdirs();
                file.createNewFile();
            } else {
                //   LogMgr.d("file exist ,delete file");
                file.delete();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            // OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            // BufferedWriter bufferedWriter = new BufferedWriter(osw);
            // LogMgr.d("写文件");
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void writeData(String data, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                // LogMgr.e("file not exist ,create new file");
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            // LogMgr.e("写文件");
            bufferedWriter.write(data);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String path) {
        String content = ""; // 文件内容字符串
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d(TAG, "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "The File doesn't not exist.");
            } catch (IOException e) {
                Log.e(TAG, "IOException::" + e.getMessage());
            }
        }
        return content;
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

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        // 如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        // 遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                // 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        // 删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        // LogMgr.d("deleteFile01 filePath = " + filePath);
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
            file.renameTo(to);
            return to.delete();
            // return file.delete();
        }
        return false;
    }

    public static boolean deleteFile(File file) {
        // LogMgr.d("deleteFile02 file = " + file.getPath());
        if (file.isFile() && file.exists()) {
            final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
            file.renameTo(to);
            return to.delete();
            // return file.delete();
        }
        return false;
    }

    /**
     * install slient
     *
     * @param context
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception
     * error
     */
    public static int installSlient(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d("installSlient", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }

//	public static void saveBinFileToSdCard(Context context, String pathName) {
//		try {
//			File dir = new File(UPGRADE_FILE);
//			if (!dir.exists()) {
//				dir.mkdirs();
//				// file.createNewFile();
//			}
//			File file = new File(dir, pathName);
//			if (!file.exists()) {
//				// file.mkdirs();
//				file.createNewFile();
//			}
//			InputStream inputStream = context.getAssets().open(pathName);
//			FileOutputStream fos = new FileOutputStream(file);
//			byte[] buffer = new byte[1024];
//			int byteCount = 0;
//			while ((byteCount = inputStream.read(buffer)) != -1) {
//				fos.write(buffer, 0, byteCount);
//			}
//			fos.flush();
//			inputStream.close();
//			fos.close();
//		} catch (Exception e) {
//
//		}
//
//	}

    public static Long getCRC32(String filePath) {// 获取文件的CRC32校验值
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = null;
        CheckedInputStream checkedinputstream = null;
        Long crc = null;
        try {
            fileinputstream = new FileInputStream(new File(filePath));
            checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
            while (checkedinputstream.read() != -1) {
            }
            // crc = Long.toHexString(crc32.getValue()).toUpperCase();
            crc = crc32.getValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileinputstream != null) {
                try {
                    fileinputstream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crc;
    }

}
