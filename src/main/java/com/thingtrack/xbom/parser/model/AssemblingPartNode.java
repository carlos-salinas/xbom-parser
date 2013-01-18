package com.thingtrack.xbom.parser.model;

/*
 * #%L
 * X-Bom Parser
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

import java.util.ArrayList;
import java.util.List;

public class AssemblingPartNode {

	private int level;
	private String partName;
	private double quantity;
	private String unit;
	private String partReferenceNumber;
	private String version;
	private AssemblingPartNode parent;
	private List<AssemblingPartNode> children = new ArrayList<AssemblingPartNode>() ;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getPartReferenceNumber() {
		return partReferenceNumber;
	}

	public void setPartReferenceNumber(String partReferenceNumber) {
		this.partReferenceNumber = partReferenceNumber;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public AssemblingPartNode getParent() {
		return parent;
	}

	public void setParent(AssemblingPartNode parent) {
		this.parent = parent;
	}

	public List<AssemblingPartNode> getChildren() {
		return children;
	}

	public void setChildren(List<AssemblingPartNode> children) {
		this.children = children;
	}
}
