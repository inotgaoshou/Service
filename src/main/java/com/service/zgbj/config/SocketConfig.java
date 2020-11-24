package com.service.zgbj.config;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SocketConfig {


    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.hostname}")
    private String hostname;

    @Value("${socketio.workCount}")
    private int workCount;

    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout}")
    private int pingTimeout;

    @Value("${socketio.pingInterval}")
    private int pingInterval;

    @Value("${socketio.maxFramePayloadLength}")
    private int maxFramePayloadLength;

    @Value("${socketio.maxHttpContentLength}")
    private int maxHttpContentLength;

    /**
     * 注册netty-socketio服务端
     */
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 重新启动端口被占用
        com.corundumstudio.socketio.SocketConfig socketConfig = config.getSocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        // 连接数大小
        config.setWorkerThreads(workCount);
        // 允许客户请求
        config.setAllowCustomRequests(allowCustomRequests);
        // 协议升级超时时间(毫秒)，默认10秒，HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(upgradeTimeout);
        // Ping消息超时时间(毫秒)，默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(pingTimeout);
        // Ping消息间隔(毫秒)，默认25秒。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(pingInterval);
        // 设置HTTP交互最大内容长度
        config.setMaxHttpContentLength(maxHttpContentLength);
        // 设置最大每帧处理数据的长度，防止他人利用大数据来攻击服务器
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setPort(port);
        if(StringUtils.isNotBlank(hostname)){
            config.setHostname(hostname);
        }
        log.info("SocketIOServer ====== {}",config.getHostname());
        config.setAuthorizationListener(new AuthorizationListener() {//类似过滤器
                                            @Override
                                            public boolean isAuthorized(HandshakeData data) {
                                                //http://localhost:8081?username=test&password=test
                                                //例如果使用上面的链接进行connect，可以使用如下代码获取用户密码信息，本文不做身份验证
                                                // String username = data.getSingleUrlParam("username");
                                                // String password = data.getSingleUrlParam("password");
                                                String uid = data.getSingleUrlParam("uid");
                                                log.info("SocketIOServer uid======{}" ,uid);
                                                return true;
                                            }
                                        }

        );
        final SocketIOServer server = new SocketIOServer(config);
        return server;
    }

    /**
     * tomcat启动时候，扫码socket服务器并注册
     *
     * @param socketServer
     * @return
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }
}