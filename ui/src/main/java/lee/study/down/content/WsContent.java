package lee.study.down.content;

import com.alibaba.fastjson.JSON;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.model.HttpDownInfo;
import lee.study.down.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WsContent {

  private final static Logger LOGGER = LoggerFactory.getLogger(WsContent.class);
  //websocket对象管理
  private static Map<String, WebSocketSession> wcContent;

  public void put(String id, WebSocketSession session) {
    wcContent.put(id, session);
  }

  public void remove(String id) {
    wcContent.remove(id);
  }

  public void sendMsg() {
    try {
      List<TaskInfo> taskInfos = new LinkedList<>();
      for (HttpDownInfo httpDownInfo : ContentManager.DOWN.getDownInfos()) {
        if (httpDownInfo.getTaskInfo().getStatus() != HttpDownStatus.WAIT) {
          taskInfos.add(httpDownInfo.getTaskInfo());
        }
      }
      TextMessage message = new TextMessage(JSON.toJSONString(taskInfos));
      for (Entry<String, WebSocketSession> entry : wcContent.entrySet()) {
        WebSocketSession session = entry.getValue();
        if (session.isOpen()) {
          synchronized (session) {
            session.sendMessage(message);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("sendMsg", e);
    }
  }

  public void init() {
    wcContent = new ConcurrentHashMap<>();
  }
}
