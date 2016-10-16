package cn.edu.buaa.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class CustomPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static int WID = 2;

	/**
	 * 
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		// 打开抗锯齿效果
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
//		g2d.setColor(new Color(0, 0, 0, 140));
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, getWidth(), getHeight());
//		g2d.fillRoundRect(WID / 2, WID / 2, getWidth() - WID, getHeight() - WID, 20, 20);
		
		// 绘制边
		g2d.setColor(Color.GRAY);
		g2d.setStroke(new BasicStroke(WID));
		g2d.drawRoundRect(WID / 2, WID / 2, getWidth() - WID, getHeight() - WID, 20, 20);
		g2d.fillRoundRect(WID / 2, WID / 2, getWidth() - WID, getHeight() - WID, 20, 20);
		
//		// 绘制标题栏
//		g2d.setClip(0, 0, getWidth(), 60);
//		g2d.setColor(Color.LIGHT_GRAY);
//		g2d.fillRoundRect(1, 1, getWidth() - 1, getHeight() - 1, 20, 20);
//		g2d.setClip(null);
		
		// 绘制菜单栏
		g2d.setClip(0, 30, getWidth(), 40);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRoundRect(0, 1, getWidth(), getHeight() - 1, 20, 20);
		g2d.setClip(null);
		
		// 设置字体
		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		g2d.setColor(Color.DARK_GRAY);
		//g2d.drawString("Compiler Verification System", 15, 24);	
	}
}
