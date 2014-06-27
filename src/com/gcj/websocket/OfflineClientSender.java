package com.gcj.websocket;

import com.gcj.common.ClientGlobalManager;
import com.gcj.common.Constants;
import com.gcj.conf.ConfigUtils;
import com.gcj.file.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class OfflineClientSender {

    private static final Log LOG = LogFactory.getLog(OfflineClientSender.class);

    private static CountDownLatch latch;

    private int seq = 1;

    private int sendCount = 0;

    private String userId = "d22348790547";

    private String targetUserId = "d22348790548";

    private String pwd = "111111";

    private int thinkingTime = 10000;

    private String msg;

    public OfflineClientSender() {
        super();
    }

    @OnOpen
    public void onOpen(Session session) {
        userId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        Constants.index++;
        targetUserId = Constants.targetUserIdList.get(Constants.targetIndex % Constants.userNumbers);
        Constants.targetIndex++;
        thinkingTime = Constants.thinkTime;
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
        while (true) {
            String info = "{\"type\":\"sendText\", \"seq\":" + seq + ", \"target\": \"" + targetUserId + "\", \"blocks\":[{\"type\": \"text\", \"data\": \"" + msg + "\"}]}";
            seq++;
            try {
                Thread.sleep(thinkingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                session.getBasicRemote().sendText(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendCount++;
            LOG.info(userId + "已发送给用户" + targetUserId + "信息数：" + sendCount);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.error(String.format("Session %s close because of %s", session.getId(), closeReason.toString()));
        latch.countDown();
    }

    public void startOfflineClient() {
        try {
            ClientGlobalManager.client.connectToServer(OfflineClientReceiver.class, new URI(Constants.url));

        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
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

        FileUtils.readFileByLines("offline-client-sender.txt");
        FileUtils.readOfflineUsers("offline-client-receiver.txt");
        latch = new CountDownLatch(Constants.userNumbers);

        for (int i = 0; i < Constants.userNumbers; i++) {
            try {
                ClientGlobalManager.client.connectToServer(OfflineClientSender.class, new URI(Constants.url));
                Thread.sleep(10);
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