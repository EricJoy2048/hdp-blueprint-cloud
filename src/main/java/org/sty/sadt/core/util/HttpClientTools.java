package org.sty.sadt.core.util;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.httpclient.HttpClient; 
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpClientTools {
	
	
	public static final Logger log = LoggerFactory.getLogger(HttpClientTools.class);

	/**
	 * 获取http get请求内容
	 * @param url
	 * 		请求地址
	 * @param params
	 * 		格式username=user1&password=pa
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Map<String, String> sendGet(String url , String params ,  String usernameAndPassword){
		
		Map<String, String> resMap = new HashedMap();
		
//		System.out.println("http请求地址：" + url);  
//      System.out.println("http请求方法：" + "GET");  
//      System.out.println("http请求参数：" + params);  
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		byte[] key = usernameAndPassword.getBytes();  
        String encoding = Base64.encodeBase64String(key);  
        
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("X-Requested-By", "ambari");
		httpget.setHeader("Authorization" , "Basic " + encoding);
		String rpString = "";
		HttpResponse response;
		try {
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				rpString = EntityUtils.toString(entity);
				httpget.abort();
			}
			
			resMap.put("code", response.getStatusLine().getStatusCode()+"");
			resMap.put("rpString", rpString);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resMap;
	}
	
	/**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
	 * @throws IOException 
	 * @throws ClientProtocolException 
     */
    public static Map<String, String> sendPost(String url , String params ,  String usernameAndPassword){
    	
//    	System.out.println("http请求地址：" + url);  
//        System.out.println("http请求方法：" + "POST");  
//        System.out.println("http请求参数：" + params);  
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		byte[] key = usernameAndPassword.getBytes();  
        String encoding = Base64.encodeBase64String(key);  
        
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("X-Requested-By", "ambari");
		httppost.setHeader("Authorization" , "Basic " + encoding);
		
		Map<String, String> resMap = new HashedMap();
		StringEntity reqEntity;
		try {
			reqEntity = new StringEntity(params);
			//reqEntity.setContentType("application/json");
			httppost.setEntity(reqEntity);  
			
			HttpResponse response = httpClient.execute(httppost);
			HttpEntity entity = response.getEntity();
			String rpString = "";
			if (entity != null) {
				rpString = EntityUtils.toString(entity);
			}
			
			resMap.put("code", response.getStatusLine().getStatusCode()+"");
			resMap.put("rpString", rpString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resMap;
    }    
    
    /**
     * 向指定 URL 发送PUT方法的请求
     * @param url
     * @param params
     * @param usernameAndPassword
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    
    
    public static Map<String, String> sendPut(String url , String params ,  String usernameAndPassword){
    	
//    	System.out.println("http请求地址：" + url);  
//        System.out.println("http请求方法：" + "PUT");  
//        System.out.println("http请求参数：" + params);  
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		byte[] key = usernameAndPassword.getBytes();  
        String encoding = Base64.encodeBase64String(key);  
        
		HttpPut httpput = new HttpPut(url);
		httpput.setHeader("X-Requested-By", "ambari");
		httpput.setHeader("Authorization" , "Basic " + encoding);
		Map<String, String> resMap = new HashedMap();
		StringEntity reqEntity;
		try {
			reqEntity = new StringEntity(params);
			//reqEntity.setContentType("application/json");
			httpput.setEntity(reqEntity);  
			
			HttpResponse response = httpClient.execute(httpput);
			HttpEntity entity = response.getEntity();
			String rpString = "";
			if (entity != null) {
				rpString = EntityUtils.toString(entity);
			}
			
			resMap.put("code", response.getStatusLine().getStatusCode()+"");
			resMap.put("rpString", rpString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return resMap;
    }   

}
