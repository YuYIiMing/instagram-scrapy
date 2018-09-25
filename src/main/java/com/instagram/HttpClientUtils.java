package com.instagram;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;
import java.util.Map;

/**
 * Created by chenminjian on 2018/1/23.
 */
public class HttpClientUtils {

    private static PoolingHttpClientConnectionManager connectionManager;

    private static HttpClientBuilder httpClientBuilder;

    private static RequestConfig requestConfig;

    static {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
        requestConfig = RequestConfig.custom()
                .setSocketTimeout(30 * 1000)
                .setConnectTimeout(30 * 1000)
                .setConnectionRequestTimeout(30 * 1000)
                .setAuthenticationEnabled(true)
                .build();
        connectionManager = new PoolingHttpClientConnectionManager(reg, new FakeDnsResolver());
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setConnectionManager(connectionManager);
    }

    public static CloseableHttpResponse execute(String uri, Object params, String method, Map<String, String> header) throws IOException {
        connectionManager.closeExpiredConnections();
        //proxy
        InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", 1086);
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute("socks.address", socksaddr);

        RequestBuilder builder = RequestBuilder.create(method).setUri(uri).setConfig(requestConfig);
        ContentType contentType = getContentTypeByString(header.get("Content-Type"));
        if (params != null) {
            String str = null;
            if (params instanceof String)
                str = (String) params;
            else {
                if (contentType.equals(ContentType.APPLICATION_JSON))
                    str = JSON.toJSONString(params);
                else if (contentType.equals(ContentType.APPLICATION_FORM_URLENCODED)) {
                    if (params instanceof Map) {
                        str = "";
                        if (((Map) params).size() > 0) {
                            for (Map.Entry<String, String> entry : ((Map<String, String>) params).entrySet()) {
                                str += entry.getKey() + "=" + entry.getValue() + "&";
                            }
                            str = str.substring(0, str.length() - 1);
                        }
                    }
                }
            }
            builder.setEntity(new StringEntity(str, contentType));
        }
        HttpUriRequest request = builder.build();

        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }

        CloseableHttpClient client = httpClientBuilder.build();
        return client.execute(request, context);
    }

    private static ContentType getContentTypeByString(String contentType) {
        switch (contentType) {
            case HttpContentType.APPLICATION_FORM_URLENCODED:
                return ContentType.APPLICATION_FORM_URLENCODED;
            case HttpContentType.APPLICATION_JSON:
                return ContentType.APPLICATION_JSON;
            default:
                throw new HttpRequestException("unsupport content type!");
        }
    }

    static class FakeDnsResolver implements DnsResolver {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            // Return some fake DNS record for every request, we won't be using it
            return new InetAddress[]{InetAddress.getByAddress(new byte[]{1, 1, 1, 1})};
        }
    }

    static class MyConnectionSocketFactory extends PlainConnectionSocketFactory {
        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                                    InetSocketAddress localAddress, HttpContext context) throws IOException {
            // Convert address to unresolved
            InetSocketAddress unresolvedRemote = InetSocketAddress
                    .createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }

    static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        public MySSLConnectionSocketFactory(final SSLContext sslContext) {
            // You may need this verifier if target site's certificate is not secure
            super(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                                    InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress
                    .createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }
}

