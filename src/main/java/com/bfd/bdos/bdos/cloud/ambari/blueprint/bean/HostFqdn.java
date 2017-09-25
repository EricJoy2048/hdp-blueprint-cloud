package com.bfd.bdos.bdos.cloud.ambari.blueprint.bean;

 /**
 * 主机对象模板
 * @author BFD_491
 *
 */
public class HostFqdn{
	
	private String fqdn;
	
	public HostFqdn(String hostname){
		this.fqdn = hostname;
	}

	public String getFqdn() {
		return fqdn;
	}

	public void setFqdn(String fqdn) {
		this.fqdn = fqdn;
	}
	
	
}