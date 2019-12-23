package com.example.rocketdemo;

import com.example.rocketfull.Consumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.ConsumerData;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.io.IOException;
import java.util.List;

public class PushConsumer {
    //    https://www.aliyun.com/product/rocketmq?spm=5176.10695662.746107.1.6ecf531a4ucX80&aly_as=73cxp9D_
    public static void main(String[] args) throws MQClientException, IOException {

        // 负载均衡 到Queue层
        // doRebalance (新增和最初创建是触发)
        // 负载均衡的结果与 Topic 的 Message Queue 数量，以及ConsumerGroup里的Consumer的数量有关 。
        // PushConsumerd会自动负载均衡，并自动保存Offset（Message在队列里的偏移量），加入新的PushConsumer时会自动触发负载均衡规则。

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumerGroupPush");

        consumer.setNamesrvAddr("192.168.1.130:9876");

        //这里设置的是一个consumer的消费策略
        //CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
        //CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
        //CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认是半个小时以前
        //consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        // 设置从这个时刻开始消费
         consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
         consumer.setConsumeTimestamp("20201223171201");

        // 消费模式  CLUSTERING 集群模式   BROADCASTING  广播模式
        consumer.setMessageModel(MessageModel.CLUSTERING);

        // 设置一次消费消息的个数  默认是1
        consumer.setConsumeMessageBatchMaxSize(1);

        // consumer端负载均衡
        consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragelyByCircle());

        // 创建线程池的个数   主要通过 ProcessQueue 来控制
        consumer.setConsumeThreadMax(64);
        consumer.setConsumeThreadMin(20);

        // 设置consumer所订阅的Topic和Tag，* 或 null 代表全部的Tag
        consumer.subscribe("TopicTestOne", "*");
//        consumer.subscribe("TopicTest", "*");

        // sql 方式
         consumer.subscribe("TopicTest", MessageSelector.bySql("a = sun"));

        // 类加载机制 网络流
         String fileCode = MixAll.file2String("/Users/fwadmin/JobProject/rocketdemo/src/main/java/com/example/rocketdemo/MessageFilterImpl.java");
        //过滤器过滤方式
        consumer.subscribe("TopicTest", "com.example.rocketdemo.MessageFilterImpl",fileCode);

        //设置一个Listener，主要进行消息的逻辑处理
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                for (MessageExt messageExt : msgs) {
                    // 手动 丢弃消息
                    long queueOffset = messageExt.getQueueOffset();
                    String property = messageExt.getProperty(MessageConst.PROPERTY_MAX_OFFSET);
                    // 当某个队列的消息数堆积到 90000条以上，就直接丢弃，以便快速追上发送消息的进度 。
                    long diff = Long.parseLong(property) - queueOffset;
                    if (diff > 9000) {
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    // 正常消费逻辑
                    String messageBody = new String(messageExt.getBody());
                    System.out.println("消费响应：msgId : " + messageExt.getMsgId() + ",  msgBody : " + messageBody + ", QueueId :" + messageExt.getQueueId());
                }
                //返回消费状态
                //CONSUME_SUCCESS 消费成功
                //RECONSUME_LATER 消费失败，需要稍后重新消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("Consumer Started.");
    }
}