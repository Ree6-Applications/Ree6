package de.presti.ree6.utils;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

public class ProxyUtil {

    private static Proxy proxy;
    private static String ip;
    private static String port;
    //private static OioSocketChannel socketChannel;

    /*public static Proxy getProxy() {
        return proxy;
    }*/

    public static void setProxy(Proxy proxy) {
        ProxyUtil.proxy = proxy;
    }

    public static void setProxy(String ip, String port) {
        try {
            ProxyUtil.ip = ip;
            ProxyUtil.port = port;
            setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, Integer.parseInt(port))));
        } catch (Exception exception) {
            setProxy(null);
            exception.printStackTrace();
        }
    }

   /* public static ChannelFactory<OioSocketChannel> createProxyChannel() {

        Logger.log("ProxyConnector", "creating ProxyChannel");

        if(getProxy() != null && getProxy() != Proxy.NO_PROXY) {
            if(socketChannel != null) {
                closeCurrentProxy();
            }
        }

        return () -> {
            if (getProxy() == null || getProxy() == Proxy.NO_PROXY) {
                return new OioSocketChannel(new Socket(Proxy.NO_PROXY));
            }
            final Socket sock = new Socket(getProxy());
            try {
                Method m = sock.getClass().getDeclaredMethod("getImpl");
                m.setAccessible(true);
                Object sd = m.invoke(sock);
                Method m1 = sd.getClass().getDeclaredMethod("setV4");
                m1.setAccessible(true);
                m1.invoke(sd);
                Logger.log("ProxyConnector", "connecting to Proxy");
                return socketChannel = new OioSocketChannel(sock);
            }
            catch (Exception ex2) {
                throw new RuntimeException("Failed to create socks 4 proxy!", new Exception());
            }
        };
    } */

    public static void connectToProxy() {
        if((ip != null && port != null) && (!ip.isEmpty() && !port.isEmpty())) {
            disconnectFromProxy();
            System.setProperty("socksProxyHost", ip);
            System.setProperty("socksProxyPort", port);
            Logger.log("ProxyConnector", "Connecting to " + System.getProperty("socksProxyHost") + ":" + System.getProperty("socksProxyPort"));
        }
    }

    public static void disconnectFromProxy() {
        Logger.log("ProxyConnector", "Disconnecting from " + System.getProperty("socksProxyHost") + ":" + System.getProperty("socksProxyPort"));
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
    }

/*    public static void closeCurrentProxy() {
        Logger.log("ProxyConnector", "closing ProxyChannel");
        socketChannel.close();
    } */

    public static String getProxies() throws Exception {
        HttpURLConnection conn = (HttpURLConnection)(new URL("https://api.proxyscrape.com/v2/?request=getproxies&protocol=socks4&timeout=150&country=all")).openConnection();
        conn.setDoInput(true);
        BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String resp = "";
        for (String read; (read = bf.readLine()) != null;) {
            resp = resp + read + "\n";
        }
        if (resp.endsWith("\n")) resp = resp.substring(0, resp.length() - 1);

        bf.close();
        return resp;
    }

}