package softwareInstaller;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import softwareInstaller.File;

/**
 * @describe 主控类，用于调用加密函数以及与数据类通信
 * @author Zhao Zhichen
 * @time 2017.08.07 下午2:26:52
 * @version softwareInstaller.17.08.07
 * @see
 */
public final class Main {

	// 软件版本默认值
	private static String softwareVersion = "1.2.364";

	// 功能开关默认值
	private static String funcationSwitch = "false";

	/*
	 * 文件读写相关
	 * 
	 */

	/**
	 * @Title: createKey
	 * @Description: 调用File类的方法创建密钥文件
	 * @param path
	 *            密钥文件
	 * @throws IOException
	 *             写入异常
	 * @throws NoSuchAlgorithmException
	 *             未找到加密字典异常
	 * @return: void
	 */
	public static void createKey(String path) throws IOException, NoSuchAlgorithmException {
		String[] own_info = new String[5];
		own_info = ownKey();
		byte[] machineKey = hashDataEncode();
		File.createMachineKeyFile(path, machineKey);
		File.createLogKeyFile(path, own_info);
	};

	// 检查注册文件，返回值boolean，参数String为文件路径
	/**
	 * @Title: checkKey
	 * @Description: 核对密钥是否一致
	 * @param path
	 *            密钥文件路径
	 * @throws IOException
	 *             读取异常
	 * @throws NoSuchAlgorithmException
	 *             未找到加密字典异常
	 * @return: boolean
	 * @throws InvalidKeySpecException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public static boolean checkKey(String path) throws IOException, NoSuchAlgorithmException {
		byte[] ownKey = {};
		byte[] correctKey = {};
		KeyCipher loadKey = File.loadKeyFile(path);
		correctKey = rsaDataDecode(loadKey);/* 解码 */
		String correctString=new String(correctKey);
		Scanner sc=new Scanner(correctString);
		String mKey=sc.next();
		String version=sc.next();
		String function=sc.next();
		sc.close();
		ownKey = hashDataEncode();
		if (mKey.equals(File.byteArrayToHexString(ownKey))&&version.equals(Main.softwareVersion)&&function.equals(Main.funcationSwitch)) {
			return true;
		} else {
			return false;
		}
	};

	/*
	 * 文件读写相关 end
	 * 
	 */

	/*
	 * 信息获取相关
	 * 
	 */

	/**
	 * @Title: ownKey
	 * @Description: 获取系统信息并调用encode()方法获取注册码
	 * @throws IOException
	 *             读取异常
	 * @throws NoSuchAlgorithmException
	 *             未找到加密字典异常
	 * @return: String[] 系统信息+注册码数组
	 */
	public static String[] ownKey() throws IOException, NoSuchAlgorithmException {
		String[] info = { "", "", "", "", "" };/* 用于存储系统信息 */
		String cpuId = getCpuId();
		String diskId = getDiskId();
		info[0] = cpuId;
		info[1] = diskId;
		info[2] = getSoftwareVersion();
		info[3] = getFuncationSwitch();
		return info;
	}

	/**
	 * @Title: getDiskId
	 * @Description: 获取硬盘序列号
	 * @throws IOException
	 *             读取异常
	 * @return: String
	 */
	private static String getDiskId() throws IOException {
		String result = "";/* 用于保存结果 */
		String appent = "";/* 用于阅读每一行 */
		Process rt = Runtime.getRuntime().exec("wmic diskdrive get SerialNumber");
		InputStreamReader rd = new InputStreamReader(rt.getInputStream());
		BufferedReader br = new BufferedReader(rd);
		br.readLine();
		while ((appent = br.readLine()) != null) {
			result = result + appent;
		}
		return result;
	};

	/**
	 * @Title: getCpuId
	 * @Description:获取CPUID
	 * @return
	 * @throws IOException
	 *             读取异常
	 * @return: String
	 */
	private static String getCpuId() throws IOException {
		String cpuId;
		Process rt = Runtime.getRuntime().exec("wmic cpu get processorId");
		InputStreamReader rd = new InputStreamReader(rt.getInputStream());
		BufferedReader br = new BufferedReader(rd);
		br.readLine();
		br.readLine();
		cpuId = br.readLine();
		return cpuId;
	}

	/**
	 * @Title: getSoftwareVersion
	 * @Description:查看版本号
	 * @return: String
	 */
	public static String getSoftwareVersion() {
		return softwareVersion;
	}

	/**
	 * @Title: getFuncationSwitch
	 * @Description: 查看功能开关状态
	 * @return: String
	 */
	public static String getFuncationSwitch() {
		return funcationSwitch;
	}

	/**
	 * @Title: setSoftwareVersion
	 * @Description: 设置版本号
	 * @param softwareVersion
	 * @return: void
	 */
	public static void setSoftwareVersion(String softwareVersion) {
		Main.softwareVersion = softwareVersion;
	}

	/**
	 * @Title: setFuncationSwitch
	 * @Description:切换功能开关状态
	 * @param funcationSwitch
	 *            参数为字符串“true”或是字符串“false”，分别代表功能开和功能关
	 * @return: void
	 */
	public static void setFuncationSwitch(String funcationSwitch) {
		Main.funcationSwitch = funcationSwitch;
	}

	/*
	 * 信息获取相关end
	 * 
	 */

	/*
	 * 哈希加密和RSA解密
	 * 
	 */

	/**
	 * @Title: dataEncode
	 * @Description: 一次加密，使用哈希加密获得机器码用于服务器的二次加密，这个加密过程是不可逆的
	 * @param cpuId
	 * @param diskId
	 * @param softwareVersion
	 * @param functionSwitch
	 * @return: byte[] 返回值为byte数组
	 * @throws IOException
	 *             读写异常
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] hashDataEncode() throws IOException, NoSuchAlgorithmException {
		String cpuId = getCpuId();
		String diskId = getDiskId();
		String oriStr = cpuId + diskId;
		MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] bkey = md.digest(oriStr.getBytes());
		return bkey;
	}

	private static byte[] rsaDataDecode(KeyCipher key) {
		try {
			byte[] bkey = {};
			// 根据n和d获取私钥
			BigInteger modulus = new BigInteger(key.modulus);
			BigInteger privateExponent = new BigInteger(key.prikey);
			RSAPrivateKeySpec pks = new RSAPrivateKeySpec(modulus, privateExponent);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PrivateKey priKey = factory.generatePrivate(pks);			
			Cipher cipher = Cipher.getInstance("RSA");/*实例化解密器*/
			cipher.init(Cipher.DECRYPT_MODE, priKey);/*配置解密器*/
			bkey = cipher.doFinal(key.result);/*解密*/
			return bkey;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String hashStringShow() throws NoSuchAlgorithmException, IOException {
		byte[] hbyte = hashDataEncode();
		String hashString = File.byteArrayToHexString(hbyte);
		return hashString;
	}
	/*
	 * 哈希加密和RSA解密
	 * 
	 */
	 
}
class KeyCipher{
	public String prikey;
	public String modulus;
	public byte[] result;
}
