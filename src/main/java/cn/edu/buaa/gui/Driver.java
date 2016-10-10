package cn.edu.buaa.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class Driver {

	public static void main(String[] args) {
		
		
		
		JMenuBar menub = new JMenuBar();
		JMenu fileMenub = new JMenu("File");
		JMenuItem exitb = new JMenuItem("Exit", KeyEvent.VK_X);
		fileMenub.add(exitb);
		menub.add(fileMenub);
		
		JPanel n = new JPanel();
		n.setLayout(new BorderLayout());
		n.setPreferredSize(new Dimension(300, 200));
		n.add(menub, BorderLayout.NORTH);
		
		JFrame f = new JFrame("MenuBarTest");
		f.add(n, BorderLayout.SOUTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

}


