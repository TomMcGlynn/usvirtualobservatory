package edu.jhu.pha.vospace.process;

import java.util.ArrayList;
import org.apache.tika.metadata.Metadata;
import org.codehaus.jackson.JsonNode;

import edu.jhu.pha.vospace.process.database.Database;
import edu.jhu.pha.vospace.process.database.MyDB;
import edu.jhu.pha.vospace.process.sax.AsciiTable;
import edu.jhu.pha.vospace.process.sax.AsciiTableContentHandler;

public class FileToDatabaseProcessor {

	public static void processNodeMeta(Metadata metadata, Object handler, JsonNode credentials) throws Exception {
		Database db = new MyDB(credentials);
		db.setup();
		db.update(metadata, (ArrayList<AsciiTable>)((AsciiTableContentHandler)handler).getTables());
  	}
}
