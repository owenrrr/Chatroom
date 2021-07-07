package edu.conn.conn;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Owen
 * @Date: 2021/7/4 18:02
 * @Description:
 */
@ServerEndpoint(value = "/server/{userId}")
@Component
public class WebSocketServer {

    static Log log = LogFactory.getLog(WebSocketServer.class);
    private static int online_user = 0;
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId){
        this.session = session;
        this.userId = userId;
        if (webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        }else{
            webSocketMap.put(userId, this);
            addOnlineCount();
        }

        log.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());

        try{

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            subOnlineCount();
        }
        log.info("用户退出:"+userId+",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:"+userId+",报文:"+message);
        //可以群发消息
        //消息保存到数据库、redis
        if(message != null && !message.equals("")){
            try {
                //解析发送的报文
                JSONObject jsonObject = JSON.parseObject(message);
                //追加发送人(防止串改)
                jsonObject.put("fromUserId",this.userId);
                String toUserId=jsonObject.getString("toUserId");
                //传送给对应toUserId用户的websocket
                if(toUserId != null && !toUserId.equals("") && webSocketMap.containsKey(toUserId)){
                    webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
                }else{
                    log.error("请求的userId:"+toUserId+"不在该服务器上");
                    //否则不在这个服务器上，发送到mysql或者redis
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error){
        log.error("用户错误:"+this.userId+",原因:"+error.getMessage());
        error.printStackTrace();
    }

    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message,@PathParam("userId") String userId) throws IOException {
        log.info("发送消息到:"+userId+"，报文:"+message);
        if(userId != null && !userId.equals("") && webSocketMap.containsKey(userId)){
            webSocketMap.get(userId).sendMessage(message);
        }else{
            log.error("用户"+userId+",不在线！");
        }
    }

    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount(){
        return online_user;
    }

    public static synchronized void addOnlineCount(){
        online_user++;
    }

    public static synchronized void subOnlineCount(){
        online_user--;
    }

}
