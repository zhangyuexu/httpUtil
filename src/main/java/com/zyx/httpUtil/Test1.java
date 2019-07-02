package com.zyx.httpUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author zyx
 *
 */
public class Test1 {

    // utf-8字符编码
    public static final String CHARSET_UTF_8 = "utf-8";

    // HTTP内容类型。
    public static final String CONTENT_TYPE_TEXT_HTML = "text/xml";

    // HTTP内容类型。相当于form表单的形式，提交数据
    public static final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";

    // HTTP内容类型。相当于form表单的形式，提交数据
    public static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";
    

    // 连接管理器
    private static PoolingHttpClientConnectionManager pool;

    // 请求配置
    private static RequestConfig requestConfig;

    static {
        
        try {
            System.out.println("初始化HttpClientTest~~~开始");
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register(
                    "http", PlainConnectionSocketFactory.getSocketFactory()).register(
                    "https", sslsf).build();
            // 初始化连接管理器
            pool = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            // 将最大连接数增加到200，实际项目最好从配置文件中读取这个值
            pool.setMaxTotal(200);
            // 设置最大路由
            pool.setDefaultMaxPerRoute(2);
            // 根据默认超时限制初始化requestConfig
            int socketTimeout = 10000;
            int connectTimeout = 10000;
            int connectionRequestTimeout = 10000;
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).build();

            System.out.println("初始化HttpClientTest~~~结束");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        

        // 设置请求超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(50000)
                .setConnectionRequestTimeout(50000).build();
    }

    public static CloseableHttpClient getHttpClient() {
        
        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(pool)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();
        
        return httpClient;
    }

    /**
     * 发送Post请求
     * 
     * @param httpPost
     * @return
     */
    private static String sendHttpPost(HttpPost httpPost) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 可以获得响应头
            // Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
            // for (Header header : headers) {
            // System.out.println(header.getName());
            // }

            // 得到响应类型
            // System.out.println(ContentType.getOrDefault(response.getEntity()).getMimeType());

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
                System.out.println(entity.getContentType());  
                System.out.println(entity.getContentLength());  
                  
                //String resString = EntityUtils.toString(entity);  
                // 使用返回的字符串直接构造一个JSONObject       
                JSONObject jsonobj = new JSONObject(responseContent);  
                System.out.println(jsonobj.toString());  
                // 获取返回对象中"resultSize的值"  
                int resutltSize = jsonobj.getInt("resultSize");  
                System.out.println("Search Results Size is: "+ resutltSize);   
                // 获取"clients"的值,它是一个JSONArray  
                JSONArray jsonarray = jsonobj.getJSONArray("clients");  
                System.out.println(jsonarray.toString());  
                  
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 发送Get请求
     * 
     * @param httpGet
     * @return
     */
    private static JSONObject sendHttpGet(HttpGet httpGet) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        JSONObject jsonobj=null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 可以获得响应头
            // Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
            // for (Header header : headers) {
            // System.out.println(header.getName());
            // }

            // 得到响应类型
            // System.out.println(ContentType.getOrDefault(response.getEntity()).getMimeType());

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
                //System.out.println(entity.getContentType());  
                //System.out.println(entity.getContentLength());  
                  
                //String resString = EntityUtils.toString(entity);  
                // 使用返回的字符串直接构造一个JSONObject       
                 jsonobj = new JSONObject(responseContent); 
                 
                //System.out.println(jsonobj.toString());  
                // 获取返回对象中"size的值"  
//                JSONObject dataObj=jsonobj.getJSONObject("data");
//                int size = dataObj.getInt("size");  
//               System.out.println("Search Size is: "+ size);   
//                // 获取"listDetail"的值,它是一个JSONArray  
//                JSONArray jsonarray = dataObj.getJSONArray("listDetail");  
//                FileWriter fw=new FileWriter("F:\\p_code.txt");
//                for(int i=0;i<10;i++) {
//                	JSONObject p_codeObj=(JSONObject)jsonarray.get(i);
//                	int p_code=p_codeObj.getInt("p_code");
//                	fw.write(p_code+"\n");
//                	//System.out.println(p_code);
//                }	
//                fw.close();
//                System.out.println(jsonarray.toString());
//
          }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonobj;
    }
    
    
    
    /**
     * 发送 post请求
     * 
     * @param httpUrl
     *            地址
     */
    public static String sendHttpPost(String httpUrl) {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 get请求
     * 
     * @param httpUrl
     */
    public static JSONObject sendHttpGet(String httpUrl) {
        // 创建get请求
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet);
    }
    
    

    /**
     * 发送 post请求（带文件）
     * 
     * @param httpUrl
     *            地址
     * @param maps
     *            参数
     * @param fileLists
     *            附件
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
        if (maps != null) {
            for (String key : maps.keySet()) {
                meBuilder.addPart(key, new StringBody(maps.get(key), ContentType.TEXT_PLAIN));
            }
        }
        if (fileLists != null) {
            for (File file : fileLists) {
                FileBody fileBody = new FileBody(file);
                meBuilder.addPart("files", fileBody);
            }
        }
        HttpEntity reqEntity = meBuilder.build();
        httpPost.setEntity(reqEntity);
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 post请求
     * 
     * @param httpUrl
     *            地址
     * @param params
     *            参数(格式:key1=value1&key2=value2)
     * 
     */
    public static String sendHttpPost(String httpUrl, String params) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (params != null && params.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(params, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_FORM_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 post请求
     * 
     * @param maps
     *            参数
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps) {
        String parem = convertStringParamter(maps);
        return sendHttpPost(httpUrl, parem);
    }

    
    
    
    /**
     * 发送 post请求 发送json数据
     * 
     * @param httpUrl
     *            地址
     * @param paramsJson
     *            参数(格式 json)
     * 
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }
    
    /**
     * 发送 post请求 发送xml数据
     * 
     * @param httpUrl   地址
     * @param paramsXml  参数(格式 Xml)
     * 
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (paramsXml != null && paramsXml.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsXml, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_TEXT_HTML);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }
    

    /**
     * 将map集合的键值对转化成：key1=value1&key2=value2 的形式
     * 
     * @param parameterMap
     *            需要转化的键值对集合
     * @return 字符串
     */
    public static String convertStringParamter(Map parameterMap) {
        StringBuffer parameterBuffer = new StringBuffer();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    value = (String) parameterMap.get(key);
                } else {
                    value = "";
                }
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }

    public static void main(String[] args) throws Exception {
    	long start_time=1514520000000L;
        long end_time=1517232754000L;
        FileWriter fw=new FileWriter("F:\\p_code.txt");
        for(int page=1;page<51;page++) {
        	JSONObject jsonobj=sendHttpGet("http://baymax.intra.xiaojukeji.com/api/monitor/getMoniterInfoList?alarm_type=0&start_time=&end_time=&monitor=&admin=&product_id=&page="+page+"&code=&node=&size=10");
        	JSONObject dataObj=jsonobj.getJSONObject("data");
        	// 获取"listDetail"的值,它是一个JSONArray  
          JSONArray jsonarray = dataObj.getJSONArray("listDetail");  
          
          for(int i=0;i<10;i++) {
          	JSONObject p_codeObj=(JSONObject)jsonarray.get(i);
          	int p_code=p_codeObj.getInt("p_code");
          	fw.write(p_code+"\n");
          	//System.out.println(p_code);
          }	
        }
        fw.close();    
        

        FileInputStream fis=new FileInputStream("f:\\p_code.txt");
        
        FileWriter fw2=new FileWriter("F:\\jsonobj3.txt");
        FileWriter fw3=new FileWriter("F:\\url3.txt");
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(fis));
        
        String line=null;
        String pro_id=null;
        List<String>citys=new ArrayList<String>();
        citys.add("010");
        citys.add("020");
        citys.add("021");
        citys.add("0755");
        List<String>products=new ArrayList<String>();
        products.add("1");
        products.add("2");
        products.add("3");
        List<String>sizes=new ArrayList<String>();
        sizes.add("min");
        sizes.add("5min");
        sizes.add("10min");
        sizes.add("hour");
        while((pro_id=bufferedReader.readLine())!=null) {
        	for(String size:sizes) {
        		for(String product:products) {
            		for(String city:citys) {
//            			JSONObject jsonobj=sendHttpGet("http://baymax.intra.xiaojukeji.com/api/monitorChart/getProLine?"+"pro_id="+pro_id+"&start_time="+
//                            	start_time+"&end_time="+end_time+"&size="+size+"&product="+product+"&city="+city);
//                    	fw3.write("http://baymax.intra.xiaojukeji.com/api/monitorChart/getProLine?"+"pro_id="+pro_id+"&start_time="+
//                            	start_time+"&end_time="+end_time+"&size=hour&product=all&city=all"+"\n");
            			fw3.write("http://baymax.intra.xiaojukeji.com/api/monitorChart/getProLine?"+"pro_id="+pro_id+"&start_time="+
                            	start_time+"&end_time="+end_time+"&size="+size+"&product="+product+"&city="+city+"\n");
//                    	jsonobj.remove("code");
//                    	jsonobj.remove("errmsg");
//                    	//jsonobj.put("pro_id", pro_id);
//                    	//jsonobj.put("start_time", start_time);
//                    	//jsonobj.put("end_time", end_time);
//                    	jsonobj.put("size", size);
//                    	jsonobj.put("product",product);
//                    	jsonobj.put("city", city);
//                     	//System.out.println(jsonobj);
//                     	fw2.write(jsonobj+"\n");
            		}
            		
            	}
        	}
        	
        }
        fis.close();
        FileInputStream fis2=new FileInputStream("f:\\url3.txt");
        BufferedReader bufferedReader2=new BufferedReader(new InputStreamReader(fis2));
        while((line=bufferedReader2.readLine())!=null) {
        	JSONObject jsonobj=sendHttpGet(line);
        	jsonobj.remove("code");
        	jsonobj.remove("errmsg");
        	//jsonobj.put("pro_id", pro_id);
        	//jsonobj.put("start_time", start_time);
        	//jsonobj.put("end_time", end_time);
         	//System.out.println(jsonobj);
         	fw2.write(jsonobj+"\n");
        }
       fis2.close();
       bufferedReader.close();
       fw2.close();
       fw3.close();
    }

}
