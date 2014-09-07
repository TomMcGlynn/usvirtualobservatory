package net.ivoa.datascope;

import java.util.List;
import java.util.Map;

import java.io.ObjectInputStream;
import java.io.FileInputStream;

/** Dump the current metadata file. */
public class MetaDump {

	public static void main(String[] args) throws Exception {

		ObjectInputStream is = new ObjectInputStream(new FileInputStream(DS
				.getDataHome()
				+ DS.getMetadataFile()));
		Map<String, Map<String, List<String>>> meta = (Map<String, Map<String, List<String>>>) is
				.readObject();
		dump(meta);
	}

	public static void dump(Map<String, Map<String, List<String>>> meta) {
		for (String id : meta.keySet()) {

			Map<String, List<String>> result = meta.get(id);

			if (result.get("shortName") != null) {
				System.out.println("\n*** " + result.get("shortName").get(0)
						+ " ***\n");
			} else {
				System.out.println("\n*** NO SHORTNAME ***\n");
			}

			printLine("title", result);
			printLine("shortName", result);
			printLine("description", result);
			printLine("identifier", result);
			String[] keys = result.keySet().toArray(new String[0]);
			java.util.Arrays.sort(keys);

			for (String key : keys) {
				if (!key.equals("title") && !key.equals("shortName")
						&& !key.equals("description")
						&& !key.equals("identifier")) {
					printLine(key, result);
				}
			}
		}
	}

	private static void printLine(String key, Map<String, List<String>> resource) {

		List<String> values = resource.get(key);
		System.out.println("   " + key);
		if (values != null) {
			for (int i = 0; i < values.size(); i += 1) {
				System.out.println("      " + values.get(i));
			}
		}
	}
}
