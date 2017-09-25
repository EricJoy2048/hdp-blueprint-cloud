package com.bfd.bdos.bdos.cloud.ambari.blueprint.bean;

/**
 * 用于监控进度
 * @author BFD_491
 *
 */
public class MonitorProgress {

	private String href;
	
	private Requests requests;
	
	
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}



	public Requests getRequests() {
		return requests;
	}

	public void setRequests(Requests requests) {
		this.requests = requests;
	}
	
	
	public static class Requests{
		
		private int id;
		
		private String status;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
		
	}
}

