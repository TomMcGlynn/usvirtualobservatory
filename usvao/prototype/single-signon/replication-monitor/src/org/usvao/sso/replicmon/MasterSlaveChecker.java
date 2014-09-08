package org.usvao.sso.replicmon;

import org.apache.log4j.Logger;

import java.sql.SQLException;

/** Check the integrity of a connection between a replication master and a slave. */
public class MasterSlaveChecker {
    private static final Logger log = Logger.getLogger(MasterSlaveChecker.class);

    private DbReadWriter master, slave;

    public MasterSlaveChecker(DbReadWriter master, DbReadWriter slave) {
        this.master = master;
        this.slave = slave;
    }

    /** Ensure that replication is working.  If it isn't, throw an exception.
     *  @param filterColumnValue the value of the filter column by which to identify our row
     *  @return the number of milliseconds (rounded to approximately the nearest 100) it too for replication
     *  to take effect. */
    public long checkReplication(Object filterColumnValue)
            throws SQLException, ReplicationFailedException
    {
        String filterColumnName = master.getConfig().getUberConfig().getFilterColumn();
        log.trace("Filter column = " + filterColumnName + ", value = " + filterColumnValue);

        // 1. get initial Master and Slave value
        String where = DbReadWriter.where(filterColumnName, filterColumnValue);
        log.trace("Where = " + where);
        Integer masterBefore = master.readInt(where), slaveBefore = slave.readInt(where);
        log.trace("master before = " + masterBefore + "; slave before = " + slaveBefore);
        // TODO recover gracefully from this so that replication doesn't break and stay broken
        if (Compare.differ(masterBefore, slaveBefore)) throw new ReplicationFailedException
                ("Initial values on master \"" + master.getConfig().getName() + "\" (" + masterBefore + ") "
                        + "and slave \"" + slave.getConfig().getName() + "\" (" + slaveBefore + ") differ.");

        // 2. increment master
        Integer masterAfter = masterBefore == null ? 1 : masterBefore + 1;
        if (masterBefore == null) // if no current value, insert a new row with value 1
            master.insertInt(filterColumnName, filterColumnValue, masterAfter);
        else
            master.updateInt(masterAfter, where);
        long start = System.currentTimeMillis(), lap = start;

        // 3. wait for slave to catch up
        long result = -1;
        while (result < 0 && System.currentTimeMillis() - start < master.getConfig().getUberConfig().getPatienceMillis()) {
            log.trace("Waiting for slave: " + result + " ms elapsed");
            Integer slaveAfter = slave.readInt(where);
            if (masterAfter.equals(slaveAfter)) {
                result = System.currentTimeMillis() - start;
                log.trace("Slave caught up after " + result + " ms");
            }
            else {
                long elapsed = System.currentTimeMillis() - lap;
                if (elapsed < 100)
                    try { Thread.sleep(100 - elapsed); } catch (InterruptedException ignored) { }
                log.trace("Slave didn't catch up after " + (System.currentTimeMillis() - start) + " ms");
            }
            lap = System.currentTimeMillis();
        }

        if (result >= 0)
            return result;

        else
            // 4. complain if slave didn't catch up in time
            throw new ReplicationFailedException("Replication from " + master.getConfig().getName() + " to "
                    + slave.getConfig().getName() + " timed out after " + (System.currentTimeMillis() - start) + " millis.");
    }

    public DbReadWriter getMaster() { return master; }
    public DbReadWriter getSlave() { return slave; }

    public String toDebugString() {
        return "MasterSlaveChecker{" +
                "master=" + master.toDebugString() +
                ", slave=" + slave.toDebugString() +
                '}';

    }
}
