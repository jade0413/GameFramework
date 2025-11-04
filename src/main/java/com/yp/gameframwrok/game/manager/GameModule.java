package com.yp.gameframwrok.game.manager;

import com.yp.gameframwrok.game.logic.VoidFunction;
import com.yp.gameframwrok.game.model.BaseRoom;
import com.yp.gameframwrok.game.model.BaseRoomConfig;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yyp
 */

@Log4j2
public abstract class GameModule<RC extends BaseRoomConfig,BR extends BaseRoom> {

    /**
     * 用户id到房间的映射
     */
    protected final Map<Long, BR> roomMaps = new ConcurrentHashMap<>();

    protected ScheduledExecutorService logicScheduledExecutorService;

    protected ScheduledExecutorService ioScheduledExecutorService;

    public GameModule(){
        logicScheduledExecutorService = Executors.newSingleThreadScheduledExecutor((run -> new Thread(run, "Logic Synchronized Thread")));
        ioScheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2 - 1, (run) -> new Thread(run, "IO Synchronized Thread"));
    }


    public void init() {
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1, run -> new Thread(run, "TableTimeOutThread"));
        //执行超时业务逻辑
        es.scheduleAtFixedRate(() -> {
            try {
                long cur = System.currentTimeMillis();
                Collection<BR> tables = roomMaps.values();
                tables.forEach(val -> {
                    if (val.isTimeOut(cur)) {
                        try {
                            val.timeOut();
                        } catch (Exception ex) {
                            log.error("超时业务异常,牌桌号[{}]", val.getRoomId(), ex);
//                            SlackAPI.get().send(ServiceManager.getNodeType(), CfgKeys.SLACK_NOTIFY_EXCEPTION, "超时业务异常", ex.getMessage());
                        }
                    }
                });
            } catch (Throwable e) {
                log.error("超时业务异常", e);
            }
        }, 3 * 1000, 200, TimeUnit.MILLISECONDS);
    }


    public void initRoom(){
    }

    /**
     * 异步业务逻辑处理， 无阻塞io操作，保证执行顺序，单线程处理
     */
    public void synTask(VoidFunction<BR> function, BR room, long mill) {
        logicScheduledExecutorService.schedule(() -> {
            try {
                function.apply(room);
            } catch (Exception throwable) {
                log.error("Syn Task Error,table Id [{}]", room != null ? room.getRoomId() : -1, throwable);
//                SlackAPI.get().send(ServiceManager.getNodeType(), CfgKeys.SLACK_NOTIFY_EXCEPTION, "Syn Task Error", throwable.getMessage());
            }
        }, mill, TimeUnit.MILLISECONDS);
    }

    public void synTask(VoidFunction<BR> function, BR table) {
        logicScheduledExecutorService.execute(() -> {
            try {
                function.apply(table);
            } catch (Exception throwable) {
                log.error("Syn Task Error,table Id [{}]", table != null ? table.getRoomId() : -1, throwable);
//                SlackAPI.get().send(ServiceManager.getNodeType(), CfgKeys.SLACK_NOTIFY_EXCEPTION, "Syn Task Error", throwable.getMessage());
            }
        });
    }

}
