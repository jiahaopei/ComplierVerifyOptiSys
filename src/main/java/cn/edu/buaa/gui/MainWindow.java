package cn.edu.buaa.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.seaglasslookandfeel.SeaGlassLookAndFeel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;


public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	// 记录窗体随鼠标移动参数
	private int mx;
	private int my;
	private int jfx;
	private int jfy;
	
	/**
	 * 组件
	 */
	private JPanel contentPane;
	private JButton btnMax;
	private JButton btnMin;
	private JButton btnExit;
	private JButton btnRun;
	private JButton btnOpen;
	private JFileChooser chooser;
	private JPanel menuPanel;
	private JLabel lblStatus;
	private JLabel lblNewLabel;
	private JPanel statusPanel;
	
	/**
	 * JTree
	 */
	private DefaultTreeModel sourceModel;
	private TreeNode sourceRoot;
	private JTree sourceTree;
	private JScrollPane sourceScrollPane;
	private DefaultTreeModel goalModel;
	private TreeNode goalRoot;
	private JTree goalTree;	
	private JScrollPane goalScrollPane;
	private DefaultTreeModel proveModel;
	private TreeNode proveRoot;
	private JTree proveTree;
	private JScrollPane proveScrollPane;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
					frame.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		
		try {
			UIManager.setLookAndFeel(new SeaGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e1) {
			try {
				UIManager.setLookAndFeel(new NimbusLookAndFeel());
			} catch (UnsupportedLookAndFeelException e2) {
				throw new RuntimeException("JDK版本过低，请把JDK升级到1.7(含)以上！");
			}
		}
		
		// 设置键盘监听事件
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {				
				// 按退出键时，退出程序
				if (e.getKeyCode() == 0) {
					System.exit(0);
				}
			}
			
		});
		
		// 设定背景
//		setUndecorated(true);
//		setBackground(new Color(0, 0, 0, 0));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(50, 30, 1000, 710);	// 设置窗口大小
		setBounds(50, 30, 600, 450);
		setTitle("Source File : ");		// 由输入文件指定
		setAlwaysOnTop(true);
		
		menuPanel = new JPanel();
		menuPanel.setOpaque(false);	
		
		contentPane = new MyPanel();
		contentPane.setOpaque(false);
		contentPane.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				setLocation(jfx + (e.getXOnScreen() - mx), jfy + (e.getYOnScreen() - my));
			}
		});
		contentPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mx = e.getXOnScreen();
				my = e.getYOnScreen();
				jfx = getX();
				jfy = getY();
			}
		});
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		statusPanel = new JPanel();
		statusPanel.setOpaque(false);
		statusPanel.setBackground(Color.WHITE);
		
		sourceScrollPane = new JScrollPane();
		sourceScrollPane.setOpaque(false);
		sourceScrollPane.setBackground(Color.DARK_GRAY);
		
		goalScrollPane = new JScrollPane();
		goalScrollPane.setOpaque(false);
		goalScrollPane.setBackground(Color.DARK_GRAY);
		
		proveScrollPane = new JScrollPane();
		proveScrollPane.setOpaque(false);
		proveScrollPane.setBackground(Color.DARK_GRAY);
		
		
		// 树
		sourceRoot = makeSourceTree();
		sourceModel = new DefaultTreeModel(sourceRoot);
		sourceTree = new JTree(sourceModel);
		sourceTree.putClientProperty("JTree.lineStyle", "None");	// 撤销父子节点之间的连线
//		sourceTree.setRootVisible(false);	// 隐藏根节点
		sourceTree.setBackground(Color.LIGHT_GRAY);
		sourceTree.addTreeSelectionListener(new TreeSelectionListener() {		// 添加选择事件
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent();
                if (node == null) return;
                
                Object object = node.getUserObject();
//                if (node.isLeaf()) {
                    User user = (User) object;
                    System.out.println("你选择了：" + user.toString());
//                }
            }
        });
		sourceScrollPane.setViewportView(sourceTree);
		
		goalRoot = makeSourceTree();
		goalModel = new DefaultTreeModel(goalRoot);
		goalTree = new JTree(goalModel);
		goalTree.putClientProperty("JTree.lineStyle", "None");
//		goalTree.setRootVisible(false);
		goalTree.setBackground(Color.LIGHT_GRAY);
		goalTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) goalTree.getLastSelectedPathComponent();
                if (node == null) return;
                
                Object object = node.getUserObject();
                User user = (User) object;
                System.out.println("你选择了：" + user.toString());
 
            }
        });
		goalScrollPane.setViewportView(goalTree);
		
		proveRoot = makeSourceTree();
		proveModel = new DefaultTreeModel(proveRoot);
		proveTree = new JTree(proveModel);
		proveTree.putClientProperty("JTree.lineStyle", "None");
//		proveTree.setRootVisible(false);
		proveTree.setBackground(Color.LIGHT_GRAY);
		proveTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) proveTree.getLastSelectedPathComponent();
                if (node == null) return;
                
                Object object = node.getUserObject();
                User user = (User) object;
                System.out.println("你选择了：" + user.toString());
 
            }
        });
		proveScrollPane.setViewportView(proveTree);
		
		// 设置标题
		lblNewLabel = new JLabel();
		lblNewLabel.setHorizontalAlignment(JLabel.CENTER);
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setText("Compiler Verification System");
		
		// 状态栏
		lblStatus = new JLabel();
		lblStatus.setHorizontalAlignment(JLabel.LEFT);
		lblStatus.setForeground(Color.WHITE);
		lblStatus.setText("Status : ");
		
		btnRun = new JButton("Run");
		btnRun.setUI(new MyButtonUI());
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("Run");
				
			}
		});
		btnRun.setUI(new MyButtonUI());
		
		// 选定一个源文件
		btnOpen = new JButton("Open");
		chooser = new JFileChooser("./src/main/resources/input");
		btnOpen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Allowed File", "c", "C", "cpp");
				chooser.setFileFilter(filter);
				
				int value = chooser.showOpenDialog(MainWindow.this);
				if (value == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					System.out.println(file.getAbsolutePath());
					
					setTitle("Source File : " + file.getName());
				}
				
			}
		});
		btnOpen.setUI(new MyButtonUI());
		
		btnExit = new JButton("Exit");
		btnExit.setUI(new MyButtonUI());
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
				
			}
		});
		btnExit.setUI(new MyButtonUI());
		
		btnMax = new JButton("Max");
		btnMax.setUI(new MyButtonUI());
		btnMax.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (getExtendedState() == MAXIMIZED_BOTH) {
					setExtendedState(JFrame.NORMAL);
				} else {
					setExtendedState(JFrame.MAXIMIZED_BOTH);
				}
			}
		});
		btnMax.setUI(new MyButtonUI());
		
		btnMin = new JButton("Min");
		btnMin.setUI(new MyButtonUI());
		btnMin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setExtendedState(JFrame.ICONIFIED);
			}
		});
		btnMin.setUI(new MyButtonUI());

		GroupLayout gl_menuPanel = new GroupLayout(menuPanel);
		gl_menuPanel.setHorizontalGroup(
			gl_menuPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_menuPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnRun)
					.addGap(18)
					.addComponent(btnOpen)
					.addGap(18)
					.addComponent(btnExit)
					.addContainerGap(335, Short.MAX_VALUE))
		);
		gl_menuPanel.setVerticalGroup(
			gl_menuPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_menuPanel.createSequentialGroup()
					.addContainerGap(10, Short.MAX_VALUE)
					.addGroup(gl_menuPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnRun)
						.addComponent(btnOpen)
						.addComponent(btnExit))
					.addContainerGap())
		);
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(menuPanel, GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
				.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(statusPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(22)
					.addComponent(sourceScrollPane, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
					.addGap(27)
					.addComponent(goalScrollPane, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
					.addGap(28)
					.addComponent(proveScrollPane, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
					.addGap(21))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblNewLabel)
					.addGap(9)
					.addComponent(menuPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(30)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(proveScrollPane, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
						.addComponent(goalScrollPane, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
						.addComponent(sourceScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
		);
		
		GroupLayout gl_statusPanel = new GroupLayout(statusPanel);
		gl_statusPanel.setHorizontalGroup(
			gl_statusPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_statusPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblStatus, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 284, Short.MAX_VALUE)
					.addComponent(btnMax)
					.addGap(18)
					.addComponent(btnMin)
					.addContainerGap())
		);
		gl_statusPanel.setVerticalGroup(
			gl_statusPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_statusPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_statusPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblStatus, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_statusPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnMax, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnMin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addGap(4))
		);
		menuPanel.setLayout(gl_menuPanel);
		contentPane.setLayout(gl_contentPane);
		statusPanel.setLayout(gl_statusPanel);
	}

	private TreeNode makeSourceTree() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(new User("软件部"));
		DefaultMutableTreeNode node11 = new DefaultMutableTreeNode(new User("小组"));
		node11.add(new DefaultMutableTreeNode(new User("hehe")));
		node11.add(new DefaultMutableTreeNode(new User("haha")));
        node1.add(node11);
        node1.add(new DefaultMutableTreeNode(new User("小虎")));
        node1.add(new DefaultMutableTreeNode(new User("小龙")));
 
        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new User("销售部"));
        node2.add(new DefaultMutableTreeNode(new User("小叶")));
        node2.add(new DefaultMutableTreeNode(new User("小雯")));
        node2.add(new DefaultMutableTreeNode(new User("小夏")));
 
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new User("职员管理"));
        top.add(new DefaultMutableTreeNode(new User("总经理")));
        top.add(node1);
        top.add(node2);
        
		return top;
	}
	
}
