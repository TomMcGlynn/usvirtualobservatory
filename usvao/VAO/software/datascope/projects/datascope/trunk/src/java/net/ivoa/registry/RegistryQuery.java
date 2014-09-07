package net.ivoa.registry;

import java.util.List;
import java.util.Map;

import java.io.InputStream;
import java.net.URL;

import uk.ac.starlink.table.RowListStarTable;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.votable.VOTableBuilder;

/** Query the JHU Registry using the GET protocols. */
public class RegistryQuery {
	/**
	 * Query the registry at the given URLs and simplify the results.
	 * 
	 * @param urls
	 *            An array of URLs to query the registry with. These are full
	 *            URLs and expected to use the JHU GET port to the registry. The
	 *            results from all of the queries will be concatenated.
	 * @param xsls
	 *            An array of file names for XSL files that are to be used to
	 *            filter the XML returned by the URLs. 
	 * @return A Map where the keys are the IVORNs of the resources and the
	 *         values are a map of the metadata for that resource. Each of these
	 *         resource maps has a key which is the key of the metadata field
	 *         and a value which is a list of String values.
	 */
	public static Map<String, Map<String, List<String>>> query(List<String> urls,
			List<String> xsls) throws Exception {

		Map<String, Map<String, List<String>>> metadata = null;

		for (int i = 0; i < urls.size(); i += 1) {

			URL RegURL = new URL(urls.get(i));
			InputStream is = RegURL.openStream();
			// Set up the parser to interpret the data.
			MetaParser mp;

			if (metadata == null) {
				mp = new MetaParser();
			} else {
				mp = new MetaParser(metadata);
			}
			metadata = mp.extract(is);
		}

		return metadata;
	}
	
	public static RowListStarTable queryTable(List<String> urls) throws Exception {

		StarTableFactory starTableFactory = new StarTableFactory();
		StarTable[] tableSet = new StarTable[ urls.size() ];
		for (int i = 0; i < urls.size(); i += 1) {
			URL RegURL = new URL(urls.get(i));
			InputStream is = RegURL.openStream();
			tableSet[i] = starTableFactory.makeStarTable( is, new VOTableBuilder() );
		}
		
		RowListStarTable randomTable = new RowListStarTable(tableSet[0]);
		for( StarTable table : tableSet ){
			RowSequence combinedSeq = table.getRowSequence();
			while (combinedSeq.next()) {
				randomTable.addRow( combinedSeq.getRow() );
			}
		}
		
        return randomTable;
	}
}
