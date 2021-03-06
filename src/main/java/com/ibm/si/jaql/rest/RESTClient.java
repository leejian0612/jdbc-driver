package com.ibm.si.jaql.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.si.jaql.api.ArielException;
import com.ibm.si.jaql.util.ConnectionUtility;

/**
 * Ariel rest API endpoint client, offering transport of data into and out of the ariel store
 * e.g. posting of ariel search string constructs, get json bound data sets etc 
 * @author IBM
 *
 */
public class RESTClient
{
	static final Logger logger = LogManager.getLogger(RESTClient.class.getName());
	
	private CloseableHttpClient client = null;
	private UsernamePasswordCredentials creds = null;
	private CredentialsProvider credProvider = null;
	private HttpClientContext context = null;
	private HttpHost targetHost = null;
	private AuthCache authCache = null;
	private BasicScheme basicAuth = null;
	
	public RESTClient(final String ip,
					  final String user,
					  final String password) throws ArielException
	{
		targetHost = new HttpHost(ip, 443, "https");
		credProvider = new BasicCredentialsProvider();
		creds = new UsernamePasswordCredentials(user, password);
		credProvider.setCredentials(new AuthScope(ip, AuthScope.ANY_PORT), creds);
		
		authCache = new BasicAuthCache();
		basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);
		
		
		context = HttpClientContext.create();
		context.setCredentialsProvider(credProvider);
		context.setAuthCache(authCache);
		
		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credProvider)
				.setSSLSocketFactory(getSSLFactory())
				.build();
		
		final HttpGet wakeUp = new HttpGet(String.format("https://%s/restapi/doc", ip));
		CloseableHttpResponse res = null;
		
		try
		{
			res = client.execute(targetHost, wakeUp, context);
			EntityUtils.consume(res.getEntity());			
		}
		catch (ClientProtocolException e)
		{
			throw new ArielException(e);
		}
		catch (IOException e)
		{
			throw new ArielException(e);
		}
		finally
		{
			if (res != null)
			{
				ConnectionUtility.closeQuietly(res);
			}
		}
	}
		
	public Result doGet(final String reqBody) throws IOException
	{
		Result result = null;
		final HttpGet method = new HttpGet(buildRequestURI(reqBody));
		 
		CloseableHttpResponse res = null;
		
		try
		{
			res = client.execute(targetHost, method, context);
			final HttpEntity bodyResult = res.getEntity();
			final String body = EntityUtils.toString(bodyResult);
			result = new Result(res.getStatusLine().getStatusCode(), body);
		}
		catch (ClientProtocolException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (res != null)
			{
				ConnectionUtility.closeQuietly(res);
			}
		}
		
		return result;
	}
	
	public Result doPost(final String uri, final Map<String,String> nvPairs) throws IOException
	{
		Result result = null;
		final HttpPost method = new HttpPost(uri);
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String nvName : nvPairs.keySet())
		{
			final BasicNameValuePair pair = new BasicNameValuePair(nvName, nvPairs.get(nvName));
			nvps.add(pair);
		}
		
		CloseableHttpResponse res = null;
		
		try
		{
			method.setEntity(new UrlEncodedFormEntity(nvps));
			res = client.execute(targetHost, method, context);
			final HttpEntity bodyResult = res.getEntity();
			final String body = EntityUtils.toString(bodyResult);
			result = new Result(res.getStatusLine().getStatusCode(), body);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IOException(e);
		}
		catch (ClientProtocolException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (res != null)
			{
				ConnectionUtility.closeQuietly(res);
			}
		}
		
		return result;
	}
	
	public Result doPut(final String uri, final Map<String,String> nvPairs) throws IOException
	{
		Result result = null;
		final HttpPut method = new HttpPut(uri);
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String nvName : nvPairs.keySet())
		{
			final BasicNameValuePair pair = new BasicNameValuePair(nvName, nvPairs.get(nvName));
			nvps.add(pair);
		}
		
		CloseableHttpResponse res = null;
		
		try
		{
			method.setEntity(new UrlEncodedFormEntity(nvps));
			res = client.execute(targetHost, method, context);
			final HttpEntity bodyResult = res.getEntity();
			final String body = EntityUtils.toString(bodyResult);
			result = new Result(res.getStatusLine().getStatusCode(), body);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IOException(e);
		}
		catch (ClientProtocolException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (res != null)
			{
				ConnectionUtility.closeQuietly(res);
			}
		}
		
		return result;
	}
	
	public Result doDelete(final String uri) throws IOException
	{
		Result result = null;
		HttpDelete method = new HttpDelete(uri);

		CloseableHttpResponse res = null;
		
		try
		{
			res = client.execute(targetHost, method, context);
			int status = res.getStatusLine().getStatusCode();
			result = new Result(status);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IOException(e);
		}
		catch (ClientProtocolException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (res != null)
			{
				ConnectionUtility.closeQuietly(res);
			}
		}
		
		return result;
	}
	
	private String buildRequestURI(final String req)
	{
		String result = null;
		
		result = String.format("https://%s/%s", targetHost.getHostName(), req);
		
		return result;
	}
	
    /** Name of the <code>qradarstore</code> resource. */
	private final String QRADARSTORE_RESOURCE_NAME = "/qradarstore";
	
	private SSLConnectionSocketFactory getSSLFactory() throws ArielException
	{
		SSLConnectionSocketFactory sslSf = null;
		
		try
		{
			final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());			
			final InputStream inStream = RESTClient.class.getResourceAsStream(QRADARSTORE_RESOURCE_NAME);
			
			try
			{
				trustStore.load(inStream, "devtest".toCharArray());
			}
			catch (final CertificateException e)
			{
				throw new ArielException(e);
			}
			catch (final IOException e)
			{
				throw new ArielException(e);
			}
			finally
			{
				ConnectionUtility.closeQuietly(inStream);
			}
			
			final SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
					.build();
			
			sslSf = new SSLConnectionSocketFactory(
					sslContext,
					new String[] { "TLSv1" },
					null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		}
		catch (final KeyManagementException e)
		{
			throw new ArielException(e);
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new ArielException(e);
		}
		catch (KeyStoreException e)
		{
			throw new ArielException(e);
		}
		
		return sslSf;
	}
	
	static class Result
	{
		private int status;
		private String body;
		
		public Result(final int status)
		{
			this.status = status;
		}
		
		public Result(final int status, final String body)
		{
			this.status = status;
			this.body = body;
		}
		
		public int getStatus()
		{
			return this.status;
		}
		
		public String getBody()
		{
			logger.debug(String.format(this.body));
			return this.body;
		}
		
		public int getCode()
		{
			Gson gson = null;
			gson = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();
			
			int returnCode = 0;
			
			ErrorResult result = null;
			if (this.status != HttpStatus.SC_OK)
			{
				result = gson.fromJson(this.body, ErrorResult.class);
				if (result != null)
				{
					returnCode = result.getCode();
				}
			}
			
			return returnCode;
		}
	}	
	
	
	static class ErrorResult
	{
		private Map<String,ColumnTuple> response;
		private int code;
		private String message;
		private String description;
		private Map<String,ColumnTuple> details;
		
		public ErrorResult(final Map<String,ColumnTuple> response, final int code, final String message, final String description, final Map<String,ColumnTuple> details)
		{
			this.response = response;
			this.code = code;
			this.message = message;
			this.description = description;
			this.details = details;
		}
		
		public int getCode()
		{
			return this.code;
		}
		
			
		public static class ColumnTuple
		{
			private String name;
			private String value;
			private String type;
			
			public ColumnTuple(final String name,
							   final String value,
							   final String typeString)
			{
				this.name = name;
				this.value = value;
				this.type = typeString;
			}
			
			public String getName()
			{
				return this.name;
			}
			
			public String getValue()
			{
				return this.value;
			}
			
			public String getType()
			{
				return this.type;
			}
		}
	}
	
	
}
