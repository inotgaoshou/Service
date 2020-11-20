package com.service.zgbj.im;

/**
 * @author yubin
 * @version 1.0.0
 * @ClassName MessageType.java
 * @Description TODO
 * @createTime 2020年11月20日 23:14:00
 */
public class MessageType {


    // 消息类型 body type
    public static int MSG_BODY_TYPE_TEXT = 1;   // 文本
    public static int MSG_BODY_TYPE_IMAGE = 2;  // 图片
    public static int MSG_BODY_TYPE_VOICE = 3;  // 语音
    public static int MSG_BODY_TYPE_VOIDE = 4;  // 视频
    public static int MSG_BODY_TYPE_LOCATION = 5;//位置
    public static int MSG_BODY_TYPE_EMOJI = 6;  // 表情
    public static int MSG_BODY_TYPE_TEXT_HELLO = 7;   // 文本 hello 打招呼
    public static int MSG_BODY_TYPE_CANCEL = 8; // 撤回
    public static int MSG_BODY_TYPE_GIF = 9; // 斗图

    // 消息状态 message status
    public static int MSG_SEND_LOADING = 1;  // 正在发送
    public static int MSG_SEND_SUCCESS = 2;  // 成功
    public static int MSG_SEND_ERROR = 3;    // 失败
    public static int MSG_VOICE_UNREAD = 4;  // 语音 未读

    // 斗图 类型
    public static int MSG_GIF_GIF = 5;      // 斗图 =  动图
    public static int MSG_GIF_IMAGE = 6;    // 斗图 = 静态图

    // 消息事件
    public static int MSG_SEND_CHAT = 1;   // 聊天 1v1
    public static int MSG_SEND_SYS = 2;    // 上线/下线 通知
    public static int MSG_OFFLINE = 3;    // 其他设备登录
    public static int MSG_ADD_FRIEND = 4; // 请求添加好友

    //消息种类 type
    public static int MSG_SINGLE_CHAT = 1;   //  1v1消息
    public static int MSG_ROOM_CHAT = 2;    // 房间消息
    public static int MSG_GROUP_CHAT = 3;    // 群消息

    // 是否显示时间
    public static int MSG_TIME_FALSE = 0;
    public static int MSG_TIME_TRUE = 1;

    private String fromId;
    private String toId;
    private String pid;
    private int bodyType;
    private String body;
    private int msgStatus;
    private Long time;
    private int type = MSG_SEND_CHAT;
    private String conversation;
    private int displaytime = MSG_TIME_FALSE;

    public int getDisplaytime() {
        return displaytime;
    }

    public void setDisplaytime(int displaytime) {
        this.displaytime = displaytime;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}