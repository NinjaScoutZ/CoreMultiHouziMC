package com.houzicore.shared.server.remotecall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.UtilSystem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import com.google.gson.Gson;

public class JsonWebCall {
	private final String _url;
	private final PoolingClientConnectionManager _connectionManager;

	@SuppressWarnings("deprecation")
	public JsonWebCall(String url) {
		_url = url;

		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		_connectionManager = new PoolingClientConnectionManager(schemeRegistry);
		_connectionManager.setMaxTotal(200);
		_connectionManager.setDefaultMaxPerRoute(20);
	}

	protected String convertStreamToString(InputStream is) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		final StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (final IOException e) {
			org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}
		}
		return sb.toString();
	}

	public void Execute() {
		Execute((Object) null);
	}

	public <T> T Execute(Class<T> returnClass) {
		return Execute(returnClass, (Object) null);
	}

	public <T> void Execute(Class<T> callbackClass, Callback<T> callback) {
		Execute(callbackClass, callback, (Object) null);
	}

	@SuppressWarnings("deprecation")
	public <T> void Execute(Class<T> callbackClass, Callback<T> callback, Object argument) {
		final HttpClient httpClient = new DefaultHttpClient(_connectionManager);
		InputStream in = null;
		String result = null;

		try {
			HttpResponse response;

			final Gson gson = new Gson();
			final HttpPost request = new HttpPost(_url);

			if (argument != null) {
				final StringEntity params = new StringEntity(gson.toJson(argument));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(params);
			}

			response = httpClient.execute(request);

			if (response != null && callback != null) {
				in = response.getEntity().getContent();

				result = convertStreamToString(in);
				callback.run(new Gson().fromJson(result, callbackClass));
			}
		} catch (final Exception ex) {
			UtilSystem.printStackTrace(ex.getStackTrace());
		} finally {
			httpClient.getConnectionManager().shutdown();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public <T> T Execute(Class<T> returnClass, Object argument) {
		final HttpClient httpClient = new DefaultHttpClient(_connectionManager);
		InputStream in = null;
		T returnData = null;
		String result = null;

		try {
			HttpResponse response;

			final Gson gson = new Gson();
			final HttpPost request = new HttpPost(_url);

			if (argument != null) {
				final StringEntity params = new StringEntity(gson.toJson(argument));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(params);
			}

			response = httpClient.execute(request);

			if (response != null) {
				in = response.getEntity().getContent();

				result = convertStreamToString(in);
				returnData = new Gson().fromJson(result, returnClass);
			}
		} catch (final Exception ex) {

			for (final StackTraceElement trace : ex.getStackTrace()) {
			}
		} finally {
			httpClient.getConnectionManager().shutdown();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return returnData;
	}

	@SuppressWarnings("deprecation")
	public void Execute(Object argument) {
		final HttpClient httpClient = new DefaultHttpClient(_connectionManager);
		final InputStream in = null;

		try {
			final Gson gson = new Gson();
			final HttpPost request = new HttpPost(_url);

			if (argument != null) {
				final StringEntity params = new StringEntity(gson.toJson(argument));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(params);
			}

			httpClient.execute(request);
		} catch (final Exception ex) {

			for (final StackTraceElement trace : ex.getStackTrace()) {
			}
		} finally {
			httpClient.getConnectionManager().shutdown();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public <T> T Execute(Type returnType, Object argument) {
		final HttpClient httpClient = new DefaultHttpClient(_connectionManager);
		InputStream in = null;
		T returnData = null;
		String result = null;

		try {
			HttpResponse response;

			final Gson gson = new Gson();
			final HttpPost request = new HttpPost(_url);

			if (argument != null) {
				final StringEntity params = new StringEntity(gson.toJson(argument));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(params);
			}

			response = httpClient.execute(request);

			if (response != null) {
				in = response.getEntity().getContent();

				result = convertStreamToString(in);
				returnData = new Gson().fromJson(result, returnType);
			}
		} catch (final Exception ex) {

			for (final StackTraceElement trace : ex.getStackTrace()) {
			}
		} finally {
			httpClient.getConnectionManager().shutdown();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return returnData;
	}

	@SuppressWarnings("deprecation")
	public String ExecuteReturnStream(Object argument) {
		final HttpClient httpClient = new DefaultHttpClient(_connectionManager);
		InputStream in = null;
		String result = null;

		try {
			HttpResponse response;

			final Gson gson = new Gson();
			final HttpPost request = new HttpPost(_url);

			if (argument != null) {
				final StringEntity params = new StringEntity(gson.toJson(argument));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(params);
			}

			response = httpClient.execute(request);

			if (response != null) {
				in = response.getEntity().getContent();
				result = convertStreamToString(in);
			}
		} catch (final Exception ex) {

			for (final StackTraceElement trace : ex.getStackTrace()) {
			}
		} finally {
			httpClient.getConnectionManager().shutdown();

			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return result;
	}
}
