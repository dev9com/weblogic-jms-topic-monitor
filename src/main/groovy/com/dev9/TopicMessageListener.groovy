package com.dev9

import javax.jms.Message
import javax.jms.MessageListener

public class TopicMessageListener implements MessageListener {

    public void onMessage(Message message) {
        System.out.println(message.text)
    }
}
