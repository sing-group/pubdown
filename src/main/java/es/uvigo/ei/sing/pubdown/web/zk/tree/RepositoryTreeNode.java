package es.uvigo.ei.sing.pubdown.web.zk.tree;

import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a category node in the {@link RepositoryTreeModel}
 * 
 */
public class RepositoryTreeNode extends CategorizableRepositoryTreeNode {
	private final String name;
	private final List<CustomTreeNode> children;

	/**
	 * Empty constructor
	 */
	public RepositoryTreeNode() {
		this(null, null);
	}

	/**
	 * Constructs a {@link RepositoryTreeNode}
	 * 
	 * @param name
	 *            the name of the {@link RepositoryTreeNode}
	 * @param parent
	 *            the parent of the {@link RepositoryTreeNode}
	 */
	public RepositoryTreeNode(final String name, final RepositoryTreeNode parent) {
		super(parent);
		this.name = name;
		this.children = new LinkedList<>();
	}

	/**
	 * Gets the {@link RepositoryTreeNode} label
	 * 
	 * @return the {@link RepositoryTreeNode} name
	 */
	@Override
	public String getLabel() {
		return name;
	}

	/**
	 * Checks if is a repository node
	 * 
	 * @return <code>true</code>
	 */
	@Override
	public boolean isRepository() {
		return true;
	}

	/**
	 * Returns a {@link CustomTreeNode} sorted list with the children of a
	 * {@link RepositoryTreeNode}
	 * 
	 * @return the children of a {@link RepositoryTreeNode}
	 */
	@Override
	public List<CustomTreeNode> getChildren() {
		final List<CustomTreeNode> sortedChildren = this.children;

		sort(sortedChildren, (r1, r2) -> {
			final int categoryCmp = Boolean.compare(r1.isRepository(), r2.isRepository());

			if (categoryCmp == 0) {
				return r1.getLabel().compareTo(r2.getLabel());
			} else {
				return categoryCmp;
			}
		});

		return sortedChildren;
	}

	/**
	 * Appends a child in a {@link RepositoryTreeNode}
	 * 
	 * @param child
	 *            the child to append
	 */
	@Override
	public void appendChild(final CustomTreeNode child) {
		requireNonNull(child, "child can't be null");

		child.setParent(this);
	}

	/**
	 * Removes a child in a {@link RepositoryTreeNode}
	 * 
	 * @param child
	 *            the child to remove
	 */
	@Override
	public void removeChild(final CustomTreeNode child) {
		if (!this.children.contains(child)) {
			child.setParent(null);
		}
	}

	void effectiveRemoveChild(final CustomTreeNode child) {
		this.children.remove(child);
	}

	void effectiveAppendChild(final CustomTreeNode child) {
		if (!this.children.contains(child)) {
			this.children.add(child);
		}
	}

	/**
	 * Returns the label of a {@link RepositoryTreeNode}
	 * 
	 * @return the {@link RepositoryTreeNode} name
	 */
	@Override
	public String toString() {
		return getLabel();
	}
}
