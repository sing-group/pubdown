package es.uvigo.ei.sing.pubdown.execution;

import java.util.LinkedList;
import java.util.List;

final class UserIds {
	private UserIds.Entry current;

	public UserIds() {
		this.current = null;
	}

	public void clear() {
		this.current = null;
	}

	public boolean isEmpty() {
		return this.current == null;
	}

	public String current() {
		return this.current.value;
	}

	public String next() {
		if (this.isEmpty()) {
			return null;
		} else {
			final String value = this.current.value;
			this.current = this.current.next;

			return value;
		}
	}

	public List<String> values() {
		final List<String> values = new LinkedList<>();

		if (!this.isEmpty()) {
			UserIds.Entry entry = this.current;
			do {
				values.add(entry.value);
			} while ((entry = entry.next) != this.current);
		}

		return values;
	}

	public void add(final String value) {
		if (this.isEmpty()) {
			final UserIds.Entry entry = new Entry(value);
			this.current = entry;
			entry.setNext(entry);
		} else {
			final UserIds.Entry entry = new Entry(value);
			entry.setPrevious(this.current.previous);
			entry.setNext(this.current);
		}
	}

	public boolean remove(final String value) {
		if (!this.isEmpty()) {
			UserIds.Entry entry = this.current;
			do {
				if (entry.value.equals(value)) {
					if (entry == entry.next) {
						this.current = null;
					} else {
						entry.previous.setNext(entry.next);

						if (entry == this.current)
							this.current = entry.next;
						entry.previous = null;
						entry.next = null;
					}

					return true;
				}
			} while ((entry = entry.next) != this.current);
		}

		return false;
	}

	private final static class Entry {
		public final String value;
		public UserIds.Entry next;
		public UserIds.Entry previous;

		public Entry(final String value) {
			this.value = value;
		}

		public void setNext(final UserIds.Entry entry) {
			this.next = entry;
			entry.previous = this;
		}

		public void setPrevious(final UserIds.Entry entry) {
			this.previous = entry;
			entry.next = this;
		}
	}
}