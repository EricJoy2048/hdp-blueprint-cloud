package org.sty.sadt.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

/**
 * HttpURL请求工具类
 * @author BFD_491
 *
 */
public class HttpURLConnectionTool {

	public static  String sendRequest(String url , String params , String method , String usernameAndPassword){
		String rpString = "";
	    URL requestUrl;
	    StringBuffer out = new StringBuffer();
		try {
			
			requestUrl = new URL(url);  
            System.out.println("http请求地址：" + url);  
            System.out.println("http请求方法：" + method);  
            System.out.println("http请求参数：" + params);  
            System.out.println("http请求认证：" + usernameAndPassword);  
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();  
            connection.setRequestMethod(method);  
            connection.setDoOutput(true);  
            
            byte[] key = usernameAndPassword.getBytes();  
            String encoding = Base64.encodeBase64String(key);  
            
            connection.setRequestProperty("Authorization", "Basic " + encoding);  
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8"); 
            connection.setRequestProperty("X-Requested-By", "ambari"); 
            if (params != null) {  
                byte[] outputInBytes = params.getBytes("UTF-8");  
                OutputStream os = connection.getOutputStream();  
                os.write(outputInBytes);  
                os.close();  
            }  
            
            InputStream content = (InputStream) connection.getInputStream();  
            // 解决乱码问题  
            BufferedReader in = new BufferedReader(new InputStreamReader(content, Charset.forName("UTF-8")));  
            String line;  
            while ((line = in.readLine()) != null) {  
                out.append(line);  
            }  
            in.close();  
            connection.disconnect();  
			
			rpString = out.toString();
			
		    
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
		
		return rpString;
	}
	
	
	/**
	 * get请求
	 * @param url
	 * @param params
	 * @param usernameAndPassword
	 * @return
	 */
	public static String sendGet(String url , String params , String usernameAndPassword){
		
		return HttpURLConnectionTool.sendRequest(url, params, "GET", usernameAndPassword);
		
	}
	
	
	public static String sendPost(String url , String params , String usernameAndPassword){
		
		return HttpURLConnectionTool.sendRequest(url, params, "POST", usernameAndPassword);
		
	}
}
