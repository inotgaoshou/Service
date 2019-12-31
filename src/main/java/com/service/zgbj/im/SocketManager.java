package com.service.zgbj.im;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.service.zgbj.mysqlTab.controller.ChatServiceImpl;
import com.service.zgbj.mysqlTab.controller.HistoryServiceImpl;
import com.service.zgbj.mysqlTab.controller.UserServiceImpl;
import com.service.zgbj.utils.GsonUtil;
import com.service.zgbj.utils.OfTenUtils;

import java.util.HashMap;
import java.util.List;

public class SocketManager {


    //Socket客户端容器
    private HashMap<String, SocketIOClient> mClientMap = new HashMap<>();
    private SocketConnectListener connectListener;
    private SocketDisConnectListener disConnectListener;
    private SocketDataListener dataListener;
    private UserServiceImpl sqlService;
    private ChatServiceImpl chatService;
    private SocketIOServer socketService;
    private HistoryServiceImpl historyService;

    public SocketManager(SocketIOServer server, UserServiceImpl userService, ChatServiceImpl chatServiceImp, HistoryServiceImpl historyServiceImpl) {
        this.sqlService = userService;
        this.chatService = chatServiceImp;
        this.historyService = historyServiceImpl;
        if (server != null) {
            this.socketService = server;
            connectListener = new SocketConnectListener();
            disConnectListener = new SocketDisConnectListener();
            dataListener = new SocketDataListener();
            server.addConnectListener(connectListener);
            server.addDisconnectListener(disConnectListener);
            server.addEventListener("chat", String.class, dataListener);
        }
    }

    /**
     * 客户端连接监听
     */
    class SocketConnectListener implements ConnectListener {

        @Override
        public void onConnect(SocketIOClient socketIOClient) {
            System.out.println("--------客户端连接------");
            String client = getClientUid(socketIOClient);
            System.out.println("======userId=====" + client);
            mClientMap.put(client, socketIOClient);
            System.out.println("-------当前连接人数--------" + mClientMap.size());
            sendOnLineASK(client, 1);
            List<ChatMessage> msg = chatService.getOffLineMsg(client);

            for (int i = 0; i < msg.size(); i++) {
                String toId = msg.get(i).getToId();
                String pid = msg.get(i).getPid();
                SocketIOClient clients = mClientMap.get(toId);
                if (clients != null) {
                    sendChatMessage(clients, msg.get(i));
                } else {
                    String json = GsonUtil.BeanToJson(msg.get(i));
                    socketService.getBroadcastOperations().sendEvent("chat", json);
                }
                Boolean b = chatService.removeMsg(pid);
                if (b) {
                    System.out.println("-------删除离线消息成功--------" + pid);
                }
            }
        }
    }

    /**
     * 广播上线,,,,下线
     *
     * @param id
     * @param type
     */
    private void sendOnLineASK(String id, int type) {
        ChatMessage message = GsonUtil.chatOnLine(id, type);
        String json = GsonUtil.BeanToJson(message);
        socketService.getBroadcastOperations().sendEvent("chat", json);
    }

    /**
     * 客户端断开连接监听
     */
    class SocketDisConnectListener implements DisconnectListener {

        @Override
        public void onDisconnect(SocketIOClient socketIOClient) {
            HashMap<String, String[]> map = new HashMap<>();
            System.out.println("---------客户端断开连接------");
            String client = getClientUid(socketIOClient);
            SocketIOClient ioClient = mClientMap.get(client);
            if (ioClient != null) {
                ioClient.disconnect();
            }
            mClientMap.remove(client);
            System.out.println("---------当前连接人数-------" + mClientMap.size());
            map.put("uid", new String[]{client});
            map.put("online", new String[]{"0"});
            sqlService.updateUser(map);
            sendOnLineASK(client, 0);
        }
    }

    /**
     * 消息监听
     */
    class SocketDataListener implements DataListener<String> {

        @Override
        public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception {
            System.out.println("监听到消息========" + s);
            ChatMessage chatMessage = GsonUtil.GsonToBean(s, ChatMessage.class);
            handleChatMessage(chatMessage, ackRequest);
            historyService.createTable(chatMessage.getFromId());
            historyService.createTable(chatMessage.getToId());
            historyService.insetData(chatMessage,chatMessage.getFromId());
            historyService.insetData(chatMessage,chatMessage.getToId());
        }
    }

    private void handleChatMessage(ChatMessage s, AckRequest ackRequest) {
        String to_id = s.getToId();
        SocketIOClient client = mClientMap.get(to_id);
        s.setMsgStatus(2);
        if (client != null) {
            sendChatMessage(client, s);
        } else {
            // 存入离线消息表
            Boolean aBoolean = chatService.insertChatMessage(s);
            if (aBoolean) {
                System.out.println("-------添加离线消息成功--------");
            }
        }
        ackRequest.sendAckData(GsonUtil.BeanToJson(s));
    }

    private void sendChatMessage(SocketIOClient client, ChatMessage s) {
        String json = GsonUtil.BeanToJson(s);
        System.out.println("回调消息========" + json);
        client.sendEvent("chat", json);
    }

    String getClientUid(SocketIOClient client) {
        String uid = client.getHandshakeData().getSingleUrlParam("uid");
        if (uid == null || uid == "") {
            return null;
        }
        return uid;
    }

}
