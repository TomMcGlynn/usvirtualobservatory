package net.ivoa.voview.tests;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestResult;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;

public class FilterTests extends SeleneseTestCase {
	protected int NumResultRows;
	protected int FirstIdSorted;
	protected int FilterCountParam;
	protected int TotalCountParam;

	protected void FilterTestProcedure() throws Exception {
		String FilteredText;
		StringReader is;
		
		SAXParserFactory sp_fac = SAXParserFactory.newInstance();
		// sp_fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
		// false);
		SAXParser sp = sp_fac.newSAXParser();

		selenium.open("/voview_devel_vao/vo/develop/view/filter_test.html");

		// Test default settings
		
		submitAjaxWait();

		FilteredText = selenium.getText("id=output");
		is = new StringReader(FilteredText);
		sp.parse(new InputSource(is), new VotableParseCallBack());
		is.close();

		selenium.setContext("Number of Returned Rows is " + NumResultRows);
		assertTrue(NumResultRows == 10);

		selenium.setContext("vovid of first row is " + FirstIdSorted);
		assertTrue(FirstIdSorted == 8);
		
		// Sort with a different column
		
		selenium.open("/voview_devel_vao/vo/develop/view/filter_test.html");

		selenium.type("sortcol", "ra");
		selenium.select("sortdir", "label=descending");
		
		submitAjaxWait();
		
		FilteredText = selenium.getText("id=output");
		is = new StringReader(FilteredText);
		sp.parse(new InputSource(is), new VotableParseCallBack());
		is.close();

		selenium.setContext("vovid of first row is " + FirstIdSorted);
		assertTrue(FirstIdSorted == 528);
		
		// Use filter_test2 to check column filtering expressions
		// Do less than ...
		
		selenium.open("/voview_devel_vao/vo/develop/view/filter_test2.html");
		selenium.type("filter_name", "7");
		selenium.type("col_exp", "<0");

		submitAjaxWait();

		FilteredText = selenium.getText("id=output");
		is = new StringReader(FilteredText);
		sp.parse(new InputSource(is), new VotableParseCallBack());
		is.close();

		selenium.setContext("vovid of first row is " + FirstIdSorted);
		assertTrue(FirstIdSorted == 8);
		
		selenium.setContext("Number of rows after filtering is " + FilterCountParam);
		assertTrue(FilterCountParam == 350);
		
		// Greater than with column name instead of number
		
		selenium.open("/voview_devel_vao/vo/develop/view/filter_test2.html");
		selenium.type("filter_name", "hardness_ratio_1");
		selenium.type("col_exp", ">0");

		submitAjaxWait();

		FilteredText = selenium.getText("id=output");
		is = new StringReader(FilteredText);
		sp.parse(new InputSource(is), new VotableParseCallBack());
		is.close();

		selenium.setContext("vovid of first row is " + FirstIdSorted);
		assertTrue(FirstIdSorted == 260);
		
		selenium.setContext("Number of rows after filtering is " + FilterCountParam);
		assertTrue(FilterCountParam == 287);
		
		// Column expression using equality
		
		selenium.open("/voview_devel_vao/vo/develop/view/filter_test2.html");
		selenium.type("filter_name", "hardness_ratio_1");
		selenium.type("col_exp", "=1");

		submitAjaxWait();

		FilteredText = selenium.getText("id=output");
		is = new StringReader(FilteredText);
		sp.parse(new InputSource(is), new VotableParseCallBack());
		is.close();

		selenium.setContext("vovid of first row is " + FirstIdSorted);
		assertTrue(FirstIdSorted == 26);
		
		selenium.setContext("Number of rows after filtering is " + FilterCountParam);
		assertTrue(FilterCountParam == 58);
		
	}

	private void submitAjaxWait() {
		selenium.click(".submit");
		new Wait() {
			public boolean until() {
				return selenium.isTextPresent("VOTABLE");
			}
		}.wait("VOTABLE did not appear.");
	}

	private class VotableParseCallBack extends DefaultHandler {
		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {

			if (qName.equals("RESOURCE")) {
				NumResultRows = 0;
				FirstIdSorted = -1;
				FilterCountParam = 0;
				TotalCountParam = 0;
			}
			
			if (qName.equals("PARAM")) {
				if (attrib.getValue("ID")!=null && attrib.getValue("ID").equals("VOV:TotalCount")) {
					TotalCountParam = Integer.parseInt( attrib.getValue("value") );
				}
				if (attrib.getValue("ID")!=null && attrib.getValue("ID").equals("VOV:FilterCount")) {
					FilterCountParam = Integer.parseInt( attrib.getValue("value") );
				}				
			}

			if (qName.equals("TR")) {
				NumResultRows += 1;
				if (FirstIdSorted<0) {
					FirstIdSorted = Integer.parseInt( attrib.getValue("vovid") );
				}
			}
		}
	}
}
