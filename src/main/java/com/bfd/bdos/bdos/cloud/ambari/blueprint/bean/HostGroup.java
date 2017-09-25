package com.bfd.bdos.bdos.cloud.ambari.blueprint.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * <li>定义主机host与hostgroup之间的映射的模板
 * <li>注意：同一个host不能同时在两个hostgroup里
 * @author BFD_491
 *
 */

public class HostGroup{
	
	private String name;
	
	private List<HostFqdn> hosts = new ArrayList<HostFqdn>();
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HostFqdn> getHosts() {
		return hosts;
	}

	public void setHosts(List<HostFqdn> hosts) {
		this.hosts = hosts;
	}

	
	
	
}
