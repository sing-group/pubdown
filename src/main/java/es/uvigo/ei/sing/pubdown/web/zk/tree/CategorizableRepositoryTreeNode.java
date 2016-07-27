package es.uvigo.ei.sing.pubdown.web.zk.tree;

/**
 * Implements getParent and setParent methods of {@link CustomTreeNode}
 * 
 */
public abstract class CategorizableRepositoryTreeNode implements CustomTreeNode {
	private RepositoryTreeNode parent;

	public CategorizableRepositoryTreeNode(final RepositoryTreeNode parent) {
		this.setParent(parent);
	}

	/**
	 * Returns the parent of a {@link RepositoryTreeNode}
	 * 
	 * @return a {@link RepositoryTreeNode}
	 */
	@Override
	public RepositoryTreeNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent of a {@link RepositoryTreeNode}
	 * 
	 * @param parentNode
	 *            the {@link RepositoryTreeNode} to be parent
	 * @throws IllegalArgumentException
	 *             if the parent node is not a {@link RepositoryTreeNode} instance
	 */
	@Override
	public void setParent(final CustomTreeNode parentNode) {
		if (parentNode == null || parentNode instanceof RepositoryTreeNode) {
			final RepositoryTreeNode parent = (RepositoryTreeNode) parentNode;
			if (this.parent != parent) {
				if (this.parent != null) {
					this.parent.effectiveRemoveChild(this);
					this.parent = null;
				}

				if (parent != null) {
					parent.effectiveAppendChild(this);
					this.parent = parent;
				}
			}
		} else {
			throw new IllegalArgumentException("parent node must be a CategoryTreeNode");
		}
	}

}