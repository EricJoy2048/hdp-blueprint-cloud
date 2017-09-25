package com.cloud.ambari.blueprint;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.sty.sadt.core.util.DataFormatTool;
import org.sty.sadt.core.util.FileTool;
import org.sty.sadt.core.util.HttpClientTools;

import com.cloud.ambari.blueprint.bean.ClusterCreatorTemplate;
import com.cloud.ambari.blueprint.bean.HostFqdn;
import com.cloud.ambari.blueprint.bean.HostGroup;
import com.cloud.ambari.blueprint.bean.MonitorProgress;
import com.cloud.ambari.blueprint.bean.Repository;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 使用blueprint创建集群
 * @author BFD_491
 *
 */
public class CreateCluster {

	//xxx-web ip或主机名
	private static String xxx_web_host = "xxx-web";

	private static ArrayList<String> xxx_runner_hosts = new ArrayList<String>();
	private static ArrayList<String> kafka_hosts = new ArrayList<String>();
	private static ArrayList<String> storm_hosts = new ArrayList<String>();

	public static void main(String[] args) {
		args = new String[8];
		//BD-OS版本
		//此处根据用户是否需要安装storm选择不同的版本
		//需要storm则改为"xxx1.3.1-hdp2.4.2-withStorm"
		//不需要storm则改为"xxx1.3.1-hdp2.4.2-withoutStorm"
		args[0] = "xxx1.3.1-hdp2.4.2-withstorm";
		//集群名称
		args[1] = "mycluster";
		//ambari的地址
		args[2] = "http://172.24.5.242:8080";


		args[3] = "http://172.24.5.242:9381";

		//主机列表 cluster-storm-1
		args[4] = "xxx-runner-1,xxx-web,cluster-master-1,"
				+ "cluster-master-2,cluster-master-3,"
				+ "cluster-slave-1,cluster-slave-2,cluster-slave-3,"
				+ "cluster-storm-1,cluster-storm-2";
		//		args[4] = "bgs-5p242-gaojun,bgs-5p243-gaojun,bgs-5p244-gaojun,bgs-5p245-gaojun,"
		//				+ "bgs-5p246-gaojun,bgs-5p247-gaojun,bgs-5p248-gaojun,bgs-5p249-gaojun,"
		//				+ "bgs-5p250-gaojun,bgs-5p251-gaojun";
		//		
		args[5] = "E:/workspaceScala/xxx-blueprint-cloud/src/main/resources/repository.json.example";

		args[6] = "xxx1qaz@WSX";

		//hawq ssh端口号
		args[7] = "3222";



		//BD-OS版本
		String xxxVersion = args[0];
		//集群名称
		String clusteName = args[1];
		//ambari的地址
		String ambariServerAddress = args[2];
		//repo地址
		String repoAddress = args[3];
		//主机列表
		String hosts = args[4];
		//reposity文件路径
		String repoFile = args[5];

		String mysqlRootPass = args[6];
		//hawq ssh的端口号
		String hawq_ssh_port = args[7];

		BluePrintBuild bpb = new BluePrintBuild( xxxVersion
				, ambariServerAddress 
				, clusteName 
				, repoAddress 
				, repoFile
				, mysqlRootPass);

		CreateCluster cc = new CreateCluster();

		//1.创建集群定义blueprint
		String blueprint = cc.getBluePrint(bpb);
		System.out.println("1.创建集群定义blueprint 成功");

		//2.注册集群定义blueprint到ambari
		if(!cc.registerBlueprint(blueprint , bpb)){
			System.out.println("注册blueprint失败");
			return;
		}
		System.out.println("2.注册集群定义blueprint到ambari 成功");

		//3.生成用于集群创建的描述信息json模板
		String templateJson = cc.createClusterCreationTemp(bpb , hosts, hawq_ssh_port);
		System.out.println("3.生成用于集群创建的描述信息json模板  成功");


		//4.设置集群的yum源
		if(!cc.setupStackRepo(bpb)){
			System.out.println("设置yum源失败!");
			return;
		}
		System.out.println("4.设置集群的yum源  成功");

		//5.请求ambari，创建集群
		MonitorProgress mp = null;
		Map<String, String> createReqMap = cc.createCluster(templateJson , bpb);
		if(createReqMap == null){
			System.out.println("创建集群失败！");
			return;
		}else if(! "202".equals(createReqMap.get("code"))){
			System.out.println("创建集群失败：" + createReqMap.get("rpString"));
			return;
		}else{
//			System.out.println(createReqMap.get("rpString"));
			mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(createReqMap.get("rpString").toLowerCase()), MonitorProgress.class);
		}
		System.out.println("5.集群创建请求提交成功！");

		//6.监控创建过程，获取进度信息
		if(cc.monitorCreationProgress(mp)){
			System.out.println("集群部署成功!");
		}else{
			System.out.println("集群部署成功，但有些服务启动失败，请登陆："+bpb.getAmbariServerAddress()+"检查！");
		}

		//7.执行mysql int
		System.out.println("init mysql......");
		if(cc.initMysql(bpb))
			System.out.println("init mysql 成功！,重启BD-OS服务");
		else{
			System.out.println("mysql init 失败，部署完成后需要您手动在ambari中执行mysql init 操作。mysql init操作完成后，请手动重启xxx-web机器上的所有服务。！");
			return;
		}
		//8.重启xxx集群
		System.out.println("restart xxx-web ......");
		if(cc.restartxxxServers(bpb))
			System.out.println("restart xxx-web 成功！,重启xxx-runner");
		else{
			System.out.println("restart xxx-web 失败，请在ambari中将xxx服务依次执行restart操作");
			return;
		}

		//9.重启runner
		System.out.println("restart xxx-runner ......");
		if(cc.restartRunner(bpb))
			System.out.println("restart xxx-runner 成功！");
		else{
			System.out.println("restart xxx-runner 失败，请查看日志信息/opt/xxx/xxx-ide-runner-server/logs/main.log");
			return;
		}
		
		
		//10.重启kafka
		if(!kafka_hosts.isEmpty()){
			System.out.println("restart kafka cluster ......");
			if(cc.restartKafka(bpb))
				System.out.println("restart kafka 成功！");
			else{
				System.out.println("restart kafka 失败，");
				return;
			}
		}
		
		//11.重启HAWQ
		System.out.println("restart hawq standby master ......");
		if(cc.restartHAWQStandyByMaster(bpb))
			System.out.println("restart hawq standby master 成功！");
		else{
			System.out.println("restart hawq standby master 失败，");
			return;
		}
		
		System.out.println("集群初始化完成！");
	}





	//生成集群定义的blueprint
	@SuppressWarnings("deprecation")
	private String getBluePrint(BluePrintBuild bpb){
		String result = "";
		try {
//			String url = ClassLoaderUtil.getExtendResource(bpb.getBlueprintFileName()).toString();
//		    url = url.substring(url.lastIndexOf(":")+1);		   
//			return FileTool.readFile2Str(Paths.get(url));
		    ClassLoader classloader = getClass().getClassLoader();
		    result = IOUtils.toString(classloader.getResourceAsStream(bpb.getBlueprintFileName()));
		    
		    return result;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}


	//注册生成好的blueprint到Ambari Server
	private boolean registerBlueprint(String blueprint , BluePrintBuild bpb){
		Map<String, String> maprep = null;
		try {
			maprep = HttpClientTools.sendPost(bpb.getBlueprintRegistAddress() , blueprint , "admin:admin");
		}  catch (Exception e) {
			e.printStackTrace();
		}
		if( "201".equals(maprep.get("code")) || "409".equals(maprep.get("code"))){
			return true;
		}else{
			System.out.println("注册blueprint失败：" + maprep.get("rpString"));
			return false;

		}
	}

	//生成用于创建集群的json模板
	@SuppressWarnings("unchecked")
	private String createClusterCreationTemp(BluePrintBuild bpb , String hosts, String hawq_ssh_port){
		boolean isStormNeeded = false;
		boolean isKafkaNeeded = false;
		ClusterCreatorTemplate ct = new ClusterCreatorTemplate();
		ct.setBlueprint(bpb.getBlueprintName());
		ct.setDefault_password("admin");


		String master_02 = "";
		String master_03 = "";
		String master_01 = "";
		String xxx_web = "";


		if(hosts != null && hosts.length() > 0){

			List<HostGroup> hostGroups = new ArrayList<HostGroup>();
			ct.setHost_groups(hostGroups);


			HostGroup xxxWebGroup = new HostGroup();
			xxxWebGroup.setName("xxx_web");

			HostGroup xxxRunnerGroup = new HostGroup();
			xxxRunnerGroup.setName("xxx_runner");

			//			HostGroup xxxClusterThrift = new HostGroup();
			//			xxxClusterThrift.setName("xxx_cluster_thrift");

			HostGroup clusterMaster_1 = new HostGroup();
			clusterMaster_1.setName("cluster_master_1");

			HostGroup clusterMaster_2 = new HostGroup();
			clusterMaster_2.setName("cluster_master_2");

			HostGroup clusterMaster_3 = new HostGroup();
			clusterMaster_3.setName("cluster_master_3");

			HostGroup clusterSlave = new HostGroup();
			clusterSlave.setName("slave");
			
			HostGroup clusterKafka = new HostGroup();
			if(hosts.contains("kafka")){
				clusterKafka.setName("cluster_kafka_1");
				hostGroups.add(clusterKafka);
				isKafkaNeeded = true;
			}
			
			HostGroup clusterStorm = new HostGroup();
			if(hosts.contains("storm")){
				clusterStorm.setName("cluster_storm_1");
				hostGroups.add(clusterStorm);
				isStormNeeded = true;
			}

			hostGroups.add(xxxWebGroup);
			hostGroups.add(xxxRunnerGroup);
			//			hostGroups.add(xxxClusterThrift);
			hostGroups.add(clusterMaster_1);
			hostGroups.add(clusterMaster_2);
			hostGroups.add(clusterMaster_3);
			hostGroups.add(clusterSlave);
		
			
			

			String[] arrayHost = hosts.split(",");
			for(String hostname : arrayHost){

				if(hostname == null || "".equals(hostname)){
					continue;
				}

				HostFqdn fqdn = new HostFqdn(hostname);

				if(hostname.contains("xxx-runner")){
					xxx_runner_hosts.add(hostname);
					xxxRunnerGroup.getHosts().add(fqdn);
					//					xxxRunnerGroup.getHosts().add(new HostFqdn("bgs-5p249-gaojun"));
					//				}else if(hostname.contains("xxx-cluster-thrift")){
					//					
					//					xxxClusterThrift.getHosts().add(fqdn);
				}else if(hostname.contains("xxx-web")){
					xxx_web = hostname;
					xxx_web_host = xxx_web;
					xxxWebGroup.getHosts().add(fqdn);
					//					xxxWebGroup.getHosts().add(new HostFqdn("bgs-5p248-gaojun"));
					//				}else if(hostname.contains("xxx-cluster-thrift")){
					//					
					//					xxxClusterThrift.getHosts().add(fqdn);
				}else if(hostname.contains("cluster-master-1")){
					master_01 = hostname;
					clusterMaster_1.getHosts().add(fqdn);
					//					clusterMaster_1.getHosts().add(new HostFqdn("bgs-5p242-gaojun"));
				}else if(hostname.contains("cluster-master-2")){
					master_02 = hostname;
					clusterMaster_2.getHosts().add(fqdn);
					//					clusterMaster_2.getHosts().add(new HostFqdn("bgs-5p243-gaojun"));
				}else if(hostname.contains("cluster-master-3")){
					master_03 = hostname;
					clusterMaster_3.getHosts().add(fqdn);
					//					clusterMaster_3.getHosts().add(new HostFqdn("bgs-5p244-gaojun"));
				}else if(hostname.contains("cluster-slave-1")){

					clusterSlave.getHosts().add(fqdn);
					//					clusterSlave.getHosts().add(new HostFqdn("bgs-5p245-gaojun"));
				}else if(hostname.contains("cluster-slave-2")){

					clusterSlave.getHosts().add(fqdn);
					//					clusterSlave.getHosts().add(new HostFqdn("bgs-5p246-gaojun"));
				}else if(hostname.contains("cluster-slave-3")){

					clusterSlave.getHosts().add(fqdn);
					//					clusterSlave.getHosts().add(new HostFqdn("bgs-5p247-gaojun"));
				}else if(hostname.contains("cluster-kafka") && isKafkaNeeded){
					kafka_hosts.add(hostname);
					clusterKafka.getHosts().add(fqdn);
					//					clusterKafka.getHosts().add(new HostFqdn("bgs-5p250-gaojun"));
				}else if(hostname.contains("cluster-storm") && isStormNeeded){
					storm_hosts.add(hostname);
					clusterStorm.getHosts().add(fqdn);
					//					clusterStorm.getHosts().add(new HostFqdn("bgs-5p251-gaojun"));
				}
			}
		}

		//针对HAWQ不支持HA的blueprint情况，单独设置参数

		List<Map<String, Map<String, Object>>> confList = new ArrayList<Map<String,Map<String,Object>>>();
		Map<String, Map<String, Object>> confMap = new HashedMap();
		confList.add(confMap);

		//hawq-site
		Map<String, Object> hawqSiteMap = new HashedMap();
		confMap.put("hawq-site", hawqSiteMap);
		hawqSiteMap.put("hawq_rm_yarn_address",  master_02 + ":8050");
		hawqSiteMap.put("hawq_rm_yarn_scheduler_address",  master_02 + ":8030");


		//hawq-env
		Map<String,Object> hawqEnvMap = new HashedMap();
		confMap.put("hawq-env",hawqEnvMap);
		hawqEnvMap.put("hawq_ssh_port",hawq_ssh_port);

		//hbase-site
		Map<String,Object> hbaseSiteMap = new HashedMap();
		String rootdir = "hdfs://"+ bpb.getClusterName()+"/apps/hbase/data";
		confMap.put("hbase-site",hbaseSiteMap);
		hbaseSiteMap.put("hbase.rootdir", rootdir);


		//hdfs-client
		Map<String, Object> hdfsClientMap = new HashedMap();
		confMap.put("hdfs-client", hdfsClientMap);

		hdfsClientMap.put("dfs.namenode.http-address.mycluster.nn1", master_01 + ":50070");
		hdfsClientMap.put("dfs.namenode.http-address.mycluster.nn2", master_03 + ":50070");
		hdfsClientMap.put("dfs.namenode.rpc-address.mycluster.nn1", master_01 + ":8020");
		hdfsClientMap.put("dfs.namenode.rpc-address.mycluster.nn2", master_03 + ":8020");
		hdfsClientMap.put("dfs.nameservices", "mycluster");
		hdfsClientMap.put("dfs.ha.namenodes.mycluster","nn1,nn2");

		//yarn-client
		Map<String, Object> yarnClientMap = new HashedMap();
		confMap.put("yarn-client", yarnClientMap);
		yarnClientMap.put("yarn.resourcemanager.ha", master_02+":8032,"+master_03+":8032");
		yarnClientMap.put("yarn.resourcemanager.scheduler.ha", master_02+":8030,"+master_03+":8030");

		//hive-site
		Map<String, Object> hiveSiteMap = new HashedMap();
		confMap.put("hive-site", hiveSiteMap);
		hiveSiteMap.put("javax.jdo.option.ConnectionURL", "jdbc:mysql://"+xxx_web+":3306/hive?createDatabaseIfNotExist=true&characterEncoding=UTF-8");
		hiveSiteMap.put("javax.jdo.option.ConnectionPassword", bpb.getMysqlRootPass());

		//nginx-server-env
		Map<String, Object> nginxMap = new HashedMap();
		confMap.put("nginx-server-env", nginxMap);
		nginxMap.put("nginx_domain", "azure.xxx.com");

		//mysql-master
		Map<String, Object> mysqlMap = new HashedMap();
		confMap.put("mysql-master", mysqlMap);
		mysqlMap.put("mysql_password", bpb.getMysqlRootPass());

		ct.setConfigurations(confList);

		JSONObject json = JSONObject.fromObject(ct);
		return json.toString();

	}

	//设置stack yum源地址
	@SuppressWarnings({ "unchecked", "deprecation" })
	private boolean setupStackRepo(BluePrintBuild bpb){
		String code = "500";
		String result = "";

//		if(bpb.getRepoFile() == null || "".equals(bpb.getRepoFile())){
//
//			System.out.println("请指定repo文件路径！");
//			return false;
//		}
		 
		    
//		String repoFile = bpb.getRepoFile().substring(bpb.getRepoFile().lastIndexOf(":")+1);
		JSONArray repoArray;
		try {
//			repoArray = JSONArray.fromObject(FileTool.readFile2Str(Paths.get(repoFile)));
			
			ClassLoader classloader = getClass().getClassLoader();
			result = IOUtils.toString(classloader.getResourceAsStream("repository.json.example"));
			if (result == ""){
				System.out.println("repository.json.example读取失败");
				return false;
			}
			repoArray = JSONArray.fromObject(result);
			
			List<Repository> repoList = JSONArray.toList(repoArray, Repository.class);
			if(repoList != null && repoList.size() > 0){
				for(Repository repo : repoList){
					if(repo.getVersion() != null && repo.getVersion().toUpperCase().equals(bpb.getXxxVersion().substring(0, 18).toUpperCase())){
						StringBuilder sb_hdp = new StringBuilder();
						sb_hdp.append("{\"Repositories\":{\"base_url\":\"")
						.append(bpb.getRepoAddress())
						.append(repo.getHdp())
						.append("\",\"verify_base_url\": true}}");
						StringBuilder sb_hdp_utils = new StringBuilder();
						sb_hdp_utils.append("{\"Repositories\":{\"base_url\":\"")
						.append(bpb.getRepoAddress())
						.append(repo.getHdputils())
						.append("\",\"verify_base_url\": true}}");

						code = HttpClientTools.sendPut(bpb.getAmbariServerAddress() + repo.getHrefhdp(), sb_hdp.toString(), "admin:admin").get("code");
						code += HttpClientTools.sendPut(bpb.getAmbariServerAddress() + repo.getHrefhdputils(), sb_hdp_utils.toString(), "admin:admin").get("code");
					}
				}
			}



		} catch (IOException e) {
			e.printStackTrace();
		}


		return "200200".equals(code)  ? true : false;

	}

	//提交创建集群
	private Map<String, String> createCluster(String clusterTemp , BluePrintBuild bpb){

		try {
			return HttpClientTools.sendPost(bpb.getCreateClusterAddress(), clusterTemp, "admin:admin");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//监控创建进度
	private boolean monitorCreationProgress(MonitorProgress mp){
		JSONObject requestJsonObject = null;
		String request_status = null;
		double progressPercent = 0.0;

		if(mp == null) return false;

		try {
			do {

				Thread.sleep(5000);
				Map<String,String> repMap = HttpClientTools.sendGet(mp.getHref(), null, "admin:admin");
				String requests = repMap.get("rpString") == null ? "" : repMap.get("rpString").toLowerCase();
				requestJsonObject = (JSONObject) JSONObject.fromObject(requests).get("requests");
				request_status = requestJsonObject.get("request_status").toString();
				progressPercent = DataFormatTool.objectToDouble(requestJsonObject.get("progress_percent"));
				System.out.println("进度: %"+(int)progressPercent);


			} while ((int)progressPercent < 100);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "completed".equals(request_status) ? true : false;
	}


	//执行MYSQL Init 并重启 BD-OS
	private boolean initMysql(BluePrintBuild bpb){
		String initMysqlJson = "{\"RequestInfo\":{\"context\":\"Execute initialize\",\"command\":\"initialize\"},\"Requests/resource_filters\":[{\"service_name\":\"MYSQL\",\"component_name\":\"mysql_master\",\"hosts\":\"xxx-web\"}]}";
		initMysqlJson.replaceAll("xxx-web", xxx_web_host);
		CreateCluster cc = new CreateCluster();

		String url = bpb.getAmbariServerAddress()+"/api/v1/clusters/mycluster/requests";
		try {
			Map<String,String> repMap = HttpClientTools.sendPost(url, initMysqlJson, "admin:admin");
			MonitorProgress mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(repMap.get("rpString").toLowerCase()), MonitorProgress.class);

			if(cc.monitorCreationProgress(mp))
				return true;


		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


	//重启BD-OS的所有服务
	private boolean restartxxxServers(BluePrintBuild bpb){

		String restartJson = "{\"RequestInfo\":{\"command\":\"RESTART\",\"context\":\"Restart all components on xxx-web\",\"operation_level\":{\"level\":\"HOST\",\"cluster_name\":\"mycluster\"}},\"Requests/resource_filters\":[{\"service_name\":\"xxx_AEGIS\",\"component_name\":\"xxx_AEGIS_SERVER\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_CAS\",\"component_name\":\"xxx_CAS_SERVER\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_DATAAUDIT\",\"component_name\":\"xxx_DATAAUDIT\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_DATAMANAGER\",\"component_name\":\"xxx_DATAMANAGER\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_DESKTOP\",\"component_name\":\"xxx_DESKTOP_SERVER\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_EUROPA\",\"component_name\":\"xxx_EUROPA\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_LOGAUDIT\",\"component_name\":\"xxx_LOGAUDIT\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_PROJECT\",\"component_name\":\"xxx_PROJECT\",\"hosts\":\"xxx-web\"},{\"service_name\":\"xxx_LICENSE\",\"component_name\":\"xxx_LICENSE_SERVER\",\"hosts\":\"xxx-web\"}]}";
		restartJson.replaceAll("xxx-web", xxx_web_host);
		CreateCluster cc = new CreateCluster();

		String url = bpb.getAmbariServerAddress()+"/api/v1/clusters/mycluster/requests";
		try {
			Map<String,String> repMap = HttpClientTools.sendPost(url, restartJson, "admin:admin");
			MonitorProgress mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(repMap.get("rpString").toLowerCase()), MonitorProgress.class);

			if(cc.monitorCreationProgress(mp))
				return true;


		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	//重启runner
	private boolean restartRunner(BluePrintBuild bpb){

		String restartJson = "{\"RequestInfo\":{\"context\":\"restart runner\",\"command\":\"RESTART\"},\"Requests/resource_filters\":[{\"service_name\":\"xxx_IDE_RUNNER\",\"component_name\":\"xxx_IDE_RUNNER_SERVER\",\"hosts\":\"xxx-runner-01\"}]}";
		CreateCluster cc = new CreateCluster();
		for(String runner_host : xxx_runner_hosts){
			String restartJson_tmp = restartJson.replaceAll("xxx-runner-01", runner_host);
			String url = bpb.getAmbariServerAddress()+"/api/v1/clusters/mycluster/requests";
			try {
				Map<String,String> repMap = HttpClientTools.sendPost(url, restartJson_tmp, "admin:admin");
				MonitorProgress mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(repMap.get("rpString").toLowerCase()), MonitorProgress.class);
				if(!cc.monitorCreationProgress(mp))
					return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
	}
	//重启kafka
	private boolean restartKafka(BluePrintBuild bpb){

		String restartJson = "{\"RequestInfo\":{\"context\":\"restart kafka\",\"command\":\"RESTART\"},\"Requests/resource_filters\":[{\"service_name\":\"KAFKA\",\"component_name\":\"KAFKA_BROKER\",\"hosts\":\"clsuter-kafka-01\"}]}";
		CreateCluster cc = new CreateCluster();
		for(String kafka_host : kafka_hosts){
			System.out.println(kafka_host);
			String restartJson_tmp = restartJson.replaceAll("clsuter-kafka-01", kafka_host);
			String url = bpb.getAmbariServerAddress()+"/api/v1/clusters/mycluster/requests";
			try {
				Map<String,String> repMap = HttpClientTools.sendPost(url, restartJson_tmp, "admin:admin");
				MonitorProgress mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(repMap.get("rpString").toLowerCase()), MonitorProgress.class);
				if(!cc.monitorCreationProgress(mp))
					return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
	}
	
	//重启HAWQ standyByMaster
	private boolean restartHAWQStandyByMaster(BluePrintBuild bpb){
		String restartJson = "{\"RequestInfo\":{\"context\":\"restart hawq standyByMaster\",\"command\":\"RESTART\"},\"Requests/resource_filters\":[{\"service_name\":\"HAWQ\",\"component_name\":\"HAWQSTANDBY\",\"hosts\":\"cluster-master-3\"}]}";
		CreateCluster cc = new CreateCluster();
		String url = bpb.getAmbariServerAddress()+"/api/v1/clusters/mycluster/requests";
		try {
			Map<String,String> repMap = HttpClientTools.sendPost(url, restartJson, "admin:admin");
			MonitorProgress mp = (MonitorProgress) JSONObject.toBean(JSONObject.fromObject(repMap.get("rpString").toLowerCase()), MonitorProgress.class);
			if(cc.monitorCreationProgress(mp))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
