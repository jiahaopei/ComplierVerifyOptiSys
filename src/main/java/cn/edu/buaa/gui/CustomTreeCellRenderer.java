package cn.edu.buaa.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

class CustomTreeCellRenderer extends DefaultTreeCellRenderer {  
    /**
	 * 
	 */
	private static final long serialVersionUID = 7686967801067184641L;
	
	private final TreeCellRenderer renderer;  
    public List<Node> keys = new ArrayList<>();
  
    public CustomTreeCellRenderer(TreeCellRenderer renderer) {  
        this.renderer = renderer;  
    }
  
    @Override
    public Component getTreeCellRendererComponent(  
            JTree tree, Object value, boolean isSelected, boolean expanded,  
            boolean leaf, int row, boolean hasFocus) {  
        JComponent c = (JComponent) renderer.getTreeCellRendererComponent(  
                tree, value, isSelected, expanded, leaf, row, hasFocus);
        
        if (isSelected) {  
            c.setOpaque(false);  
            c.setForeground(Color.WHITE);
            
        } else {
            c.setOpaque(false);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Node user = (Node) node.getUserObject(); 
            
            if (keys != null && keys.contains(user)) {
                c.setForeground(Color.YELLOW);  
            } else {
            	c.setForeground(Color.BLACK);
            }
        }  
        return c;  
    }  
}
