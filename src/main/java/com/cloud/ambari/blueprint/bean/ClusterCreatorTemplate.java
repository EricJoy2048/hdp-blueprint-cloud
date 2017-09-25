package com.cloud.ambari.blueprint.bean;

import java.util.List;
import java.util.Map;

/**
 * <li>模板对象，该对象用于定义创建集群的json模板。
 * @author BFD_491
 *
 */
public class ClusterCreatorTemplate {

	//集群定义blueprint的名称，这里为"bdos1.3-hdp2.4-blueprint"
	private String blueprint;
	
	private String default_password = "admin";

	private List<Map<String, Map<String, Object>>> configurations;
	
	private List<HostGroup> host_groups;
	
	private String config_recommendation_strategy = "ALWAYS_APPLY_DONT_OVERRIDE_CUSTOM_VALUES";
	
	
	public List<Map<String, Map<String, Object>>> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(
			List<Map<String, Map<String, Object>>> configurations) {
		this.configurations = configurations;
	}

	public String getBlueprint() {
		return blueprint;
	}

	public void setBlueprint(String blueprint) {
		this.blueprint = blueprint;
	}

	public String getDefault_password() {
		return default_password;
	}

	public void setDefault_password(String default_password) {
		this.default_password = default_password;
	}


	public List<HostGroup> getHost_groups() {
		return host_groups;
	}

	public void setHost_groups(List<HostGroup> host_groups) {
		this.host_groups = host_groups;
	}

	public String getConfig_recommendation_strategy() {
		return config_recommendation_strategy;
	}

	public void setConfig_recommendation_strategy(
			String config_recommendation_strategy) {
		this.config_recommendation_strategy = config_recommendation_strategy;
	}


	
	
}
