package com.gcj.websocket;

import com.gcj.common.Constants;
import com.gcj.conf.ConfigUtils;
import com.gcj.file.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class NotActiveClient {

    private static final Log LOG = LogFactory.getLog(NotActiveClient.class);

    private static CountDownLatch latch;

    private int seq = 1;

    private String userId = "d22348790547";

    private String targetUserId = "d22348790548";

    private String pwd = "111111";

    private int thinkingTime = Integer.MAX_VALUE;

    private String msg;

    public NotActiveClient() {
        super();
    }

    @OnOpen
    public void onOpen(Session session) {
        userId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        Constants.index++;
        targetUserId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        msg = Constants.msg;
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
    public String onMessage(String message, Session session) {
        LOG.debug(userId + ".Received: " + message);
        LOG.info(userId + " is sleeping......");
        try {
            Thread.sleep(thinkingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info(userId + " sleep over!");
        String info = "{\"type\":\"sendText\", \"seq\":" + seq + ", \"target\": \"" + targetUserId + "\", \"blocks\":[{\"type\": \"text\", \"data\": \"" + msg + "\"}]}";
        return info;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.warn(String.format("Client %s close because of %s", userId, closeReason.toString()));
        latch.countDown();
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
        Constants.userNumbers = conf.getInt("no_active_numbers_of_threads");
        Constants.msg = conf.getString("msg");
        Constants.url = Constants.protocol + "://" + Constants.serverIp + ":" + Constants.port + Constants.path;
        FileUtils.readFileByLines("no-active-userid.txt");
        latch = new CountDownLatch(Constants.userNumbers);
        ClientManager client = ClientManager.createClient();
        for (int i = 0; i < Constants.userNumbers; i++) {
            try {
                client.connectToServer(NotActiveClient.class, new URI(Constants.url));
                Thread.sleep(100);
            } catch (DeploymentException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}