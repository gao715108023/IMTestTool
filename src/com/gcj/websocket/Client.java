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
public class Client {

    private static final Log LOG = LogFactory.getLog(Client.class);

    private static CountDownLatch latch;

    private int seq = 1;

    private int recvCount = 0;

    private int sendCount = 0;

    private String userId = "d22348790547";

    private String targetUserId = "d22348790548";

    private String pwd = "111111";

    private int thinkingTime = 10000;

    private String msg;

    public Client() {
        super();
    }

    @OnOpen
    public void onOpen(Session session) {
        userId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        Constants.index++;
        targetUserId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
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
        if (seq != 2) {
            JSONObject jsonObject = JSONObject.fromObject(message);
            String uid = jsonObject.getString("uid");
            recvCount++;
            LOG.info(userId + "已接受到来自于" + uid + "用户的信息数：" + recvCount);
        }
        String info = "{\"type\":\"sendText\", \"seq\":" + seq + ", \"target\": \"" + targetUserId + "\", \"blocks\":[{\"type\": \"text\", \"data\": \"" + msg + "\"}]}";
        seq++;
        try {
            Thread.sleep(thinkingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendCount++;
        LOG.info(userId + "已发送给用户" + targetUserId + "信息数：" + sendCount);
        return info;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.error(String.format("Session %s close because of %s", session.getId(), closeReason.toString()));
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
        Constants.userNumbers = conf.getInt("numbers_of_threads");
        Constants.thinkTime = conf.getInt("think_time") * 1000;
        Constants.msg = conf.getString("msg");
        Constants.url = Constants.protocol + "://" + Constants.serverIp + ":" + Constants.port + Constants.path;
        FileUtils.readFileByLines("userid.txt");
        latch = new CountDownLatch(Constants.userNumbers);
        ClientManager client = ClientManager.createClient();
        for (int i = 0; i < Constants.userNumbers; i++) {
            try {
                client.connectToServer(Client.class, new URI(Constants.url));
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