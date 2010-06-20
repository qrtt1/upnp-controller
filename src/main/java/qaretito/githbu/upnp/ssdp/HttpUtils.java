package qaretito.githbu.upnp.ssdp;

import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

    private static Log logger = LogFactory.getLog(HttpUtils.class);
    
    public static String getData(String url) throws Exception {
        HttpClient client = HttpClientFactory.createThreadSafeHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String data = EntityUtils.toString(client.execute(httpGet).getEntity());
        client.getConnectionManager().shutdown();
        return data;
    }

    public static StringReader getReader(String url) throws Exception {
        return new StringReader(getData(url));
    }

    public static Object post(String url, Map<String, String> request,
            String body) throws Exception {
        
        logger.info("post to url: " + url);
        HttpPost httpPost = new HttpPost(url);
        for (Header header : httpPost.getAllHeaders()) {
            httpPost.removeHeader(header);
        }
        for (Entry<String, String> entry : request.entrySet()) {
            httpPost.addHeader(entry.getKey(), entry.getValue());
        }
        
        httpPost.setEntity(new ByteArrayEntity(body.getBytes()));
        HttpClient client = HttpClientFactory.createThreadSafeHttpClient();
        final HttpResponse response = client.execute(httpPost);
        logger.info(response.getStatusLine().toString());
        String data = EntityUtils.toString(response.getEntity());
        client.getConnectionManager().shutdown();
        return data;
    }

}
