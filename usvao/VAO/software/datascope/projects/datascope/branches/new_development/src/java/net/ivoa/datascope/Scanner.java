package net.ivoa.datascope;

import java.util.BitSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;

import uk.ac.starlink.table.ArrayColumn;
import uk.ac.starlink.table.ColumnStarTable;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.JoinStarTable;
import uk.ac.starlink.table.RowListStarTable;
import uk.ac.starlink.table.RowSubsetStarTable;
import uk.ac.starlink.table.StarTableOutput;
import uk.ac.starlink.table.StarTable;

public class Scanner {

	private MetaFile metaFile;
	
	private RowListStarTable resultsTable;
	private HashMap<String, Queue> queues = new HashMap<String, Queue>();
	private ArrayList<Service> services;
	private String cache;
	private double ra, dec, size;

	private float percentComplete = 0;
	private DescribedValue percentCompleteValue;

	private int[] hits;
	private int[] fitsImages;
	private int[] nonFitsImages;
	private String[] tableURL;
	private String[] serviceStatus;

	private ColumnStarTable statusTable;
	private StarTable resultsStatusTable;
	private RowSubsetStarTable hitsStatusTable;
	private BitSet positiveHits;

	private static final Pattern hostPat = Pattern.compile("//([^/?]*)[/?]");
	
	private int resultsFileVersion = 0;

	public Scanner(String cache) throws Exception {

		Logger.getLogger( "uk.ac.starlink" ).setLevel( Level.WARNING );

		if (!cache.endsWith("/")) {
			cache += "/";
		}
		this.cache = cache;

		metaFile = new MetaFile();
		resultsTable = metaFile.getMeta();

		// Initialize status tables

		percentCompleteValue = new DescribedValue(new DefaultValueInfo(
				"percentComplete", Float.class));

		int resultsCount = metaFile.getRowCount();
		hits = new int[resultsCount];
		fitsImages = new int[resultsCount];
		nonFitsImages = new int[resultsCount];
		tableURL = new String[resultsCount];
		serviceStatus = new String[resultsCount];

		statusTable = ColumnStarTable.makeTableWithRows(resultsCount);
		statusTable.addColumn(ArrayColumn.makeColumn("hits", hits));
		statusTable.addColumn(ArrayColumn.makeColumn("fitsImages", fitsImages));
		statusTable.addColumn(ArrayColumn.makeColumn("nonFitsImages",
				nonFitsImages));
		statusTable.addColumn(ArrayColumn.makeColumn("tableURL", tableURL));
		statusTable.addColumn(ArrayColumn.makeColumn("serviceStatus",
				serviceStatus));
		statusTable.setParameter(percentCompleteValue);

		resultsStatusTable = new JoinStarTable(new StarTable[] { resultsTable,
				statusTable });

		/**
		ColumnStarTable tmpStatusTable = ColumnStarTable
				.makeTableWithRows(resultsCount);
		tmpStatusTable.addColumn(ArrayColumn.makeColumn("identifier",
				identifier));
		tmpStatusTable.addColumn(ArrayColumn.makeColumn("hits", hits));
		tmpStatusTable.addColumn(ArrayColumn.makeColumn("fitsImages",
				fitsImages));
		tmpStatusTable.addColumn(ArrayColumn.makeColumn("nonFitsImages",
				nonFitsImages));
		tmpStatusTable.setParameter(percentCompleteValue);
		**/

		hitsStatusTable = new RowSubsetStarTable(resultsStatusTable);
		positiveHits = new BitSet(resultsCount);
	}


	public void setup(double ra, double dec, double size) throws IOException {
		this.ra = ra;
		this.dec = dec;
		this.size = size;

		services = new ArrayList<Service>();

		 for(int statusRow=0; statusRow<metaFile.getRowCount(); statusRow++) {
			String id = metaFile.getScalar("identifier", statusRow);

			serviceStatus[statusRow] = "NOTINVOKED";
			DS.log("Processing service " + id);

			String url = metaFile.getScalar("accessURL", statusRow);
			String sn = metaFile.getScalar("shortName", statusRow);
			String rorS = metaFile.getScalar("regionOfRegard", statusRow);
			String maxRS = metaFile.getScalar("maxRadius", statusRow);
			String type = metaFile.getScalar("capabilityClass", statusRow);

			if (id == null || url == null || type == null) {
				continue;
			}	
			
			if ( sn == null || sn.matches("\\s*\\[.*") ) {
				DS.log("Bad short name " + sn);
				//if(id.contains("wfau")){
				//	int index = id.lastIndexOf("/");
				//	sn = "WFAU" + id.substring(index);
				//}else{
					continue;
				//}
			}
			sn.replaceAll("\n", "");
			sn = sn.trim();			
			
			DS.log("Short name " + sn);			
			
			double ror = DS.parseDouble(rorS, 0);
			double maxR = DS.parseDouble(maxRS, 360);
			if (maxR == 0) {
				maxR = 360;
			}
			if (maxR < size) {
				continue;
			}

			Service sv = Service.factory(type);
			if (sv == null) {
				continue;
			}

			String host = null;

			try {
				Matcher match = hostPat.matcher(url);
				match.find();
				host = match.group(1);
			} catch (Exception e) {
				// Do nothing... We'll note that host is still null.
			}
			if (host == null) {
				continue;
			}
			if (host.startsWith("heasarc")) {
				String nsn = SNFixer.fix(sn);

				if (nsn.equalsIgnoreCase("XXX")) {
					// The SNFixer indicates these aren't to
					// be displayed in datascope.
					sv.setStatus("NOTINVOKED");
					continue;
				}
				if (!sn.equals(nsn)) {
					resultsTable.setCell(metaFile.getRowIndex(id),
							metaFile.getColumnIndex("shortName"), nsn);
					sn = nsn;
				}
			}
			
			Queue q = queues.get(host);
			if (q == null) {
				q = new Queue(host, ra, dec, size);
				queues.put(host, q);
			}
			// Initialize the service
			sv.initialize(id, url, cache, sn, services.size(), ror);

			// Add to a host queue.
			DS.log("Adding service " + id + " to queue " + host + "\n  URL:"
					+ sv.getURL());
			q.addService(sv);

			// Add to the list of services.
			services.add(sv);
		}

		// Add in links from the indices to the metadata.
		// for (int i = 0; i < services.size(); i += 1) {
		// Service sv = services.get(i);
		// results.put("" + i, results.get(sv.getID()));
		// }
	}

	public void scan() throws Exception {

		// First write out the metadata stem file so that
		// the JavaScript client can be happy!
		writeStem();

		String[] statuses = new String[services.size()];
		HashMap<Thread, String> threads = new HashMap<Thread, String>();

		ThreadGroup tg = new ThreadGroup("ScanGroup");
		for (String host : queues.keySet()) {
			Thread t = new Thread(tg, queues.get(host));
			threads.put(t, host);
			t.start();
		}

		// Create a quicklook image of the region.
		Runnable rb = new Runnable() {
			public void run() {
				DSSImg.gen(ra, dec, size, cache, "DssImg");
			}
		};
		Thread t = new Thread(tg, rb);
		threads.put(t, "skyview");
		t.start();

		// Now that the requests are started we save the metadata.
		try {
			metaFile.writeTable();
		} catch (Exception e) {
			DS.log("Error saving metadata:" + e);
		}

		// This will kill everything after a time...
		Runnable killer = new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000000);
				} catch (InterruptedException e) {
				}
				System.exit(0);
			}
		};
		new Thread(killer).start();

		while (tg.activeCount() > 0) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
			}
			int nullCount = 0;
			for (int i = 0; i < statuses.length; i += 1) {
				if (statuses[i] == null) {
					statuses[i] = services.get(i).getMessage();
					if (statuses[i] == null) {
						nullCount += 1;
					}
				}
			}
			writeStat();

			DS.log("Currently: Threads:" + tg.activeCount() + " Services:"
					+ nullCount);

			if (nullCount == 0) {
				DS.log("No remaining services. Processing terminates");
				//System.exit(0);
				break;
			}

		}
		//System.exit(0);
	}

	private void writeStem() {

		String stemname = cache + DS.getMetadataStem();
		try {
			File stem = new File(stemname + ".tmp");
			FileWriter fw = new FileWriter(stem);
			for (Service s : services) {
				int metaRow = metaFile.getRowIndex(s.getID());
				String line = s.getShortName() + '|' + metaFile.getScalar("title", metaRow)
						+ '|' + metaFile.getScalar("capabilityClass", metaRow) + '|'
						+ metaFile.getScalar("publisher", metaRow) + '|' + metaFile.getArr("type", metaRow) + '|'
						+ metaFile.getArr("subject", metaRow) + '|' + metaFile.getArr("waveband", metaRow) + '|'
						+ metaFile.getScalar("identifier", metaRow) + "|" + metaFile.getScalar("facility", metaRow)
						+ '\n';
				fw.write(line);
			}
			fw.close();
			File rstem = new File(stemname);
			stem.renameTo(rstem);

			String statname = cache + DS.getStatusFile();
			File stat = new File(statname + ".tmp");
			FileOutputStream fo = new FileOutputStream(stat);
			byte[] buf = new byte[services.size()];
			for (int i = 0; i < buf.length; i += 1) {
				buf[i] = '\n';
			}
			fo.write(buf);
			fo.close();
			File rstat = new File(statname);
			stat.renameTo(rstat);
		} catch (Exception e) {
			// This pretty much kills us... We'll write an error. Maybe can do
			// something with that...
			DS.log("Error writing basic files:" + e);
		}
	}

	private void writeStat() {
		try {
			String statname = cache + DS.getStatusFile();
			File stat = new File(statname + ".tmp");
			FileWriter fw = new FileWriter(stat);

			Integer doneServices = 0;
			Integer serviceCount = services.size();
			for (int i = 0; i < serviceCount; i += 1) {
				Service service = services.get(i);
				String msg = service.getMessage();
				if (msg == null) {
					fw.write("\n");
				} else {
					fw.write(msg + "\n");
				}

				int statusRow = metaFile.getRowIndex(service.getID());
				tableURL[statusRow] = "http://" + DS.getHost()
						+ DS.homeToBase(service.getResultFilename());
				
				serviceStatus[statusRow] = service.getStatus();
				if( serviceStatus[statusRow].matches("^(COMPLETED|FILTERED|ERROR)$") ){
					doneServices++;
				}

				int[] serviceHits = service.getHits();
				if (serviceHits != null) {
					if (serviceHits.length > 0) {
						hits[statusRow] = serviceHits[0];
						if (serviceHits[0] > 0) {
							positiveHits.set(statusRow);
						}

						if( service.getClass().getName().equals("net.ivoa.datascope.SIA")){
							if (serviceHits.length > 1) {
								fitsImages[statusRow] = serviceHits[1];
							}

							if (serviceHits.length > 2) {
								nonFitsImages[statusRow] = serviceHits[2];
							}
						}
					}
				}
			}
			fw.close();
			File rstat = new File(statname);
			stat.renameTo(rstat);

			DS.log("Services, finish/total: " + doneServices + "/"
					+ serviceCount);
			percentComplete = doneServices.floatValue()
					/ serviceCount.floatValue();
			percentCompleteValue.setValue(percentComplete);

			resultsFileVersion++;

			writeTable("results", resultsStatusTable);
			
			writeTable("status", statusTable);
			
			hitsStatusTable.setMask(positiveHits);
			writeTable("hits", hitsStatusTable);

			String numberOfHitsName = cache + "numberOfHits_v" + resultsFileVersion + ".txt";
			PrintWriter out =
			    new PrintWriter(
			        new BufferedWriter(
			            new FileWriter(numberOfHitsName) ) );
			out.println(hitsStatusTable.getRowCount());
			out.close();

			if(resultsFileVersion > 3){
				File oldFile = new File(cache + "numberOfHits_v" + (resultsFileVersion-3) + ".txt");
				oldFile.delete();
			}
			
		} catch (Exception e) {
			DS.log("Error in status overwrite:" + e);
		}
	}
	
	private void writeTable(String tableName, StarTable starTable) throws Exception{
		
		String tableFileName = cache + tableName + "_v" + resultsFileVersion + ".xml";
		
		new StarTableOutput().writeStarTable(hitsStatusTable, tableFileName + ".tmp", "votable");
		
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		DefaultExecutor executor = new DefaultExecutor();
		CommandLine cmdLine = new CommandLine("mv");
		cmdLine.addArgument(tableFileName + ".tmp");
		cmdLine.addArgument(tableFileName);
		
		executor.execute(cmdLine, resultHandler);
		resultHandler.waitFor();
		
		int exitValue = resultHandler.getExitValue();
		if( exitValue != 0 ){
			DS.log("Error updating file " + tableFileName + ", exit value " + exitValue);
		}
		
		Exception e = resultHandler.getException();
		if( e != null ){
			DS.log("Error updating file " + tableFileName + "," + e);
		}
		
		if(resultsFileVersion > 3){
			File oldFile = new File(cache + tableName + "_v" + (resultsFileVersion-3) + ".xml");
			oldFile.delete();
		}
	}

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner("save/");
		sc.setup(10., 10., .250);
		sc.scan();
	}
}
