package com.gcj.websocket;

import com.gcj.common.Constants;
import com.gcj.conf.ConfigUtils;
import com.gcj.file.FileUtils;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class OfflineClientReceiver {

    private static final Log LOG = LogFactory.getLog(OfflineClientReceiver.class);

    public static CountDownLatch latch;

    private int seq = 1;

    private int recvCount = 0;

    private String userId = "d22348790547";

    private String pwd = "111111";

    public OfflineClientReceiver() {
        super();
    }

    @OnOpen
    public void onOpen(Session session) {
        session.setMaxIdleTimeout(3000);
        userId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        Constants.index++;
        LOG.info(userId + " established a connection with " + Constants.url + " successfully！");
        String login = "{\"type\":\"login\",\"seq\":" + seq + ",\"uid\":\"" + userId + "\",\"pwd\":\"" + pwd + "\"}";
        seq++;
        LOG.debug(login);
        try {
            session.getBasicRemote().sendText(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOG.debug(userId + ".Received: " + message);
        if (seq != 2) {
            JSONObject jsonObject = JSONObject.fromObject(message);
            String uid = jsonObject.getString("uid");
            recvCount++;
            LOG.info(userId + "已接受到来自于" + uid + "用户的信息数：" + recvCount);
        }
        seq++;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        try {
            latch.countDown();
        } finally {
            try {
                if (session != null) {
                    session.close();
                    LOG.warn(String.format("Client %s close because of %s", userId, closeReason.toString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        ConfigUtils conf = new ConfigUtils("infoconfig.properties");
        Constants.serverIp = conf.getString("server_ip");
        Constants.port = conf.getInt("server_port");
        Constants.path = conf.getString("path");
        Constants.protocol = conf.getString("protocol");
        Constants.userNumbers = conf.getInt("numbers_of_threads");
        Constants.thinkTime = conf.getInt("think_time") * 1000;
        Constants.msg = conf.getString("msg");
        Constants.url = Constants.protocol + "://" + Constants.serverIp + ":" + Constants.port + Constants.path;
        FileUtils.readFileByLines("offline-client-receiver.txt");
        ClientManager client;
        client = ClientManager.createClient();
        while (true) {
            try {
                if (Constants.index >= 100000) {
                    Constants.index = 0;
                    if (client != null) {
                        client = null;
                        client = ClientManager.createClient();
                    }
                }
                Thread.sleep(10000);
                latch = new CountDownLatch(Constants.userNumbers);
                for (int i = 0; i < Constants.userNumbers; i++) {
                    client.connectToServer(OfflineClientReceiver.class, new URI(Constants.url));
                    Thread.sleep(10);
                }
                latch.await();
                if (latch != null)
                    latch = null;
            } catch (DeploymentException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}