
package com.thingtrack.xbom.parser;

/*
 * #%L
 * X-Bom Parser
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 Thingtrack S.L.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

import com.thingtrack.xbom.parser.model.AssemblingPartNode;
import com.thingtrack.xbom.parser.model.NoAssemblingPartException;
import com.thingtrack.xbom.parser.model.XbomParsingException;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * Demonstration application that shows how to use a simple custom client-side
 * GWT component, the ColorPicker.
 */
@SuppressWarnings("serial")
public class XBomParserApplication extends com.vaadin.Application {

	public static final String XBOM_SHEET_CAPTION = "XBOM";
	public static final int XBOM_PART_NAME_COLUMN_INDEX = 6;
	public static final int XBOM_PART_QUANTITY_COLUMN_INDEX = 7;
	public static final int XBOM_PART_UNIT_COLUMN_INDEX = 8;
	public static final int XBOM_PART_REFERENCE_NUM_COLUMN_INDEX = 9;
	public static final int XBOM_PART_VERSION_COLUMN_INDEX = 10;

	public List<AssemblingPartNode> assemblingPartNodes = new ArrayList<AssemblingPartNode>();

	private Window main = new Window("X-Bom Parser");
	private VerticalLayout mainLayout = new VerticalLayout();
	private XBomReceiver xbomReceiver = new XBomReceiver();
	private Upload upload = new Upload("Subir X-Bom", xbomReceiver);
	private Label label = new Label("X-Bom resultante");
	private TreeTable treeTable = new TreeTable();
	// Data Container
	private HierarchicalContainer treeTableContainer;

	@Override
	public void init() {

		setTheme("xbomparser");

		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();
		
		main.setContent(mainLayout);
		
		upload.addListener(xbomReceiver);
		label.setStyleName(Reindeer.LABEL_H2);
		treeTable.setSizeFull();
		
		mainLayout.addComponent(upload);
		mainLayout.addComponent(label);
		mainLayout.addComponent(treeTable);
		mainLayout.setExpandRatio(treeTable, 1.0F);
		
		treeTableContainer = new HierarchicalContainer();
		treeTableContainer.addContainerProperty("icon", Resource.class, null);
		treeTableContainer.addContainerProperty("level", Integer.class, null);
		treeTableContainer.addContainerProperty("partName", String.class, null);
		treeTableContainer.addContainerProperty("quantity", Double.class, null);
		treeTableContainer.addContainerProperty("unit", String.class, null);
		treeTableContainer.addContainerProperty("partReferenceNumber", String.class, null);
		treeTableContainer.addContainerProperty("version", String.class, null);
		
		treeTable.setContainerDataSource(treeTableContainer);
		treeTable.setItemIconPropertyId("icon");
		treeTable.setItemCaptionPropertyId("partName");
		treeTable.setVisibleColumns(new String[]{ "partName", "level", "quantity", "unit", "partReferenceNumber", "version" });
		treeTable.setColumnHeaders(new String[]{ "Part Name", "Level", "Quantity", "Unit", "Part Number", "Version" });
		treeTable.setColumnAlignment("quantity", Table.ALIGN_RIGHT);
		treeTable.setColumnAlignment("unit", Table.ALIGN_CENTER);
		treeTable.setColumnAlignment("partReferenceNumber", Table.ALIGN_CENTER);
		treeTable.setColumnAlignment("version", Table.ALIGN_CENTER);
		
			
		setMainWindow(main);

	}

	private void parseXbom(InputStream file) throws FileNotFoundException,
			IOException, XbomParsingException {

		//Clear previous importations
		assemblingPartNodes.clear();
				
		// Load XLS file
		POIFSFileSystem fs = new POIFSFileSystem(file);
		HSSFWorkbook workbook = new HSSFWorkbook(fs);
		HSSFSheet sheet = workbook.getSheet(XBOM_SHEET_CAPTION);

		// Parsing assembling parts
		for (int i = 11; i < sheet.getLastRowNum(); i++) {

			HSSFRow row = (HSSFRow) sheet.getRow(i);

			try {
				assemblingPartNodes.add(getAssemblingPart(row));
			} 
			//The assembling part parsing has ended
			catch (NoAssemblingPartException e) {
				break;
			}
		}
		
		if(assemblingPartNodes.size() > 2){
			//Build tree relationship
			buildTreeRelationship(assemblingPartNodes.get(0), assemblingPartNodes.subList(1, assemblingPartNodes.size()));
			loadTreeTable(assemblingPartNodes.get(0));
		}
		
		assemblingPartNodes.size();
	}

	private AssemblingPartNode getAssemblingPart(HSSFRow row)
			throws XbomParsingException, NoAssemblingPartException {

		AssemblingPartNode assemblingPartNode = new AssemblingPartNode();

		try {

			Integer level = null;
			// Assembling part level
			for (int i = 0; i < 6; i++) {

				HSSFCell cell = row.getCell(i);

				if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
					continue;

				level = (int) cell.getNumericCellValue();
				break;
			}
			
			
			if(level == null)
				throw new NoAssemblingPartException();
			
			assemblingPartNode.setLevel(level);
			assemblingPartNode.setPartName(row.getCell(
					XBOM_PART_NAME_COLUMN_INDEX).getStringCellValue());
			assemblingPartNode.setQuantity(row.getCell(
					XBOM_PART_QUANTITY_COLUMN_INDEX).getNumericCellValue());
			assemblingPartNode.setUnit(row.getCell(XBOM_PART_UNIT_COLUMN_INDEX)
					.getStringCellValue());
			assemblingPartNode.setPartReferenceNumber(row.getCell(
					XBOM_PART_REFERENCE_NUM_COLUMN_INDEX).getStringCellValue());
			assemblingPartNode.setVersion(row.getCell(
					XBOM_PART_VERSION_COLUMN_INDEX).getStringCellValue());

		} catch (RuntimeException e) {
			throw new XbomParsingException(e);
		}

		return assemblingPartNode;

	}
	
	
	private void buildTreeRelationship(AssemblingPartNode root, List<AssemblingPartNode> nodes){
		
		for(AssemblingPartNode node : nodes){
			if(node.getLevel() - root.getLevel() == 1){
				root.getChildren().add(node);
				node.setParent(root);
				
				buildTreeRelationship(node, nodes.subList(nodes.indexOf(node), nodes.size()));
			}
		}
	}
	
	
	private void loadTreeTable(AssemblingPartNode root){
		
		//clean container
		treeTableContainer.removeAllItems();
		
		treeTableContainer.addItem(root);
		treeTableContainer.getContainerProperty(root, "icon").setValue(new ThemeResource("icons/block.png"));
		treeTableContainer.getContainerProperty(root, "partName").setValue(root.getPartName());
		treeTableContainer.getContainerProperty(root, "level").setValue(root.getLevel());
		treeTableContainer.getContainerProperty(root, "partReferenceNumber").setValue(root.getPartReferenceNumber());
		treeTableContainer.getContainerProperty(root, "version").setValue(root.getVersion());
		
		//Add children
		addAssemblingPartsToContainer(root);
	}
	
	
	private void addAssemblingPartsToContainer(AssemblingPartNode root){
		
		for(AssemblingPartNode child : root.getChildren())
		{
			treeTableContainer.addItem(child);
			
			treeTableContainer.getContainerProperty(child, "partName").setValue(child.getPartName());
			treeTableContainer.getContainerProperty(child, "level").setValue(child.getLevel());
			treeTableContainer.getContainerProperty(child, "quantity").setValue(child.getQuantity());
			treeTableContainer.getContainerProperty(child, "unit").setValue(child.getUnit());
			treeTableContainer.getContainerProperty(child, "partReferenceNumber").setValue(child.getPartReferenceNumber());
			treeTableContainer.getContainerProperty(child, "version").setValue(child.getVersion());
			
			treeTableContainer.setParent(child, root);
			
			if(child.getChildren().isEmpty()){
				treeTableContainer.setChildrenAllowed(child, false);
			}
			else{
				treeTableContainer.getContainerProperty(child, "icon").setValue(new ThemeResource("icons/block.png"));
				addAssemblingPartsToContainer(child);
			}
		}
	}

	private class XBomReceiver implements Receiver, SucceededListener {

		private File file;
		private FileOutputStream fos;
		private String fileName;
		private String mtype;

		public OutputStream receiveUpload(String filename, String mimeType) {

			this.fileName = filename;
			this.mtype = mimeType;

			// Create upload stream
			FileOutputStream fos = null; // Output stream to write to
			try {
				// Open the file for writing.
				file = new File("/Users/carlos/Desarrollo/" + filename);
				fos = new FileOutputStream(file);
			} catch (final java.io.FileNotFoundException e) {

				getMainWindow().showNotification("Could not open file<br/>",
						e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
				return null;
			}
			return fos; // Return the output stream to write to

		}

		public void uploadSucceeded(SucceededEvent event) {

			getMainWindow().showNotification("X-Bom File uploaded!");

			try {
				parseXbom(new FileInputStream(file));				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
