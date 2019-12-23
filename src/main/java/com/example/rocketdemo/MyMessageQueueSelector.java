package com.example.rocketdemo;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

public class MyMessageQueueSelector implements MessageQueueSelector {
    public MessageQueue select(List<MessageQueue> mqs, Message msg,
                               Object orderKey) {
        int id = Integer.parseInt(orderKey.toString());
        return mqs.get(id);
    }
}
