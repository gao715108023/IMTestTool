package com.gcj.common;

import org.glassfish.tyrus.client.ClientManager;

/**
 * Created by gaochuanjun on 14-4-24.
 */
public class ClientGlobalManager {

    public static ClientManager client = ClientManager.createClient();
}
