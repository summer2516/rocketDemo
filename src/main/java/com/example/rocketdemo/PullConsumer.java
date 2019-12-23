/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.rocketdemo;

import java.util.*;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

public class PullConsumer {
    private static final Map<MessageQueue, Long> OFFSE_TABLE = new HashMap<MessageQueue, Long>();

    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("consumerGroupPull");

        consumer.setNamesrvAddr("192.168.1.130:9876");
        consumer.start();
//        拉取消息  逐个读取TopicTestOne 下所有队列的消息
//        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("TopicTestOne");
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("TopicTestTran");
//        遍历消息队列
        for (MessageQueue mq : mqs) {
            System.out.printf("Consume from the queue: %s%n", mq);
            SINGLE_MQ:
            while (true) {
                try {
//                    从队列里指定的Offset处开始 获取messageList
                    PullResult pullResult =
                            consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);
                    System.out.printf("%s%n", pullResult);
//                    保存Offest数据
                    putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
                    switch (pullResult.getPullStatus()) {
                        case FOUND:   // 有message
                            List<MessageExt> msgFoundList = pullResult.getMsgFoundList();
                            for (MessageExt messageExt : msgFoundList){
                                System.out.println("消费响应：msgId : " + messageExt.getMsgId() + ",  msgBody : " + new String(messageExt.getBody()) + ", QueueId :" + messageExt.getQueueId());
                            }
                            break;
                        case NO_MATCHED_MSG:  // 没有匹配到消息
                            System.out.println("没有匹配到消息");
                            break;
                        case NO_NEW_MSG:   // 没有信息消息
                            System.out.println("没有新消息");
                            break SINGLE_MQ;
                        case OFFSET_ILLEGAL:  // offset 不合法
                            System.out.println("Offset不合法");
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        consumer.shutdown();
    }

    private static long getMessageQueueOffset(MessageQueue mq) {
        Long offset = OFFSE_TABLE.get(mq);
        if (offset != null)
            return offset;

        return 0;
    }

    private static void putMessageQueueOffset(MessageQueue mq, long offset) {
        OFFSE_TABLE.put(mq, offset);
    }

}
