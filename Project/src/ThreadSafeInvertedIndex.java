import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * @author Sophia
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** The lock object */
	private final SimpleReadWriteLock lock;

	/**
	 * Constructor
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public void add(String word, String file, Integer wordOccurence) {
		lock.writeLock().lock();
		try {
			super.add(word, file, wordOccurence);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void toJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.toJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void countToJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.countToJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.contains(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<QueryResult> exactSearch(Collection<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<QueryResult> partialSearch(Collection<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

}
