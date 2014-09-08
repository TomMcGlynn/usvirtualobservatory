package net.ivoa.voview.tests;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;

public class RenderTests extends SeleneseTestCase {

	protected void RenderTestProcedure() throws Exception {
		String FilteredText;

		// Load the page and submit the form.

		// selenium.setSpeed("1000");

		selenium.open("/voview_vao/vo/develop/view/voview_test2.html");
//		selenium.open("/vo/view/voview_test2.html");

		selenium.type("sortcol", "hardness_ratio_1");
		selenium.select("sortdir", "label=ascending");

		selenium.click(".submit");
		ajaxWait();

		// Check table size and page size.

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 1-10 of 641"));

		// Check first column of main table and column sorting table.

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/tbody/tr[1][@id='vov_8']/td[1]");
		selenium.setContext("First table row, column value: " + FilteredText);
		assertTrue(FilteredText.contains("56143"));

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.columnArranging']/table[@id='voview_column_fields']/tbody/tr[1]/td[1]");
		selenium.setContext("Name of first row " + FilteredText);
		assertTrue(FilteredText.contains("unique_id"));

		// Check last column displayed

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/thead/tr[1]/th[last()-2]");
		selenium.setContext("Name of last row " + FilteredText);
		assertTrue(FilteredText.contains("date_included"));

		// Test sorting by RA, both ascending and descending.

		selenium.click("ra_3_top");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/tbody/tr[1][@id='vov_280']/td[1]");
		selenium.setContext("First table row, column value: " + FilteredText);
		assertTrue(FilteredText.contains("47847"));

		selenium.click("ra_3_top");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/tbody/tr[1][@id='vov_528']/td[1]");
		selenium.setContext("First table row, column value: " + FilteredText);
		assertTrue(FilteredText.contains("49926"));

		// Test table paging.

		selenium.click("//div[@id='voviewTab.paging.top']/div[@class='pagebuttons']/a[span='Next']/span");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 11-20 of 641"));

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/tbody/tr[last()][@id='vov_519']/td[1]");
		selenium.setContext("First table row, column value: " + FilteredText);
		assertTrue(FilteredText.contains("49366"));

		selenium.click("//div[@id='voviewTab.paging.top']/div[@class='pagebuttons']/a[span='65']/span");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 641-641 of 641"));

		selenium.click("//div[@id='voviewTab.paging.top']/div[@class='pagebuttons']/a[span='1']/span");
		ajaxWait();
		selenium.select(
				"xpath=//div[@id='voviewTab.paging.top']/div[@class='pageLengthControl']/select",
				"label=50");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 1-50 of 641"));

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/tbody/tr[last()][@id='vov_516']/td[1]");
		selenium.setContext("First table row, column value: " + FilteredText);
		assertTrue(FilteredText.contains("43923"));

		// Test column filtering

		selenium.click("vovfilter7");
		selenium.type("vovfilter7", ">0");
		selenium.click("//div[@id='voviewTab.filterButtons']/span[text()='Apply Filter']");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 1-50 of 287"));

		selenium.click("vovfilter3");
		selenium.type("vovfilter3", "12:0:0 .. 13:0:0");
		selenium.click("//div[@id='voviewTab.filterButtons']/span[text()='Apply Filter']");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 1-50 of 233"));

		selenium.click("//div[@id='voviewTab.filterButtons']/span[text()='Clear Filter']");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.paging.top']/div[@class='pagelabel']");
		selenium.setContext("pagelabel is " + FilteredText);
		assertTrue(FilteredText.contains("Results 1-50 of 641"));

		// Test column arranging

		selenium.dragAndDropToObject(
				"xpath=//div[@id='voviewTab.columnArranging']/table/tbody/tr/td[text()='Columns below are hidden - Drag to change']",
				"xpath=//div[@id='voviewTab.columnArranging']/table/tbody/tr[@id='fieldrow_3']");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/thead/tr[1]/th[last()-2]");
		selenium.setContext("Name of last row " + FilteredText);
		assertTrue(FilteredText.contains("ra"));

		selenium.dragAndDropToObject(
				"xpath=//div[@id='voviewTab.columnArranging']/table/tbody/tr/td[text()='bii']",
				"xpath=//div[@id='voviewTab.columnArranging']/table/thead/tr[1]");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/thead/tr[1]/th[1]");
		selenium.setContext("Name of last row " + FilteredText);
		assertTrue(FilteredText.contains("bii"));

		selenium.click("//div[@id='voviewTab.columnArranging']/span[@title='Restore original column order']");
		ajaxWait();

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.columnArranging']/table[@id='voview_column_fields']/tbody/tr[1]/td[1]");
		selenium.setContext("Name of first row " + FilteredText);
		assertTrue(FilteredText.contains("unique_id"));

		FilteredText = selenium
				.getText("xpath=//div[@id='voviewTab.table']/form/table/thead/tr[1]/th[last()-2]");
		selenium.setContext("Name of last row " + FilteredText);
		assertTrue(FilteredText.contains("date_included"));
	}

	private void ajaxWait() throws Exception {
		new Wait() {
			public boolean until() {
				return selenium.isTextPresent("ROSAT");
			}
		}.wait("VOTABLE did not appear.");
	}
}
