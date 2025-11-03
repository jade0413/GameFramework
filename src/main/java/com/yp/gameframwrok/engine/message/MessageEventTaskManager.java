package com.yp.gameframwrok.engine.message;

import java.io.Serializable;

public class MessageEventTaskManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int ARRAY_LENGTH = 1;
    private final MessageEventTask[] messageEventTaskGroup = new MessageEventTask[ARRAY_LENGTH];

    public MessageEventTaskManager(MessageManager messageManager) {
        for (int i = 0; i < ARRAY_LENGTH; i++) {
//            messageEventTaskGroup[i] = new MessageEventTask(messageManager);
        }
    }

    public MessageEventTask get(long id) {
        return messageEventTaskGroup[(int) (id % ARRAY_LENGTH)];
    }
}
