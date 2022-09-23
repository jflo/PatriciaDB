package io.patriciadb.fs.disk.directory.versioned;

import io.patriciadb.fs.disk.DirectoryError;
import io.patriciadb.fs.disk.directory.Directory;
import io.patriciadb.fs.disk.directory.VersionedDirectory;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.roaringbitmap.longlong.Roaring64NavigableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class MvccDirectory implements VersionedDirectory {
    private final static Logger log = LoggerFactory.getLogger(MvccDirectory.class);
    private final Directory directory;
    private final AtomicLong currentVersion = new AtomicLong(0);
    private final TreeMap<Long, LongLongHashMap> changeMap = new TreeMap<>();

    public MvccDirectory(Directory atomicDirectory) {
        this.directory = atomicDirectory;
    }

    @Override
    public synchronized void expandCapacity() throws DirectoryError {
        directory.expandCapacity();
    }

    @Override
    public synchronized long get(long blockId) throws DirectoryError {
        return directory.get(blockId);
    }


    @Override
    public synchronized Roaring64NavigableMap getFreeBlocksMap() {
        return directory.getFreeBlocksMap();
    }

    public synchronized void deleteOldSnapshots(long version) {

       var oldVersions = changeMap.headMap(version);
       log.trace("Removing {} versions older than version id {}", oldVersions.size(), version);
       oldVersions.clear();
    }

    public synchronized long get(long version, long blockId) {
        if (changeMap.isEmpty()) {
            return directory.get(blockId);
        }
        var changeDelta = changeMap.tailMap(version+1);
        if (changeDelta.isEmpty()) {
            return directory.get(blockId);
        }
        for (var map : changeDelta.values()) {
            if (map.containsKey(blockId)) {
                return map.get(blockId);
            }
        }
        return directory.get(blockId);
    }


    public synchronized long setAndGetVersion(LongLongHashMap batch) {
        var prevVersion = directory.get(batch.keySet());
        long newVersion = currentVersion.incrementAndGet();
        changeMap.put(newVersion, prevVersion);
        directory.set(batch);
        return newVersion;
    }

    public long getCurrentVersion() {
        return currentVersion.get();
    }
}
