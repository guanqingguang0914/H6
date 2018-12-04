package com.abilix.walktunner.udp;

import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

public class MyConstant {
	public static final int UDPPORT = 2827;             //发布会测试用，避免与其它端口冲突
	public static final int TCPPORT = 6495;	
	public static final int byteSize = 1024*10;
	
	public static byte binIndex=1;
	
	public static final byte TYPE0 = (byte)0x00;  //无效类型 
	public static final byte TYPE1 = (byte)0x01;  //C系列 (VJC)   
	public static final byte TYPE2 = (byte)0x02;  //M系列
	public static final byte TYPE3 = (byte)0x03;  //H系列
	public static final byte TYPE4 = (byte)0x04;  //F系列

	public static final byte CMD1_F1 = (byte)0xF1; //主命令字1 -- 广播 (时钟同步广播协议, 客户端发送F1 00; 服务器回馈 E0 00) 
	public static final byte CMD1_F2 = (byte)0xF2; //主命令字1 -- 广播 (时钟同步广播协议, 客户端发送F1 00; 服务器回馈 E0 00) 
	public static final byte CMD1_0 = (byte)0x00; //主命令字1 -- 广播 (客户端发送 00 00; 服务器回馈 A0 00)
	public static final byte CMD1_1 = (byte)0x01; //主命令字1 
	public static final byte CMD1_2 = (byte)0x02; //主命令字1 
	public static final byte CMD1_3 = (byte)0x03; //主命令字1 
	public static final byte CMD1_4 = (byte)0x04; //主命令字1 
	public static final byte CMD1_A0 = (byte)0xA0; //主命令字1 -- 底层向上层回馈
	public static final byte CMD1_A1 = (byte)0xA1; //主命令字1 
	public static final byte CMD1_E0 = (byte)0xF1; //主命令字1

	public static final byte CMD2_0 = (byte)0x00; //主命令字2
	public static final byte CMD2_1 = (byte)0x01; //主命令字2 
	public static final byte CMD2_2 = (byte)0x02; //主命令字2 
	public static final byte CMD2_3 = (byte)0x03; //主命令字2 
	public static final byte CMD2_4 = (byte)0x04; //主命令字2 
			
	
	//协议封装： AA 55 len1 len2 type cmd1 cmd2 00 00 00 00 (data) check
	public static byte[] addProtocol(byte[] buff)
	{
		short len = (short)(buff.length + 5); 
		byte[] sendbuff = new byte[len];
		sendbuff[0] = (byte)0xAA;             //头
		sendbuff[1] = (byte)0x55;
		sendbuff[2] = (byte)(len & 0x00FF);   //长度: 从type到check
		sendbuff[3] = (byte)((len>>8) & 0x00FF);		
		System.arraycopy(buff, 0, sendbuff, 4, buff.length);  //type - data

		byte check = 0x00;		              //校验位
		for(int n=0; n<len-2; n++) {
			check += sendbuff[n];
		}
		sendbuff[len-1] = (byte)(check & 0x00FF);
		return sendbuff;
	}
	
	//设置与服务器的偏差值
	private static long offsetT = 0;
	public static void setOffsetT(long t)
	{
		offsetT = t;
	}
	
	public static long getOffsetT()
	{
		return offsetT;
	}
		
	//获取本机时间
	public static long getLocalTime()
	{
		Calendar calendar = Calendar.getInstance();
		long calTime = calendar.getTimeInMillis();
		return calTime;
	}
	
	//获取服务器偏移时间
	public static long getServerTime()
	{
		return getLocalTime() + offsetT;
	}
	
	//设置服务器同步时间
	private static long synctime = 0;
	public static void setSyncTime(long time)
	{
		synctime = time;
	}
	
	public static long getSyncTime()
	{
		return synctime;
	}
		
	//获取本地ip地址
	public static String getLocalHostIp()
	{
		String ipaddress = "";
		try
		{
			for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface.getNetworkInterfaces(); mEnumeration.hasMoreElements();)
			{
				NetworkInterface intf = mEnumeration.nextElement();
				for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIPAddr.nextElement();
					//如果不是回环地址
					if (!inetAddress.isLoopbackAddress())
					{
						if(inetAddress.getHostAddress().contains("192.") || inetAddress.getHostAddress().contains("10.") || inetAddress.getHostAddress().contains("176."))
						//直接返回本地IP地址
							return ipaddress=inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		catch (SocketException ex)
		{
			Log.e("Error", ex.toString());
		}
		return ipaddress;
	}
	
	//获取广播ip
	public static String getBroadCastIP()
	{
		String ip = getLocalHostIp().substring(0, getLocalHostIp().lastIndexOf(".") + 1) + "255";
		return ip;
	}
	
	//获取机器名
	public static String getName()
	{
		return Build.MODEL;
	}
	
	//bytes转int
	public static int bytesToInt(byte[] ary, int offset) {
		int value;	
		value = (int) ((ary[offset] & 0xFF) 
				| ((ary[offset+1]<<8) & 0xFF00)
				| ((ary[offset+2]<<16)& 0xFF0000) 
				| ((ary[offset+3]<<24) & 0xFF000000));
		return value;
	}
	
	//int转bytes
	public static void intToBytes(int n, byte buf[], int offset) {  
		buf[offset]   =  (byte) (n & 0xFF);	
		buf[offset+1] =  (byte) ((n>>8) & 0xFF);  
		buf[offset+2] =  (byte) ((n>>16) & 0xFF);
		buf[offset+3] =  (byte) ((n>>24) & 0xFF);
    }  
	
	//bytes格式化为string，用于打印log
	public static String bytesToString(byte[] buf, int len)
	{
		String str=null, str1;
		for(int n=0; n<len; n++)
		{
			str1 = String.format("%02x ", buf[n]);
			if(n == 0)
				str = str1;
			else
				str += str1;
		}
		return str;
	}
		
	//byte 数组与 long 的相互转换  
	/*	private static ByteBuffer buffer = ByteBuffer.allocate(8); 
	public static byte[] longToBytes(long x) {  
	    buffer.putLong(0, x);  
	    return buffer.array();  
	}  
	
	public static long bytesToLong(byte[] bytes) {  
	    buffer.put(bytes, 0, bytes.length);  
	    buffer.flip();  //need flip   
	    return buffer.getLong();  
	}  */
	
	//long类型转成byte数组 
	public static byte[] longToBytes(long number)
	{ 
	  long temp = number; 
	  byte[] b = new byte[8]; 
	  for (int i = 0; i < b.length; i++) { 
	      b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位 
	      temp = temp >> 8; // 向右移8位 
	  } 
	  return b; 
	}
	
	public static long bytesToLong(byte[] bytes,int offset)  
	{  
	    long l64;  
	    l64=0l;  
	    l64 =  (long)bytes[offset]&0xff;  
	    l64 |= (long)(bytes[offset + 1]&0xff)<<8;  
	    l64 |= (long)(bytes[offset + 2]&0xff)<<16;  
	    l64 |= (long)(bytes[offset + 3]&0xff)<<24;  
	    l64 |= (long)(bytes[offset + 4]&0xff)<<32;  
	    l64 |= (long)(bytes[offset + 5]&0xff)<<40;  
	    l64 |= (long)(bytes[offset + 6]&0xff)<<48;  
	    l64 |= (long)(bytes[offset + 7]&0xff)<<56;  
	    return l64;  
	}      

	
}
