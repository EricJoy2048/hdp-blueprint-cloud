package com.cloud.ambari.blueprint.bean;

/**
 * 集群安全认证类型
 * @author BFD_491
 *
 */
public enum SecurityType {
	NONE("NONE") , KERBEROS("KERBEROS");
	
	private String name;
	
	private SecurityType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
