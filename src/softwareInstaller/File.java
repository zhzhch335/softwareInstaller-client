package softwareInstaller;

import java.io.*;
import java.util.*;

/**
 * @describe 数据类，用于序列号的读写操作
 * @author Zhao Zhichen
 * @time 2017.08.07 下午2:26:25
 * @version softwareInstaller.17.08.07
 * @see
 */
public class File {

	/**
	 * @Title: createKeyFile
	 * @Description: 创建机器码文件
	 * @param url
	 *            密钥文件地址
	 * @param key
	 *            密钥
	 * @throws IOException
	 *             写入异常
	 * @return: void
	 */
	public static void createMachineKeyFile(String url, byte[] key) throws IOException {
		FileWriter fw=new FileWriter(url);
		fw.write(byteArrayToHexString(key)+"\n");
		fw.write(Main.getSoftwareVersion()+"\n");
		fw.write(Main.getFuncationSwitch());
		fw.close();
	}

	/**
	 * @Title: createKeyFile
	 * @Description: 创建系统信息文件
	 * @param url
	 *            系统信息文件地址
	 * @param info
	 *            系统信息字符串数组
	 * @throws IOException
	 *             写入异常
	 * @return: void
	 */
	public static void createLogKeyFile(String url, String[] info) throws IOException {
		url = url.substring(0, url.length() - 4) + "系统信息.txt";
		Writer wr = new FileWriter(url);
		wr.append("CPUID:" + info[0] + "\n");
		wr.append("DiskID:" + info[1].replaceAll(" +", ",") + "\n");
		wr.append("软件版本:" + info[2] + "\n");
		wr.append("功能开关:" + info[3] + "\n");
		wr.append("文件生成日期:" + new Date() + "\n");
		wr.close();
	}

	// 加载注册码文件，返回值为字符串序列号，参数为字符串路径
	/**
	 * @Title: loadKeyFile
	 * @Description: 加载注册码文件
	 * @param url
	 *            密钥文件地址
	 * @throws IOException
	 *             读取异常
	 * @return: String
	 */
	public static KeyCipher loadKeyFile(String url) throws IOException {
		KeyCipher key = new KeyCipher();
		Reader isr = new FileReader(url);
		BufferedReader br = new BufferedReader(isr);
		String result=br.readLine();
		System.out.println(result.split("")[0]);
		key.result = hexStringToByteArray(result);/*读取加密后的字符*/
		key.modulus = br.readLine();/*读取模*/
		key.prikey = br.readLine();/*读取d*/
		br.close();
		return key;
	}
	
	public static String byteArrayToHexString(byte[] b) {
		String result="";
		String tempresult="";
		for(Byte i : b) {
			tempresult=Integer.toHexString(i.intValue()+128);
			if(tempresult.length()==1) {
				tempresult="0"+tempresult;
			}
			result=result+tempresult;
		}
		return result;
		
	}
	
	static byte[] hexStringToByteArray(String s) {
		String[] arr=s.split("");
		byte[] result=new byte[arr.length/2];
		int k=0;
		for(int i=1;i<=arr.length-2;i=i+2) {
			int tempint=Integer.parseInt(arr[i]+arr[i+1], 16)-128;
			result[k]=(byte)tempint;
			k++;
		}
		return result;
	}
}