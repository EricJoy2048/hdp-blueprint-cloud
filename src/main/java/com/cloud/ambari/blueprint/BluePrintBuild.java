package com.cloud.ambari.blueprint;

public class BluePrintBuild {
	
	private String xxxVersion;
	
	private String blueprintName;
	
	private String blueprintFileName;
	
	private String ambariServerAddress;
	
	private String blueprintRegistAddress;
	
	private String clusterName;
	
	private String createClusterAddress;
	
	private String repoAddress;
	
	private String repoFile;
	
	private String mysqlRootPass;
	
	
	
	public BluePrintBuild(String xxxVersion 
			, String ambariServerAddress 
			, String clusterName 
			, String repoAddress 
			, String repoFile
			, String mysqlRootPass){
		
		this.xxxVersion = xxxVersion;
		this.clusterName = clusterName;
		this.ambariServerAddress = ambariServerAddress;
		this.blueprintName = (this.xxxVersion + "-blueprint").toLowerCase();
		this.blueprintFileName = this.blueprintName + ".json";
		this.blueprintRegistAddress = this.ambariServerAddress + "/api/v1/blueprints/" + this.blueprintName.toLowerCase();
		this.createClusterAddress = this.ambariServerAddress + "/api/v1/clusters/" + this.clusterName;
		this.repoAddress = repoAddress;
		this.repoFile = repoFile;
		this.mysqlRootPass = mysqlRootPass;
	}





	public String getXxxVersion() {
		return xxxVersion;
	}





	public void setXxxVersion(String xxxVersion) {
		this.xxxVersion = xxxVersion;
	}





	public String getBlueprintName() {
		return blueprintName;
	}


	public void setBlueprintName(String blueprintName) {
		this.blueprintName = blueprintName;
	}


	public String getAmbariServerAddress() {
		return ambariServerAddress;
	}


	public void setAmbariServerAddress(String ambariServerAddress) {
		this.ambariServerAddress = ambariServerAddress;
	}


	public String getBlueprintRegistAddress() {
		return blueprintRegistAddress;
	}


	public void setBlueprintRegistAddress(String blueprintRegistAddress) {
		this.blueprintRegistAddress = blueprintRegistAddress;
	}


	public String getClusterName() {
		return clusterName;
	}


	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}


	public String getCreateClusterAddress() {
		return createClusterAddress;
	}


	public void setCreateClusterAddress(String createClusterAddress) {
		this.createClusterAddress = createClusterAddress;
	}


	public String getBlueprintFileName() {
		return blueprintFileName;
	}


	public void setBlueprintFileName(String blueprintFileName) {
		this.blueprintFileName = blueprintFileName;
	}


	public String getRepoAddress() {
		return repoAddress;
	}


	public void setRepoAddress(String repoAddress) {
		this.repoAddress = repoAddress;
	}


	public String getRepoFile() {
		return repoFile;
	}


	public void setRepoFile(String repoFile) {
		this.repoFile = repoFile;
	}


	public String getMysqlRootPass() {
		return mysqlRootPass;
	}


	public void setMysqlRootPass(String mysqlRootPass) {
		this.mysqlRootPass = mysqlRootPass;
	}
	
	
	
	
}
