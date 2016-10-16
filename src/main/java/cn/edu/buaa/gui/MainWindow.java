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
import javax.swing.tree.TreePath;

import com.seaglasslookandfeel.SeaGlassLookAndFeel;

import cn.edu.buaa.assembler.Assembler;
import cn.edu.buaa.lexer.Lexer;
import cn.edu.buaa.parser.Parser;
import cn.edu.buaa.prover.Prover;
import cn.edu.buaa.recorder.Recorder;

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
import java.util.Enumeration;
import java.util.List;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;


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
	private JButton btnExpandAll;
	private JButton btnCollapseAll;
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
	private DefaultMutableTreeNode sourceRoot;
	private JTree sourceTree;
	private CustomTreeCellRenderer sourceRenderer;
	private JScrollPane sourceScrollPane;
	private DefaultTreeModel goalModel;
	private DefaultMutableTreeNode goalRoot;
	private JTree goalTree;
	private CustomTreeCellRenderer goalRenderer;
	private JScrollPane goalScrollPane;
	private DefaultTreeModel proveModel;
	private DefaultMutableTreeNode proveRoot;
	private JTree proveTree;
	private JScrollPane proveScrollPane;
	
	/**
	 * System
	 */
	private String srcPath = "src/main/resources/input/evenSum.c";
	private List<String> sources;
	private List<String> sourceLabels;
	private List<String> goals;
	private List<String> goalLabels;
	
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
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(50, 30, 1000, 710);	// 设置窗口大小
		setBounds(50, 30, 800, 600);
		setTitle("Source File : " + getSrcFileName() + ".c");		// 由输入文件指定
//		setAlwaysOnTop(true);
		
		menuPanel = new JPanel();
		menuPanel.setOpaque(false);	
		
		contentPane = new CustomPanel();
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
		sourceRoot = new DefaultMutableTreeNode(new Node("SourceCode"));
		sourceModel = new DefaultTreeModel(sourceRoot);
		sourceTree = new JTree(sourceModel);
		sourceTree.putClientProperty("JTree.lineStyle", "None");	// 撤销父子节点之间的连线
		sourceTree.setBackground(Color.LIGHT_GRAY);
		sourceTree.setRootVisible(false);
		sourceRenderer = new CustomTreeCellRenderer(sourceTree.getCellRenderer());
		sourceTree.setCellRenderer(sourceRenderer);
		sourceTree.addTreeSelectionListener(new TreeSelectionListener() {		// 添加选择事件
            @Override
            public void valueChanged(TreeSelectionEvent e) {                
                TreePath[] paths = sourceTree.getSelectionPaths();
                if (paths == null) return;
                
                sourceRenderer.keys.clear();
                goalRenderer.keys.clear();
                goalModel.reload(goalRoot.getLastChild());
                for (TreePath path : paths) {
                	DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                	Node user = (Node) node.getUserObject();
                	
                	goalRenderer.keys.add(user);
                	
                	Enumeration<DefaultMutableTreeNode> goals = goalRoot.breadthFirstEnumeration();
                    while (goals.hasMoreElements()) {
                    	DefaultMutableTreeNode cur = goals.nextElement();
                    	if (user.equals(cur.getUserObject())) {
                    		// 高亮显示               
                    		TreeNode[] nodes = goalModel.getPathToRoot(cur);
                    		TreePath treePath = new TreePath(nodes);
                    		goalTree.makeVisible(treePath);
                    		goalTree.scrollPathToVisible(treePath);
                    	}
                    }
                }
                goalTree.repaint();
                sourceTree.repaint();
            }
        });
		sourceScrollPane.setViewportView(sourceTree);
		
		goalRoot = new DefaultMutableTreeNode(new Node("GoalCode"));
		goalModel = new DefaultTreeModel(goalRoot);
		goalTree = new JTree(goalModel);
		goalTree.putClientProperty("JTree.lineStyle", "None");
		goalTree.setBackground(Color.LIGHT_GRAY);
		goalTree.setRootVisible(false);
		goalRenderer = new CustomTreeCellRenderer(goalTree.getCellRenderer());
		goalTree.setCellRenderer(goalRenderer);
		goalTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath[] paths = goalTree.getSelectionPaths();
                if (paths == null) return;
                
                sourceRenderer.keys.clear();
                goalRenderer.keys.clear();
                sourceModel.reload(sourceRoot.getLastChild());
                for (TreePath path : paths) {
                	DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                	Node user = (Node) node.getUserObject();
                	
                	sourceRenderer.keys.add(user);
                	
                	Enumeration<DefaultMutableTreeNode> goals = sourceRoot.breadthFirstEnumeration();
                    while (goals.hasMoreElements()) {
                    	DefaultMutableTreeNode cur = goals.nextElement();
                    	if (user.equals(cur.getUserObject())) {
                    		TreeNode[] nodes = sourceModel.getPathToRoot(cur);
                    		TreePath treePath = new TreePath(nodes);
                    		sourceTree.makeVisible(treePath);
                    		sourceTree.scrollPathToVisible(treePath);
                    	}
                    }
                }
                goalTree.repaint();
                sourceTree.repaint();
            }
        });
		goalScrollPane.setViewportView(goalTree);
		
		proveRoot = makeSourceTree();
		proveModel = new DefaultTreeModel(proveRoot);
		proveTree = new JTree(proveModel);
		proveTree.putClientProperty("JTree.lineStyle", "None");
		proveTree.setBackground(Color.LIGHT_GRAY);
		proveTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) proveTree.getLastSelectedPathComponent();
                if (node == null) return;
                
                Object object = node.getUserObject();
                Node user = (Node) object;
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
		lblStatus.setText("Status : (Started)");
		
		btnRun = new JButton("Run");
		btnRun.setUI(new CustomButtonUI());
		btnRun.setForeground(Color.RED);
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblStatus.setText("Status : (Running)");
				runApp();
			}
		});
		btnRun.setUI(new CustomButtonUI());
		
		// 选定一个源文件
		btnOpen = new JButton("Open");
		btnOpen.setUI(new CustomButtonUI());
		btnOpen.setForeground(Color.RED);
		chooser = new JFileChooser("./src/main/resources/input");
		btnOpen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Allowed File", "c", "C", "cpp");
				chooser.setFileFilter(filter);
				
				int value = chooser.showOpenDialog(MainWindow.this);
				if (value == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();					
					srcPath = file.getAbsolutePath();
					setTitle("Source File : " + file.getName());
				}
			}
		});
		
		btnExit = new JButton("About");
		btnExit.setUI(new CustomButtonUI());
		btnExit.setForeground(Color.RED);
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
				
			}
		});
		
		btnExpandAll = new JButton("ExpandAll");
		btnExpandAll.setUI(new CustomButtonUIForContent());
		btnExpandAll.setForeground(Color.LIGHT_GRAY);
		btnExpandAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showAllNodes(sourceRoot, sourceModel, sourceTree);
				showAllNodes(goalRoot, goalModel, goalTree);
				showAllNodes(proveRoot, proveModel, proveTree);
			}
		});
		
		btnCollapseAll = new JButton("CollapseAll");
		btnCollapseAll.setUI(new CustomButtonUIForContent());
		btnCollapseAll.setForeground(Color.LIGHT_GRAY);
		btnCollapseAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sourceModel.reload(sourceRoot.getLastChild());
				goalModel.reload(goalRoot.getLastChild());
				proveModel.reload(goalRoot.getLastChild());
			}
		});

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
					.addComponent(btnExpandAll)
					.addGap(18)
					.addComponent(btnCollapseAll)
					.addContainerGap())
		);
		gl_statusPanel.setVerticalGroup(
			gl_statusPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_statusPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_statusPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblStatus, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_statusPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnExpandAll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnCollapseAll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addGap(4))
		);
		menuPanel.setLayout(gl_menuPanel);
		contentPane.setLayout(gl_contentPane);
		statusPanel.setLayout(gl_statusPanel);
	}
	
	// 运行整个业务系统
	protected void runApp() {
		new SwingWorker<List<String>, String>() {

			@Override
			protected List<String> doInBackground() throws Exception {
				// 公共记录
				Recorder recorder = new Recorder();

				Lexer lexer = new Lexer(srcPath, recorder);
				lexer.runLexer();
				lexer.outputSrc();
				lexer.outputLabelSrc();
				lexer.outputLexer();
				
				Parser parser = new Parser(lexer.getTokens(), recorder);
				parser.runParser();
				parser.outputParser();

				Prover prover = new Prover(recorder, srcPath);
				Assembler assembler = new Assembler(parser.getTree(), recorder, prover);
				assembler.runAssembler();
				assembler.generateAssemblerFile(srcPath);
				assembler.generateSymbolTableFile();
				assembler.outputAssembler();
				
				sources = lexer.getSrcs();
				sourceLabels = lexer.getLabels();
				
				goals = assembler.getValues();
				goalLabels = assembler.getLabels();
				
				return null;
			}
			
			@Override
			protected void process(List<String> chunks) {
				super.process(chunks);
			}
			
			@Override
			protected void done() {
				/**
				 * 绘制sourceTree
				 */
				Enumeration<DefaultMutableTreeNode> children = sourceRoot.children();
				while (children.hasMoreElements()) {
					DefaultMutableTreeNode cur = children.nextElement();
					sourceModel.removeNodeFromParent(cur);		// 删除旧的节点
				}
				DefaultMutableTreeNode tmp = makeTree(sources, sourceLabels, sourceModel, getSrcFileName() + ".c");
				sourceModel.insertNodeInto(tmp, sourceRoot, 0);		// 增加新的节点
				sourceModel.reload();
				if (!tmp.isLeaf()) {
					TreeNode[] nodes = sourceModel.getPathToRoot(tmp.getLastChild());
					sourceTree.makeVisible(new TreePath(nodes));
				}
				
				/**
				 * 绘制goalTree
				 */
				children = goalRoot.children();
				while (children.hasMoreElements()) {
					DefaultMutableTreeNode cur = children.nextElement();
					goalModel.removeNodeFromParent(cur);		// 删除旧的节点
				}
				tmp = makeGoalTree(getSrcFileName() + ".s");
				goalModel.insertNodeInto(tmp, goalRoot, 0);		// 增加新的节点
				goalModel.reload();
				if (!tmp.isLeaf()) {
					TreeNode[] nodes = goalModel.getPathToRoot(tmp.getLastChild());
					goalTree.makeVisible(new TreePath(nodes));
				}
				
				/**
				 * 绘制proveTree
				 */
								
				
				lblStatus.setText("Status : (Completed)");
			}
			
		}.execute();
	}

	
	protected DefaultMutableTreeNode makeGoalTree(String topName) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new Node(topName));
		for (int i = 0; i < goals.size(); i++) {
			String value = goals.get(i);
			String label = goalLabels.get(i);
			if (label == null || label.trim().length() == 0) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
						new Node(value));
				DefaultMutableTreeNode parentNode = top;
				sourceModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
			} else {
				DefaultMutableTreeNode father = new DefaultMutableTreeNode(new Node("# " + label, label));
				while (i < goals.size()) {
					String subValue = goals.get(i);
					String subLabel = goalLabels.get(i);
					if (subLabel == null || subLabel.trim().length() == 0 || !subLabel.equals(label)) {
						break;
					}
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
							new Node(subValue, subLabel));
					DefaultMutableTreeNode parentNode = father;
					sourceModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
					
					i++;
				}
				i--;
				sourceModel.insertNodeInto(father, top, top.getChildCount());
			}
		}
		
		return top;
	}

	protected DefaultMutableTreeNode makeTree(List<String> codes, List<String> labels, DefaultTreeModel model, String topName) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new Node(topName));
		
		for (int i = 0; i < codes.size(); i++) {
			String value = codes.get(i);
			if (value == null || value.trim().length() == 0) continue;
			
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new Node(value, labels.get(i)));
			int newLevel = getLevel(newNode);
			DefaultMutableTreeNode parentNode = top;
			
			Enumeration<DefaultMutableTreeNode> goals = top.breadthFirstEnumeration();
            while (goals.hasMoreElements()) {
            	DefaultMutableTreeNode cur = goals.nextElement();
            	int curLevel = getLevel(cur);
            	
            	// 为第一层子节点
            	if (curLevel == 0 && newLevel == 1) {
            		parentNode = cur;
            		break;
            	} else if (curLevel != 0 && curLevel + 1 == newLevel && isParentAndSon(cur, newNode)) {
            		parentNode = cur;
            		break;
            	}
            }
            
            model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
		}
		
		return top;
	}
	
	private int getLevel(DefaultMutableTreeNode newNode) {
		Node a = (Node) newNode.getUserObject();
		if (a.getLabel() == null || a.getLabel().trim().length() == 0) {
			return 0;
		} else {
			String label = a.getLabel();
			String[] ls = label.trim().split("\\.");
			return ls.length;
		}
	}
	
	private boolean isParentAndSon(DefaultMutableTreeNode cur, DefaultMutableTreeNode newNode) {
		Node a = (Node) cur.getUserObject();
		Node b = (Node) newNode.getUserObject();
		
		String subLabel = b.getLabel().substring(0, b.getLabel().lastIndexOf("."));
		return a.getLabel().equals(subLabel);
	}
	
	private String getSrcFileName() {
		String tmp = srcPath.substring(srcPath.lastIndexOf("/") + 1);
		return tmp.substring(0, tmp.lastIndexOf("."));
	}

	protected void showAllNodes(DefaultMutableTreeNode root, DefaultTreeModel model, JTree tree) {
		TreeNode[] nodes = model.getPathToRoot(root);
		TreePath path = new TreePath(nodes);
		tree.makeVisible(path);
				
		if (root.isLeaf()) return;
		
		Enumeration<DefaultMutableTreeNode> subs = root.children();
        while (subs.hasMoreElements()) {
        	DefaultMutableTreeNode cur = subs.nextElement();
        	showAllNodes(cur, model, tree);
        }
	}

	private DefaultMutableTreeNode makeSourceTree() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(new Node("软件部"));
		DefaultMutableTreeNode node11 = new DefaultMutableTreeNode(new Node("小组"));
		node11.add(new DefaultMutableTreeNode(new Node("hehe")));
		node11.add(new DefaultMutableTreeNode(new Node("haha")));
        node1.add(node11);
        node1.add(new DefaultMutableTreeNode(new Node("小虎")));
        node1.add(new DefaultMutableTreeNode(new Node("小龙")));
 
        DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Node("销售部"));
        node2.add(new DefaultMutableTreeNode(new Node("小叶")));
        node2.add(new DefaultMutableTreeNode(new Node("小雯")));
        node2.add(new DefaultMutableTreeNode(new Node("小夏")));
 
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new Node("职员管理"));
//        top.add(new DefaultMutableTreeNode(new Node("总经理")));
//        top.add(node1);
//        top.add(node2);
        
		return top;
	}
}
