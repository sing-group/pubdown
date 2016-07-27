package es.uvigo.ei.sing.pubdown.web.zk.tree;

import java.util.List;

/**
 * Interface for the {@link RepositoryTreeModel} methods
 * 
 */
public interface CustomTreeNode {
	public String getLabel();

	public boolean isRepository();

	public CustomTreeNode getParent();

	public void setParent(CustomTreeNode parent);

	public List<CustomTreeNode> getChildren();

	public void appendChild(CustomTreeNode child);

	public void removeChild(CustomTreeNode child);
}
