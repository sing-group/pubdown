package es.uvigo.ei.sing.pubdown.execution;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class CustomBlockingQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable>, Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, Queue<Runnable>> tasksByUser;
	private final UserIds userIds;

	private final AtomicInteger count;

	private final ReadWriteLock lock;
	private final Lock readLock, writeLock;
	private final Condition emptyCondition;

	private String currentUserId; // Used when adding a new subtask

	public CustomBlockingQueue() {
		this.tasksByUser = new HashMap<>();

		this.userIds = new UserIds();

		this.count = new AtomicInteger();

		this.lock = new ReentrantReadWriteLock(true);
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();

		this.emptyCondition = this.writeLock.newCondition();
	}

	public void beginBatchAdd(final String userId) {
		this.writeLock.lock();
		this.currentUserId = userId;
	}

	public void endBatchAdd() {
		this.writeLock.unlock();
		this.currentUserId = null;
	}

	public void beginBatchRemove() {
		this.writeLock.lock();
	}

	public void endBatchRemove() {
		this.writeLock.unlock();
	}

	// Non thread safe. Must be called from a thread safe method
	private Runnable pollTask() {
		if (this.isEmpty()) {
			return null;
		} else {
			final String userId = this.userIds.next();
			final Queue<Runnable> queue = this.tasksByUser.get(userId);
			final Runnable task = queue.poll();

			if (queue.isEmpty()) {
				this.tasksByUser.remove(userId);
				this.userIds.remove(userId);
			}

			this.count.decrementAndGet();

			return task;
		}
	}

	private Runnable peekTask() {
		if (this.isEmpty()) {
			return null;
		} else {
			final String userId = this.userIds.current();
			final Queue<Runnable> queue = this.tasksByUser.get(userId);

			return queue.peek();
		}
	}

	// Must be called when this.writeLock.lock is locked
	@Override
	public boolean offer(final Runnable runnable) throws IllegalArgumentException {
		if (this.currentUserId != null) {
			final String userId = this.currentUserId;

			if (!this.tasksByUser.containsKey(userId)) {
				this.tasksByUser.put(userId, new LinkedList<Runnable>());
				this.userIds.add(userId);
			}

			this.tasksByUser.get(userId).add(runnable);
			this.count.incrementAndGet();
			this.emptyCondition.signalAll();

			return true;
		} else {
			throw new IllegalStateException("Missing user id");
		}
	}

	@Override
	public boolean offer(final Runnable e, final long timeout, final TimeUnit unit) {
		return this.add(e);
	}

	@Override
	public void put(final Runnable runnable) {
		this.offer(runnable);
	}

	@Override
	public Runnable poll() {
		this.writeLock.lock();
		try {
			return this.pollTask();
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		this.writeLock.lockInterruptibly();

		try {
			final Date until = new Date(System.currentTimeMillis() + unit.toMillis(timeout));
			while (this.isEmpty() && this.emptyCondition.awaitUntil(until))
				;

			return this.pollTask();
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public Runnable peek() {
		this.readLock.lock();
		try {
			return this.peekTask();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public Runnable take() throws InterruptedException {
		this.writeLock.lockInterruptibly();

		try {
			while (this.isEmpty()) {
				this.emptyCondition.await();
			}

			return this.pollTask();
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public int remainingCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int drainTo(final Collection<? super Runnable> c) {
		this.writeLock.lock();

		try {
			int numElements = 0;

			if (!this.isEmpty()) {
				for (final Queue<Runnable> tasks : this.tasksByUser.values()) {
					if (c.addAll(tasks)) {
						numElements += tasks.size();
					}
				}

				this.clear();
			}

			return numElements;
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public int drainTo(final Collection<? super Runnable> c, final int maxElements) {
		this.writeLock.lock();

		try {
			int numElements = 0;

			if (!this.isEmpty()) {
				for (final Map.Entry<String, Queue<Runnable>> taskEntry : this.tasksByUser.entrySet()) {
					final Queue<Runnable> tasks = taskEntry.getValue();
					final String userId = taskEntry.getKey();

					if (maxElements - numElements < tasks.size()) {
						final Iterator<Runnable> itTasks = tasks.iterator();

						while (maxElements > numElements && itTasks.hasNext()) {
							c.add(itTasks.next());
							itTasks.remove();
							numElements++;
						}

						break;
					} else {
						numElements += tasks.size();
						c.addAll(tasks);
						this.userIds.remove(userId);
						this.tasksByUser.remove(userId);
					}
				}
			}

			this.count.addAndGet(-numElements);
			return numElements;
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public Iterator<Runnable> iterator() {
		return new Iterator<Runnable>() {
			private final Iterator<String> userIds;
			private Iterator<Runnable> currentIterator;
			private String currentUserId;

			{
				CustomBlockingQueue.this.readLock.lock();
				try {
					this.userIds = CustomBlockingQueue.this.userIds.values().iterator();
				} finally {
					CustomBlockingQueue.this.readLock.unlock();
				}
			}

			@Override
			public boolean hasNext() {
				CustomBlockingQueue.this.readLock.lock();

				try {
					return (this.currentIterator != null && this.currentIterator.hasNext())
							|| (this.currentIterator == null && this.userIds.hasNext());
				} finally {
					CustomBlockingQueue.this.readLock.unlock();
				}
			}

			@Override
			public Runnable next() {
				CustomBlockingQueue.this.readLock.lock();

				try {
					if (this.currentIterator == null || !this.currentIterator.hasNext()) {
						if (this.userIds.hasNext()) {
							this.currentUserId = this.userIds.next();
							this.currentIterator = CustomBlockingQueue.this.tasksByUser.get(this.currentUserId)
									.iterator();
						} else {
							throw new NoSuchElementException();
						}
					}

					return this.currentIterator.next();
				} finally {
					CustomBlockingQueue.this.readLock.unlock();
				}
			}

			@Override
			public void remove() {
				if (this.currentUserId == null || this.currentIterator == null)
					throw new IllegalStateException("No current element selected");

				CustomBlockingQueue.this.writeLock.lock();

				try {
					if (this.currentUserId == null || this.currentIterator == null)
						throw new IllegalStateException("No current element selected");

					this.currentIterator.remove();
					CustomBlockingQueue.this.count.decrementAndGet();

					final Queue<Runnable> queue = CustomBlockingQueue.this.tasksByUser.get(this.currentUserId);

					if (queue.isEmpty()) {
						CustomBlockingQueue.this.tasksByUser.remove(this.currentUserId);
						CustomBlockingQueue.this.userIds.remove(this.currentUserId);
					}
				} finally {
					CustomBlockingQueue.this.writeLock.unlock();
				}
			}
		};
	}

	@Override
	public int size() {
		this.readLock.lock();

		try {
			int counter = 0;

			for (final Queue<Runnable> tasks : this.tasksByUser.values()) {
				counter += tasks.size();
			}

			return counter;
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public void clear() {
		this.writeLock.lock();

		try {
			this.userIds.clear();
			this.tasksByUser.clear();
			this.count.set(0);
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public Object[] toArray() {
		this.readLock.lock();

		try {
			final Runnable[] tasksArray = new Runnable[this.size()];

			int index = 0;
			for (final Queue<Runnable> tasks : this.tasksByUser.values()) {
				final int size = tasks.size();

				System.arraycopy(tasks.toArray(), 0, tasksArray, index, size);

				index += size;
			}

			return tasksArray;
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public <T extends Object> T[] toArray(final T[] a) {
		this.readLock.lock();

		try {
			@SuppressWarnings("unchecked")
			final T[] array = (a.length < this.size())
					? (T[]) Array.newInstance(a.getClass().getComponentType(), this.size()) : a;

			int index = 0;
			for (final Queue<Runnable> tasks : this.tasksByUser.values()) {
				final int size = tasks.size();

				System.arraycopy(tasks.toArray(), 0, array, index, size);

				index += size;
			}

			return array;
		} finally {
			this.readLock.unlock();
		}
	}
}