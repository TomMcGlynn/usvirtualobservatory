package net.ivoa.datascope;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
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

import net.ivoa.util.Settings;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;

import uk.ac.starlink.table.ArrayColumn;
import uk.ac.starlink.table.ColumnPermutedStarTable;
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
	private StarTable statusIdTable;
	private BitSet positiveHits;
	private BitSet priorities;
	
	private RowSubsetStarTable priorityStatusTable, priorityResultsStatusTable, priorityHitsStatusTable;
		
	private List<String> priorityResources;

	private static final Pattern hostPat = Pattern.compile("//([^/?]*)[/?]");
	
	private int resultsFileVersion = 0;

	public Scanner(String cache) throws Exception {

		Logger.getLogger( "uk.ac.starlink" ).setLevel( Level.WARNING );

		if (!cache.endsWith("/")) {
			cache += "/";
		}
		this.cache = cache;

		/*
		 *  Get table containing registry data
		 */		
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

		/*
		 * Make table to hold status info
		 */
		statusTable = ColumnStarTable.makeTableWithRows(resultsCount);
		statusTable.addColumn(ArrayColumn.makeColumn("hits", hits));
		statusTable.addColumn(ArrayColumn.makeColumn("fitsImages", fitsImages));
		statusTable.addColumn(ArrayColumn.makeColumn("nonFitsImages", nonFitsImages));
		statusTable.addColumn(ArrayColumn.makeColumn("tableURL", tableURL));
		statusTable.addColumn(ArrayColumn.makeColumn("serviceStatus", serviceStatus));
		statusTable.setParameter(percentCompleteValue);
		
		/*
		 * Add identifier (ivoid) column to status table
		 */
		int[] columnMap = new int[1];
		for(int icol=0; icol<resultsTable.getColumnCount(); icol++){
			if( resultsTable.getColumnInfo(icol).getName().equals("identifier") ){
				columnMap[0] = icol;
			}
		}		
		ColumnPermutedStarTable idTable = new ColumnPermutedStarTable(resultsTable, columnMap);
		statusIdTable = new JoinStarTable(new StarTable[] { idTable, statusTable });

		/*
		 * Make table with all registry and status info
		 */
		resultsStatusTable = new JoinStarTable(new StarTable[] { resultsTable, statusTable });


		/*
		 * Table that only contains rows with >0 hits
		 */
		hitsStatusTable = new RowSubsetStarTable(resultsStatusTable);
		positiveHits = new BitSet(resultsCount);
		
		
		priorities = new BitSet(resultsCount);
		priorityStatusTable = new RowSubsetStarTable(statusTable);
		priorityResultsStatusTable = new RowSubsetStarTable(resultsStatusTable);
		priorityHitsStatusTable = new RowSubsetStarTable(hitsStatusTable);
		
		if( Settings.has("services") ){
			priorityResources =  Arrays.asList( Settings.get("services").split(",") ) ;
		}
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
			String version = metaFile.getScalar("version", statusRow);
			String interfaceVersion = metaFile.getScalar("interfaceVersion", statusRow);

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
			sv.initialize(id, url, cache, sn, services.size(), ror, version, interfaceVersion);

			// Add to a host queue.
			boolean priority = false;
			String idRoot = id;
			if( id.matches(".*#.*") ){
				idRoot = id.split("#")[0];
			}
			
			// DS.log("id_root, priority service 0: " + idRoot +" "+ priorityResources.get(0));
			if( priorityResources != null && priorityResources.contains(idRoot) ){
				priority = true;
				priorities.set(statusRow);
			}
			
			Service duplicateService = q.addService(sv, priority);
			if( duplicateService != null ){
				if( duplicateService != sv ){
					int index = services.indexOf(duplicateService);
					sv.setIndex( duplicateService.getIndex() );
					services.set(index, sv);
					DS.log("Replaced service " + id + " in queue " + host + 
							"\n  old URL : " + duplicateService.getURL() + " new URL: " + sv.getURL());
				}
			}else{
				// Add to the list of services.
				services.add(sv);
				DS.log("Added service " + id + " to queue " + host + "\n  URL:" + sv.getURL());
			}
		}
	}

	public void scan() throws Exception {

		// First write out the metadata stem file so that
		// the JavaScript client can be happy!
		writeStem();

		//String[] statuses = new String[services.size()];
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

		int processingCount = 0;
		while (tg.activeCount() > 0) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
			}
			processingCount = 0;
			for (int i = 0; i < services.size(); i += 1) {
				String status = services.get(i).getStatus();
				// DS.log("Status " + services.get(i).getID() +": " + status);
				if (status == null || status.equals("PENDING") || status.equals("EXECUTING") ) {
					processingCount += 1;
				}
			}
			writeStat();

			DS.log("Currently: Threads:" + tg.activeCount() + " Services:"
					+ processingCount);

			if (processingCount == 0) {
				DS.log("No remaining services. Processing terminates");
				break;
			}
		}

		if(processingCount>0){
			cleanupErrors("Queue thread died unexpectedly.");
		}
	}


	public void cleanupErrors(String errorMessage) {
		for (Service s: services) {
			String status = s.getStatus();
			if( status.equals("PENDING")  || status.equals("EXECUTING") ){
				s.setError();
				s.setMessage("-1|" + errorMessage);
			}
		}				
		writeStat();
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
			writeTable("status", statusIdTable);
			
			hitsStatusTable.setMask(positiveHits);
			writeTable("hits", hitsStatusTable);
			
			if( Settings.has("resources") ){
				priorityStatusTable.setMask(priorities);
				priorityResultsStatusTable.setMask(priorities);
				priorityHitsStatusTable.setMask(priorities);
				
				writeTable("priority_results", priorityResultsStatusTable);			
				writeTable("priority_status", priorityStatusTable);
				writeTable("priority_hits", priorityHitsStatusTable);
			}

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
		
		new StarTableOutput().writeStarTable(starTable, tableFileName + ".tmp", "votable");
		
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
