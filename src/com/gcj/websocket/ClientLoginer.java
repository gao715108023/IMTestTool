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
public class ClientLoginer {

    private static final Log LOG = LogFactory.getLog(ClientLoginer.class);

    private static CountDownLatch latch;

    private int seq = 1;

    private String userId = "d22348790547";

    private String pwd = "111111";

    public ClientLoginer() {
        super();
    }

    //private static long start;

    @OnOpen
    public void onOpen(Session session) {
        userId = Constants.userIdList.get(Constants.index % Constants.userNumbers);
        Constants.index++;
        LOG.info(userId + " established a connection with " + Constants.url + " successfully！");
        String login = "{\"type\":\"login\",\"seq\":" + seq + ",\"uid\":\"" + userId + "\",\"pwd\":\"" + pwd + "\"}";
        seq++;
        LOG.debug(login);
        //long end = System.currentTimeMillis();
        //System.out.println(end-start);
        try {
            session.getBasicRemote().sendText(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            LOG.debug(userId + ".Received: " + message);
            JSONObject jsonObject = JSONObject.fromObject(message);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                LOG.debug(userId + "登录成功！");
            } else if (code == -1) {
                LOG.error(userId + "登录失败！");
            }
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.debug(String.format("Session %s close because of %s", session.getId(), closeReason.toString()));
        LOG.info(userId + "退出登录！");
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
        Constants.thinkTime = conf.getInt("think_time") * 1000;
        Constants.msg = conf.getString("msg");
        Constants.url = Constants.protocol + "://" + Constants.serverIp + ":" + Constants.port + Constants.path;
        int duration = conf.getInt("duration");
        int time = duration * 60 * 60 * 1000;
        int iter = time / 20000;
        FileUtils.readAllContent("client-login.txt");
        Constants.userNumbers = Constants.userIdList.size();
        latch = new CountDownLatch(iter);

        ClientManager client = ClientManager.createClient();

        long start;

        long end;

        long spendTime;

        while (true) {

            start = System.currentTimeMillis();

            for (int i = 0; i < Constants.userNumbers; i++) {
                try {
                    client.connectToServer(ClientLoginer.class, new URI(Constants.url));
                    Thread.sleep(10);
                } catch (DeploymentException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            end = System.currentTimeMillis();

            spendTime = end - start;

            if (spendTime >= time) {
                LOG.info("测试时间到，即将退出测试......");
                break;
            } else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            latch.await();
            LOG.info("测试结束！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}