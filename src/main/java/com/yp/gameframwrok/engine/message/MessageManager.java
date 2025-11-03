package com.yp.gameframwrok.engine.message;

import com.yp.gameframwrok.game.annotation.GameHandler;
import com.yp.gameframwrok.game.enums.EGameAction;
import com.yp.gameframwrok.game.enums.EGameType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Component
public class MessageManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 游戏对应的处理器
     */
    private final Map<EGameType, Map<EGameAction, IMessageHandler>> handlerMap;

    public MessageManager(List<IMessageHandler> handlers) {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        handler -> handler.getClass().getAnnotation(GameHandler.class).value(),
                        handler -> {
                            GameHandler annotation = handler.getClass().getAnnotation(GameHandler.class);
                            return Map.of(annotation.action(), handler);
                        }
                ));
    }

    public IMessageHandler getHandler(int gameType,int action) {
        return handlerMap.get(EGameType.valueOf(gameType)).get(EGameAction.valueOf(action));
    }
}
