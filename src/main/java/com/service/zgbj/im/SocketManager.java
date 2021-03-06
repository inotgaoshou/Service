package com.service.zgbj.im;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.service.zgbj.mysqlTab.impl.ChatServiceImpl;
import com.service.zgbj.mysqlTab.impl.HistoryServiceImpl;
import com.service.zgbj.mysqlTab.impl.UserServiceImpl;
import com.service.zgbj.utils.DateUtils;
import com.service.zgbj.utils.GsonUtil;
import com.service.zgbj.utils.OfTenUtils;

import java.util.HashMap;
import java.util.List;

public class SocketManager {


    public static AckRequest mAckRequest;
    //Socket客户端容器
    public static HashMap<String, HashMap<String, SocketIOClient>> mClientMap = new HashMap<>();

    private SocketConnectListener connectListener;
    private SocketDisConnectListener disConnectListener;
    private SocketDataListener chatListener;
    private UserServiceImpl sqlService;
    private static ChatServiceImpl chatService;
    public static SocketIOServer socketService;
    private HistoryServiceImpl historyService;


    public SocketManager(SocketIOServer server, UserServiceImpl userService,
                         ChatServiceImpl chatServiceImp, HistoryServiceImpl historyServiceImpl) {

        this.sqlService = userService;
        this.chatService = chatServiceImp;
        this.historyService = historyServiceImpl;

        if (server != null) {
            this.socketService = server;
            connectListener = new SocketConnectListener();
            disConnectListener = new SocketDisConnectListener();
            chatListener = new SocketDataListener();
            server.addConnectListener(connectListener);
            server.addDisconnectListener(disConnectListener);
            server.addEventListener("chat", String.class, chatListener);
        }
    }

    public static SocketIOServer getService() {
        return socketService;
    }


    /**
     * 客户端连接监听
     */
    class SocketConnectListener implements ConnectListener {

        @Override
        public void onConnect(SocketIOClient socketIOClient) {
            SocketBean socketInfo = getClientInfo(socketIOClient);
            System.out.println("======token=====" + socketInfo.getToken());
            System.out.println("======uid=====" + socketInfo.getUid());
            System.out.println("======mobile=====" + socketInfo.getMobile());
            System.out.println("======desc=====" + socketInfo.getDesc());
            System.out.println("======netCode=====" + socketInfo.getOnline());
            System.out.println("--------客户端连接------");

            Boolean online = chatService.whereIsOnline(socketInfo.getUid());
            System.out.println("是否存在:" + online);
            if (online) {
                String token = chatService.getToken(socketInfo.getUid());
                if (!token.isEmpty() && !token.equals(socketInfo.getToken())) {
                    HashMap<String, SocketIOClient> clientHashMap = mClientMap.get(socketInfo.getUid());
                    SocketIOClient ioClient = clientHashMap.get(token);
                    if (ioClient != null) {
                        String time = DateUtils.getCurrentTime();
                        ChatMessage message = new ChatMessage();
                        message.setType(3);
                        message.setConversation("abcdefgl123456789");
                        message.setBody("你的账号于" + time + "在另一台" + socketInfo.getDesc() + "手机登录了Learn。如非本人操作,则密码可能一泄漏,建议修改密码。");
                        String toJson = GsonUtil.BeanToJson(message);
                        ioClient.sendEvent("chat", toJson);
                    }
                    chatService.updateToken(socketInfo.getToken(), socketInfo.getUid());
                }
            } else {
                chatService.insert(socketInfo);
            }
            HashMap<String, SocketIOClient> hashMap = new HashMap<>();
            hashMap.put(socketInfo.getToken(), socketIOClient);
            mClientMap.put(socketInfo.getUid(), hashMap);

            // 改变自己在线状态
            HashMap<String, String[]> map = new HashMap<>();
            map.put("uid", new String[]{socketInfo.getUid()});
            map.put("online", new String[]{socketInfo.getOnline()});
            sqlService.updateUser(map);
            // 查询我所有的好友
            String json = sqlService.getAllFriend(socketInfo.getUid());
            FriendAllBean bean = GsonUtil.GsonToBean(json, FriendAllBean.class);
            List<FriendAllBean.DataBean> data = bean.getData();
            // 查看我的好友是否在线 在线的通知他们我上线了
            // 遍历我的好友给我的备注
            try {
                for (int i = 0; i < data.size(); i++) {
                    FriendAllBean.DataBean dataBean = data.get(i);
                    //  > 0 在线
                    int net = dataBean.getOnline();
                    HashMap<String, SocketIOClient> mapToken = mClientMap.get(dataBean.getUid());
                    if (net > 0) {
                        String info = sqlService.getFriendUserInfo(dataBean.getUid(), socketInfo.getUid());
                        FriendBean userBean = GsonUtil.GsonToBean(info, FriendBean.class);
                        int code = userBean.getCode();
                        if (code > 0) {
                            ChatMessage message = GsonUtil.chatOnLine(userBean.getData());
                            String token = chatService.getToken(dataBean.getUid());
                            if (!token.isEmpty()) {
                                SocketIOClient ct = mapToken.get(token);
                                if (ct != null) {
                                    ct.sendEvent("chat", GsonUtil.BeanToJson(message));
                                }
                            }

                        }
                    }
                }
            } catch (Exception e) {

            }
            System.out.println("-------当前连接人数--------" + (mClientMap.size()));
            sendChatMessage(socketInfo.getUid());
        }
    }


    /**
     * 客户端断开连接监听
     */
    class SocketDisConnectListener implements DisconnectListener {

        @Override
        public void onDisconnect(SocketIOClient socketIOClient) {

            HashMap<String, String[]> map = new HashMap<>();
            System.out.println("---------客户端断开连接------");
            SocketBean socketInfo = getClientInfo(socketIOClient);
            System.out.println("======token=====" + socketInfo.getToken());
            System.out.println("======uid=====" + socketInfo.getUid());
            System.out.println("======mobile=====" + socketInfo.getMobile());
            System.out.println("======desc=====" + socketInfo.getDesc());
            System.out.println("======netCode=====" + socketInfo.getOnline());

            try {
                String json = sqlService.getAllFriend(socketInfo.getUid());
                FriendAllBean bean = GsonUtil.GsonToBean(json, FriendAllBean.class);
                List<FriendAllBean.DataBean> data = bean.getData();
                for (int i = 0; i < data.size(); i++) {
                    FriendAllBean.DataBean dataBean = data.get(i);
                    int online = dataBean.getOnline();
                    if (online > 0) {
                        String info = sqlService.getFriendUserInfo(dataBean.getUid(), socketInfo.getUid());
                        FriendBean userBean = GsonUtil.GsonToBean(info, FriendBean.class);
                        HashMap<String, SocketIOClient> clientHashMap = mClientMap.get(dataBean.getUid());
                        String token = chatService.getToken(dataBean.getUid());
                        if (!token.isEmpty()) {
                            SocketIOClient ct = clientHashMap.get(token);
                            ChatMessage message = GsonUtil.chatOnLine(userBean.getData());
                            if (ct != null) {
                                ct.sendEvent("chat", GsonUtil.BeanToJson(message));
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }

            String token = chatService.getToken(socketInfo.getUid());
            if (token.equals(socketInfo.getToken())) {
                map.put("uid", new String[]{socketInfo.getUid()});
                map.put("online", new String[]{"0"});
            }
            sqlService.updateUser(map);
            chatService.delete(socketInfo.getToken());
            mClientMap.remove(socketInfo.getUid());
            System.out.println("---------当前连接人数-------" + mClientMap.size());
        }
    }

    /**
     * 消息监听
     */
    class SocketDataListener implements DataListener<String> {

        @Override
        public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception {
            System.out.println("监听到消息========" + s);
            mAckRequest = ackRequest;
            ChatMessage chatMessage = GsonUtil.GsonToBean(s, ChatMessage.class);
            handleChatMessage(chatMessage, ackRequest);
            historyService.createTable(OfTenUtils.replace(chatMessage.getFromId()));
            historyService.createTable(OfTenUtils.replace(chatMessage.getToId()));
            historyService.insetData(chatMessage, chatMessage.getFromId());
            if (chatMessage.getBodyType() == 3) {
                chatMessage.setMsgStatus(4);
            }
            historyService.insetData(chatMessage, chatMessage.getToId());
        }
    }

    private void handleChatMessage(ChatMessage s, AckRequest ackRequest) {
        String to_id = s.getToId();
        int bodyType = s.getBodyType();
        int status = s.getMsgStatus();
        String token = chatService.getToken(to_id);
        if (token != null && !token.isEmpty()) {
            HashMap<String, SocketIOClient> map = mClientMap.get(to_id);
            SocketIOClient client = map.get(token);
            addRedEnvelopeMsg(bodyType, status, s);
            s.setMsgStatus(2);
            if (client != null) {
                sendChatMessage(client, s);
            } else {
                addLineMsg(s);
            }
        } else {
            addLineMsg(s);
        }
        ackRequest.sendAckData(GsonUtil.BeanToJson(s));
    }

    private void addRedEnvelopeMsg(int bodyType, int status, ChatMessage s) {
        if (bodyType == 7) {
            if (status == 1) {
                RedEnvelopeBean bean = new RedEnvelopeBean();
                bean.setFromId(s.getFromId());
                bean.setToId(s.getToId());
                bean.setPid(s.getPid());
                bean.setStatus(RedEnvelopeBean.STATUS_UNCLAIMED);
                bean.setTime(s.getTime());
                bean.setBody(s.getBody());
                bean.setConversation(s.getConversation());
                chatService.addRedEnvelope(bean);
            }
        }
    }

    // 存入离线消息表
    public static void addLineMsg(ChatMessage s) {
        s.setMsgStatus(2);
        Boolean aBoolean = chatService.insertChatMessage(s);
        if (aBoolean) {
            System.out.println("-------添加离线消息成功--------");
        }
    }

    public static void sendChatMessage(SocketIOClient client, ChatMessage s) {
        String json = GsonUtil.BeanToJson(s);
        System.out.println("发送消息========" + json);
        client.sendEvent("chat", json);
    }

    public void sendChatMessage(String client) {
        List<ChatMessage> msg = chatService.getOffLineMsg(client);
        if (msg.size() > 0) {
            for (int i = 0; i < msg.size(); i++) {
                String toId = msg.get(i).getToId();
                String pid = msg.get(i).getPid();
                HashMap<String, SocketIOClient> map = mClientMap.get(toId);
                SocketIOClient clients = map.get(chatService.getToken(toId));
                ChatMessage chatMessage = msg.get(i);
                chatMessage.setMsgStatus(2);
                if (clients != null) {
                    sendChatMessage(clients, chatMessage);
                    Boolean b = chatService.removeMsg(pid);
                    if (b) {
                        System.out.println("-------删除离线消息成功--------" + pid);
                    }
                }

            }
        }
    }

    String getClientUid(SocketIOClient client) {
        String uid = client.getHandshakeData().getSingleUrlParam("uid");
        if (uid == null || uid == "") {
            return null;
        }
        return uid;
    }

    SocketBean getClientInfo(SocketIOClient client) {
        SocketBean socketBean = new SocketBean();
        String desc = client.getHandshakeData().getSingleUrlParam("desc");
        String token = client.getHandshakeData().getSingleUrlParam("token");
        String uid = client.getHandshakeData().getSingleUrlParam("uid");
        String mobile = client.getHandshakeData().getSingleUrlParam("mobile");
        String netCode = client.getHandshakeData().getSingleUrlParam("online");
        socketBean.setMobile(mobile);
        socketBean.setUid(uid);
        socketBean.setToken(token);
        socketBean.setDesc(desc);
        socketBean.setOnline(netCode);
        return socketBean;
    }

}
