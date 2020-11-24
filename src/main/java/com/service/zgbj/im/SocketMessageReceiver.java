package com.service.zgbj.im;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.service.zgbj.constant.Constant;
import com.service.zgbj.redis.JedisLockService;
import com.service.zgbj.redis.JedisService;
import com.service.zgbj.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;


@Slf4j
@Component
public class SocketMessageReceiver {

    @Resource(name = "bizExecutor")
    private Executor bizExecutor;

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    protected JedisService jedisService;
    @Autowired
    protected JedisLockService jedisLockService;

    public void receiveMessage(String message) {
        log.info("receive socket push message, message = {}", message);
        try {
            ChatMessage chatMessage = GsonUtil.GsonToBean(message, ChatMessage.class);
            if(chatMessage.getType() == MessageType.MSG_SINGLE_CHAT){
                HashMap<String, SocketIOClient> map = SocketManager.mClientMap.get(chatMessage.getToId());
                if(map != null){
                    SocketIOClient client = map.get(getToken(chatMessage.getToId()));
                    if(client != null){
                        SocketManager.sendChatMessage(client, chatMessage);
                    }else{
                        //添加分布式锁
//                        SocketManager.addLineMsg(chatMessage);
                    }
                }

            }else if(chatMessage.getType() == MessageType.MSG_ROOM_CHAT){

            }
        } catch (Exception e) {
            log.error("receive  socket push message error, message = {}", message, e);
        }
    }




    /**
     * @description 发送房间内的事件消息
     * @author
     */
    public void sendRoomMsg(Long roomUid, String event, String json) {
        log.info("sendRoomMsg, room={}, event={}, message={}", roomUid, event, json);
        socketIOServer.getRoomOperations(roomUid.toString()).sendEvent(event, json);
    }


    public String getToken(String uid) {
        List<Map<String, Object>> map;
        try {
            String sql = "SELECT * FROM table_socket WHERE uid = " + "'" + uid + "'";
            System.out.println(sql);
            map = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            map = null;
            System.out.println("getToken:" + e.toString());
        }
        if (map != null && map.size() > 0) {
            String token = map.get(0).get("token").toString();
            System.out.println("token:" + token);
            return token;
        }
        return "";
    }

}
