package softwareInstaller;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.jvnet.substance.*;
import org.jvnet.substance.skin.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Toolkit;

/**
 * @describe 用户UI类 用于用户生成机器码向服务器获取注册文件以及使用这个注册文件激活软件
 * @author Zhao Zhichen
 * @time 2017.08.07 下午2:24:57
 * @version softwareInstaller.17.08.07
 * @see
 */
public class Client {

	
	public static void main(String[] args) {

		/*
		 * 使用substance美化界面
		 */
		JFrame.setDefaultLookAndFeelDecorated(true);

		JDialog.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
			SubstanceLookAndFeel.setSkin(new BusinessBlueSteelSkin());
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		/*
		 * 使用substance美化界面end
		 */

		/*
		 * 将UI绘制操作加入EDT中，加载窗体和组件 （线程安全）
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		/*
		 * 将UI绘制操作加入EDT中，加载窗体和组件 （线程安全）end
		 */
	}

	/**
	 * 窗体和组件初始化
	 * 
	 * @throws IOException
	 *             文件读写异常处理
	 * @throws NoSuchAlgorithmException
	 *             加密方法异常处理
	 */
	private static void initialize() throws IOException, NoSuchAlgorithmException {

		// 获取系统信息（在第一次加载时）
		final String[] info = Main.ownKey();

		// 窗体
		final JFrame frmHey = new JFrame();
		frmHey.setResizable(false);/* 禁止调整大小 */
		frmHey.setIconImage(
				Toolkit.getDefaultToolkit().getImage(Client.class.getResource("/resource/computer.png")));/* 设置左上角图标 */
		frmHey.getContentPane().setFont(new Font("方正兰亭超细黑简体", Font.PLAIN, 12));
		frmHey.setTitle("软件注册机-客户端");
		frmHey.setBounds(500, 200, 677, 427);
		frmHey.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);/* 设置窗体关闭时结束进程 */
		frmHey.getContentPane().setLayout(null);

		// 创建一个空对话框作为文件选择框和提示框以及模拟软件窗口的容器
		final JDialog dialog = new JDialog();
		dialog.setBounds(200, 200, 640, 480);

		/**
		 * 下方操作区，认证身份后根据不同身份显示（代碼要放在登録區代碼之前，否則無法在登陸代碼中調用其顯示隱藏函數）
		 */

		// 生成文件文本框
		final JTextField initText = new JTextField();
		initText.setVisible(false);
		initText.setBounds(209, 262, 211, 21);
		frmHey.getContentPane().add(initText);

		// 激活文件文本框
		final JTextField fetchText = new JTextField();
		fetchText.setVisible(false);
		fetchText.setBounds(209, 262, 211, 21);
		frmHey.getContentPane().add(fetchText);

		// 提示标签
		final JLabel initHint = new JLabel("请输入生成文件的路径：");
		initHint.setVisible(false);
		initHint.setFont(new Font("微软雅黑", Font.BOLD, 12));
		initHint.setBounds(209, 226, 179, 15);
		frmHey.getContentPane().add(initHint);
		final JLabel fetchHint = new JLabel("请输入要激活的文件路径：");
		fetchHint.setVisible(false);
		fetchHint.setFont(new Font("微软雅黑", Font.BOLD, 12));
		fetchHint.setBounds(209, 226, 161, 15);
		frmHey.getContentPane().add(fetchHint);

		// 获取桌面路径
		final File fsv = FileSystemView.getFileSystemView().getHomeDirectory();
		// final JFileChooser chooser = new JFileChooser(fsv);/* 将初始位置设定为桌面 */

		// 浏览按钮，用于唤起文件选择对话框
		final JButton initFile = new JButton("浏览...");
		initFile.setVisible(false);
		initFile.addMouseListener(new MouseAdapter() {
			/**
			 * Title: mouseClicked Description: 鼠标点击唤起文件选择对话框
			 * 
			 * @param e
			 *            鼠标响应事件
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser(fsv);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("机器码文件 .mkey", "mkey");
				chooser.setFileFilter(filter);/* 设置并加载文件过滤器 */
				int returnVal = chooser.showSaveDialog(dialog);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String url = chooser.getCurrentDirectory().getAbsolutePath() + "\\"
							+ chooser.getSelectedFile().getName();
					if (!url.endsWith(".mkey")) {
						url = url + ".mkey";
					}
					initText.setText(url);

				}
			}
		});
		initFile.setBounds(430, 261, 79, 23);
		frmHey.getContentPane().add(initFile);

		final JButton fetchFile = new JButton("浏览...");
		fetchFile.setVisible(false);

		// 浏览按钮，用于唤起文件保存对话框
		// 尝试使用通用方法写事件相应（也就是说所有动作都会触发事件）
		fetchFile.addActionListener(new ActionListener() {
			/**
			 * Title: actionPerformed Description: 鼠标通用事件用于唤起文件保存对话框
			 * 
			 * @param e
			 *            通用事件
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(fsv);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("注册文件 .key", "key");
				chooser.setFileFilter(filter);/* 设置并加载新的文件过滤器 */
				int returnVal = chooser.showOpenDialog(dialog);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fetchText.setText(chooser.getSelectedFile().getPath());
				}
			}
		});
		fetchFile.setBounds(430, 261, 79, 23);
		frmHey.getContentPane().add(fetchFile);

		// 生成按钮
		final JButton init = new JButton("生成");
		init.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		init.setVisible(false);
		init.addMouseListener(new MouseAdapter() {
			/**
			 * Title: mouseClicked Description: 鼠标点击调用生成密钥方法
			 * 
			 * @param e
			 *            鼠标响应事件
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				try {
					try {
						Main.createKey(initText.getText());
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					}
					String sn = Main.ownKey()[4];
					JOptionPane.showMessageDialog(dialog,
							"写入机器码文件成功，写入路径为" + initText.getText() + "\n" + Main.hashStringShow() + "\n"
									+ "请将此文件或机器码发送至357611628@qq.com或登陆网站http://jstorage.cn上传",
							"完成", JOptionPane.INFORMATION_MESSAGE);

				} catch (IOException | NoSuchAlgorithmException e1) {
					JOptionPane.showMessageDialog(dialog, "无权限写入文件，请稍后或更换路径再试", "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		init.setBounds(516, 261, 69, 23);
		frmHey.getContentPane().add(init);

		// 激活按钮
		final JButton fetch = new JButton("激活");
		fetch.setVisible(false);
		fetch.addMouseListener(new MouseAdapter() {
			/**
			 * Title: mouseClicked Description: 鼠标点击调用激活密钥方法
			 * 
			 * @param e
			 *            鼠标响应事件
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				try {
					if (Main.checkKey(fetchText.getText())) {
						String[] info = Main.ownKey();
						JOptionPane.showMessageDialog(dialog, "软件认证成功，请继续使用", "成功", JOptionPane.INFORMATION_MESSAGE);
						JOptionPane.showMessageDialog(dialog, "CPUID:" + info[0] + "\n" + "DiskID:" + info[1] + "\n"
								+ "软件版本：" + info[2], "系统信息", JOptionPane.INFORMATION_MESSAGE);
						frmHey.dispose();
						dialog.setVisible(true);
						dialog.setTitle("软件版本:" + Main.getSoftwareVersion() + " 认证通过！");
						JTextPane txt = new JTextPane();
						txt.setText("这里是软件本体，请继续体验本软件的神奇魅力！");
						txt.setBounds(200, 200, 300, 20);
						dialog.add(txt);
					} else {
						JOptionPane.showMessageDialog(dialog, "认证失败，请重试", "失败", JOptionPane.WARNING_MESSAGE);
					}
				} catch (IOException | HeadlessException | NoSuchAlgorithmException | NumberFormatException
						| NullPointerException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(dialog, "找不到文件或文件密钥失效，请重试", "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		fetch.setBounds(516, 261, 69, 23);
		frmHey.getContentPane().add(fetch);

		/**
		 * 下方操作区，认证身份后根据不同身份显示end
		 */

		/*
		 * 上方信息显示区（也要放在登陆代码前面，原因同上）
		 * 
		 */

		// 版本提示标签
		final JLabel vHint = new JLabel("当前版本号为：");
		vHint.setVisible(false);
		vHint.setBounds(274, 128, 96, 15);
		frmHey.getContentPane().add(vHint);

		// 版本文本框
		final JTextField vText = new JTextField(Main.getSoftwareVersion());
		vText.addFocusListener(new FocusAdapter() {
			/**
			 * Title: focusLost Description: 焦点消失后进行修改版本号操作
			 * 
			 * @param e
			 *            焦点消失响应事件
			 * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
			 */
			@Override
			public void focusLost(FocusEvent e) {
				if (!Main.getSoftwareVersion().contentEquals(vText.getText())) {/* 判断当前文本框中数据与原数据是否相同，相同则不必修改 */
					if (JOptionPane.showConfirmDialog(dialog, "【注意】请在确认之后再修改软件版本号，否则会导致注册文件不匹配", "提示",
							JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
						Main.setSoftwareVersion(vText.getText());
					} else {/* 点击取消则将版本号复原 */
						vText.setText(Main.getSoftwareVersion());
					}
				}
			}
		});
		vText.setVisible(false);
		vText.setBounds(361, 125, 148, 21);
		vText.setColumns(10);
		frmHey.getContentPane().add(vText);

		// 功能输入标签
		final JLabel fHint = new JLabel("功能开关：");
		fHint.setVisible(false);
		fHint.setBounds(274, 157, 79, 15);
		frmHey.getContentPane().add(fHint);

		// 功能切换按钮
		final JToggleButton fSwitch = new JToggleButton();
		fSwitch.setVisible(false);
		if (Main.getFuncationSwitch().contentEquals("true")) {/* 获取初始状态 */
			fSwitch.setText("开");
			fSwitch.setSelected(true);
		} else {
			fSwitch.setText("关");
			fSwitch.setSelected(false);
		}
		fSwitch.addActionListener(new ActionListener() {

			/**
			 * Title: actionPerformed Description: 通用事件唤起功能切换修改操作
			 * 
			 * @param e
			 *            通用响应事件
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(dialog, "【注意】请在确认之后再修改功能开关，否则会导致注册文件不匹配", "提示",
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					if (fSwitch.isSelected()) {
						fSwitch.setText("开");
						Main.setFuncationSwitch("true");
						SubstanceLookAndFeel.setSkin(new MangoSkin());
					} else {
						fSwitch.setText("关");
						Main.setFuncationSwitch("false");
						SubstanceLookAndFeel.setSkin(new BusinessBlueSteelSkin());
					}
				} else {
					fSwitch.setSelected(!fSwitch.isSelected());
				}
			}
		});
		fSwitch.setBounds(361, 153, 135, 23);
		frmHey.getContentPane().add(fSwitch);

		// 硬盘序列号提示标签
		final JLabel dHint = new JLabel("硬盘序列号：");
		dHint.setVisible(false);
		dHint.setBounds(274, 79, 79, 15);
		frmHey.getContentPane().add(dHint);

		// 硬盘序列号显示标签
		final JLabel dText = new JLabel("");
		dText.setVisible(false);
		dText.setBounds(361, 79, 269, 15);
		frmHey.getContentPane().add(dText);
		dText.setText(info[1] + "\n");

		// CPUID提示标签
		final JLabel cHint = new JLabel("CPUID：");
		cHint.setVisible(false);
		cHint.setBounds(275, 54, 54, 15);
		frmHey.getContentPane().add(cHint);

		// CPUID显示标签
		final JLabel cText = new JLabel("");
		cText.setVisible(false);
		cText.setBounds(361, 54, 148, 15);
		frmHey.getContentPane().add(cText);
		cText.setText(info[0]);

		/*
		 * 上方信息显示区end
		 * 
		 */

		/*
		 * 下方角色选择区
		 */

		// 首先声明注销按钮但不进行事件书写，只为了ruleButton的点击事件调用，解决二者相互调用，鸡生蛋蛋生鸡的问题
		final JButton unloadBtn = new JButton("注销");

		// 角色选择提示标签
		final JLabel ruleHint = new JLabel("请选择角色再进行相关操作：");
		ruleHint.setBounds(253, 104, 200, 15);
		frmHey.getContentPane().add(ruleHint);

		// 角色选择下拉框
		String[] rule = { "生成机器码（用于向服务器获取注册文件）", "激活（使用注册文件激活软件)" };
		final JComboBox<String> roleChooser = new JComboBox<String>(rule);/* JCombo类的String泛型 */
		roleChooser.setSelectedIndex(1);
		roleChooser.setBounds(253, 138, 316, 21);
		frmHey.getContentPane().add(roleChooser);

		// 登录按钮
		final JButton roleButton = new JButton("选择");
		roleButton.setBounds(359, 186, 80, 21);
		roleButton.addMouseListener(new MouseAdapter() {
			/**
			 * Title: mouseClicked Description: 点击登陆按钮进入登录后界面
			 * 
			 * @param e
			 *            鼠标响应事件
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				if (roleChooser.getSelectedIndex() == 1) {

					// 显示系统信息组件
					cText.setVisible(true);
					cHint.setVisible(true);
					dText.setVisible(true);
					dHint.setVisible(true);
					vText.setVisible(true);
					vHint.setVisible(true);
					fHint.setVisible(true);
					fSwitch.setVisible(true);
					vText.setEnabled(false);
					fSwitch.setEnabled(false);

					// 显示激活文件相关组件
					fetchFile.setVisible(true);
					fetchHint.setVisible(true);
					fetchText.setVisible(true);
					fetch.setVisible(true);
					fetchText.requestFocus();

					// 隐藏登陆组件
					ruleHint.setVisible(false);
					roleChooser.setVisible(false);
					roleButton.setVisible(false);

					// 显示注销按钮
					unloadBtn.setVisible(true);
				} else {

					// 显示系统信息组件
					cText.setVisible(true);
					cHint.setVisible(true);
					dText.setVisible(true);
					dHint.setVisible(true);
					vText.setVisible(true);
					vHint.setVisible(true);
					fHint.setVisible(true);
					fSwitch.setVisible(true);
					vText.setEnabled(true);
					fSwitch.setEnabled(true);

					// 显示生成文件相关组件
					initFile.setVisible(true);
					initHint.setVisible(true);
					initText.setVisible(true);
					init.setVisible(true);
					initText.requestFocus();

					// 隐藏登陆组件
					ruleHint.setVisible(false);
					roleChooser.setVisible(false);
					roleButton.setVisible(false);

					// 显示注销按钮
					unloadBtn.setVisible(true);

				}
			}

		});
		frmHey.getContentPane().add(roleButton);

		// 退出按钮，用于切换角色（与选择框和登陆按钮不同时可见）
		unloadBtn.setBounds(492, 325, 93, 23);
		unloadBtn.setVisible(false);
		frmHey.getContentPane().add(unloadBtn);
		unloadBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				// 隐藏系统信息组件
				cText.setVisible(false);
				cHint.setVisible(false);
				dText.setVisible(false);
				dHint.setVisible(false);
				vText.setVisible(false);
				vHint.setVisible(false);
				fHint.setVisible(false);
				fSwitch.setVisible(false);

				// 隐藏激活文件相关组件
				fetchFile.setVisible(false);
				fetchHint.setVisible(false);
				fetchText.setVisible(false);
				fetch.setVisible(false);

				// 隐藏生成文件相关组件
				initFile.setVisible(false);
				initHint.setVisible(false);
				initText.setVisible(false);
				init.setVisible(false);

				// 显示登陆组件
				ruleHint.setVisible(true);
				roleChooser.setVisible(true);
				roleButton.setVisible(true);

				// 隐藏注销按钮
				unloadBtn.setVisible(false);
			}
		});

		/*
		 * 左侧提示区域
		 * 
		 */
		JTextPane txtpnkey = new JTextPane();
		txtpnkey.setText(
				"\r\n\r\n\r\n\r\n     尊敬的用户，要激活软件，请采取以下两个步骤：\r\n     1、生成机器码，通过网页或邮件方式提交给我们，稍后您将会获得密钥文件(后缀名.mkey)\r\n     2、选择激活选项，载入您获取到的密钥文件(后缀名.key)\r\n     3、请勿在提交机器码之后随意修改版本号和功能开关，可能导致注册文件无法使用！");
		txtpnkey.setEditable(false);
		txtpnkey.setBounds(0, 0, 169, 399);
		frmHey.getContentPane().add(txtpnkey);
		/*
		 * 左侧提示区域end
		 * 
		 */

		// 显示窗体
		frmHey.setVisible(true);

	}

}
