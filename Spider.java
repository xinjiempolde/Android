package com.test.spider;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Spider {
	private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36";
	private UrlEncodedFormEntity entity = null;
	private CloseableHttpClient httpClient;
	private CloseableHttpResponse httpResponse = null;
	HttpEntity httpEntity;
	private String rsa = null;
	private String ul = null;
	private String pl = null;
	private String lt = null;
	private String execution = null;
	private String _eventId = "submit";
	private List<NameValuePair> postDate = null;
	private CookieStore cookieStore = null;
	public Spider(String stuId, String stuPass) {
		rsa = stuId + stuPass;
		ul = String.valueOf(stuId.length());
		pl = String.valueOf(stuPass.length());
	}
	
	//通过HttpGet获取源代码
	//通过源代码隐藏的数据构造表单数据
	public void prepareFormDate() throws ClientProtocolException, IOException {
		httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		HttpGet httpGet = new HttpGet("https://pass.neu.edu.cn/tpass/login");
		httpResponse = httpClient.execute(httpGet);
		httpEntity = httpResponse.getEntity();
		//获取到page为网页未提交数据时源代码
		String page = EntityUtils.toString(httpEntity);
		httpResponse.close();
		httpGet.releaseConnection();
		
		//获取lt数据
		Pattern ltpattern= Pattern.compile("<input type=\"hidden\" id=\"lt\" name=\"lt\" value=\"(.*?)\" />");
		Matcher ltgroup = ltpattern.matcher(page);
		if (ltgroup.find()) {
			lt = ltgroup.group(1);
			rsa += lt;
		}
		
		//获取execution数据
		Pattern executionPattern = Pattern.compile("<input type=\"hidden\" name=\"execution\" value=\"(.*?)\" />");
		Matcher executiongroup = executionPattern.matcher(page);
		if (executiongroup.find()) {
			execution = executiongroup.group(1);
		}
		
		
		//向postDate中添加数据
		postDate = new ArrayList<NameValuePair>();
		postDate.add(new BasicNameValuePair("rsa", rsa));
		postDate.add(new BasicNameValuePair("ul", ul));
		postDate.add(new BasicNameValuePair("pl", pl));
		postDate.add(new BasicNameValuePair("lt", lt));
		postDate.add(new BasicNameValuePair("execution", execution));
		postDate.add(new BasicNameValuePair("_eventId", _eventId));
		entity = new UrlEncodedFormEntity(postDate,"utf-8");
		
		
	}
	
	//开始提交表单数据，模拟登陆
	public void postDate() throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost("https://pass.neu.edu.cn/tpass/login");
		httpPost.addHeader("User-Agent",userAgent);
		httpPost.setEntity(entity);
		
		httpResponse = httpClient.execute(httpPost);
		
		//大佬说:post的多次跳转httpclient不能自己处理
        if (httpResponse .getStatusLine().getStatusCode()>=300&&httpResponse.getStatusLine().getStatusCode()<400){
        	//nextURL = http://portal.neu.edu.cn/tp_up
            String nextURL = httpResponse.getFirstHeader("location").getValue();
            httpResponse.close();
            HttpGet jump = new HttpGet(nextURL);
            jump.addHeader("User-Agent", userAgent);
            httpResponse  = httpClient.execute(jump);
        }
		httpEntity = httpResponse.getEntity();
		String content = EntityUtils.toString(httpEntity);
		System.out.println(content);
		httpPost.releaseConnection();
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		Spider spider = new Spider("stuId", "stuPass");
		spider.prepareFormDate();
		spider.postDate();
	}

}
