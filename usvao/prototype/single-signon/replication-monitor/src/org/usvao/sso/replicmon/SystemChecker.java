package org.usvao.sso.replicmon;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/** Check the configured list of masters and slaves. Be sure to call close() when finished. */
public class SystemChecker {
    private static final Logger log = Logger.getLogger(SystemChecker.class);

    private List<MasterSlaveChecker> checkers;
    private List<ExceptionReport> setupExceptions = new ArrayList<ExceptionReport>();

    public SystemChecker(ReplicmonConfig config) {
        Map<Integer, DbServerConfig> configs = config.extractDbServerConfigs();
        checkers = new ArrayList<MasterSlaveChecker>();
        Map<Integer, DbReadWriter> readWriters = new TreeMap<Integer, DbReadWriter>();
        // create read-writers
        for (Integer id : configs.keySet()) {
            DbServerConfig server = configs.get(id);
            try {
                readWriters.put(id, createRW(server));
            } catch(Exception e) {
                String msg = "Unable to connect to " + server.getName();
                log.info(msg, e);
                setupExceptions.add(new ExceptionReport(e, msg));
            }
        }
        // match slaves to their masters
        for (Integer id : readWriters.keySet()) {
            DbReadWriter slaveRW = readWriters.get(id);
            Integer masterId = configs.get(id).getMasterId();
            DbReadWriter masterRW = readWriters.get(masterId);
            if (masterRW == null) {
                String msg = "No master matches " + masterId + ".";
                setupExceptions.add(new ExceptionReport
                        (new ConfigurationException(msg)));
                log.info(msg);
            }
            else {
                MasterSlaveChecker checker = new MasterSlaveChecker(masterRW, slaveRW);
                checkers.add(checker);
                log.trace("Added checker #" + (checkers.size()) + ": " + checker.toDebugString());
            }
        }
    }

    private boolean exceptionsChecked = false;
    public List<ExceptionReport> getSetupExceptions() {
        exceptionsChecked = true;
        return Collections.unmodifiableList(setupExceptions);
    }

    private void checkExceptionsChecked() {
        if (!exceptionsChecked)
            throw new IllegalStateException("call getSetupExceptions first");
    }

    public int getCheckCount() { return checkers.size(); }
    public MasterSlaveChecker getChecker(int index) { return checkers.get(index); }

    public String describe(MasterSlaveChecker checker) {
        return checker.getMaster().getConfig().getName() + " - " + checker.getSlave().getConfig().getName();
    }

    /** Run a check of a master-slave pair. Return the number of milliseconds replication took,
     *  rounded up to about the nearest 100 ms because of polling. */
    public long check(MasterSlaveChecker checker) throws SQLException, ReplicationFailedException {
        checkExceptionsChecked();
        return checker.checkReplication(describe(checker));
    }

    public long checkAll() throws SQLException, ReplicationFailedException {
        checkExceptionsChecked();
        long result = 0;
        for (MasterSlaveChecker checker : checkers)
            result += check(checker);
        return result;
    }

    private List<Connection> openConnections = new LinkedList<Connection>();

    private DbReadWriter createRW(DbServerConfig dbConfig) throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl(dbConfig.getUrl());
        ds.setUser(dbConfig.getUsername());
        ds.setPassword(dbConfig.getPassword());
        Connection conn = ds.getConnection();
        openConnections.add(conn);
        return new DbReadWriter(conn, dbConfig);
    }

    public void close() {
        for (Iterator<Connection> i = openConnections.iterator(); i.hasNext();) {
            Connection conn =  i.next();
            try { conn.close(); }
            catch(SQLException ignored) {}
            i.remove();
        }
    }
}
