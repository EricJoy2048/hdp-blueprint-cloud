package com.bfd.bdos.bdos.cloud.ambari.blueprint.bean;


/**
 * 定义集群安全模块的模板
 * @author BFD_491
 *
 */
public class Security{
	
	//集群的安全认证方式，只有"NONE"和"KERBEROS"选择
	private SecurityType type;

	public SecurityType getType() {
		return type;
	}

	public void setType(SecurityType type) {
		this.type = type;
	}
	
	
}
