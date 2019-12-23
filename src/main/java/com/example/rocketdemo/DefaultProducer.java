package com.example.rocketdemo;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;

public class DefaultProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        // 普通生产者
        DefaultMQProducer producer = new DefaultMQProducer("producerGroup");

        // 多个 ; 分割
        producer.setNamesrvAddr("192.168.1.130:9876;192.168.1.133:9876");
        producer.setInstanceName("producerOne");

        // 3.2.6的版本没有该设置，在更新或者最新的版本中务必将其设置为false，否则会有问题
        // producer.setVipChannelEnabled(false);

        // 发送失败的重试次数
        producer.setRetryTimesWhenSendAsyncFailed(3);
        producer.start();

//        producer.createTopic("TopicTestOne","CreateTopic",8);

        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("TopicTestOne",// topic
                        "TagA",// tag
                        ("Hello RocketMQ " + i).getBytes("utf-8")// body
                );

                msg.putUserProperty("a", "sun");  // 设置消息属性 用于消息过滤 非必须
                msg.setKeys("TestKeyOne"); // 设置消息的KEY 非必须
                //延迟信息
                //(1s/5s/1Os/30s/Im/2m/3m/4m/5m/6m/ 7m/8m/9m/1Om/20m/30m/1h/2h)
//                msg.setDelayTimeLevel(4);

                // 1.同步方式写
                SendResult sendResult = producer.send(msg);
                // 2.异步方式写
//                SendResult sendResult = producer.send(msg, new SendCallback() {
//                    @Override
//                    public void onSuccess(SendResult sendResult) {
//                        System.out.println("发送成功！");
//                    }
//
//                    @Override
//                    public void onException(Throwable e) {
//                        System.out.println("发送异常！");
//                    }
//                });
                // 3.不等返回结果方式
//                SendResult sendResult = producer.sendOneway(msg);
//                设置发送给某个MessageQueue
//                MessageQueueSelector mySelector = new MyMessageQueueSelector();
//                SendResult sendResult = producer.send(msg, mySelector , 1);
                //FLUSH DISK TIMEOUT : 表示没有在规定时间内完成刷盘(需要 Broker 的刷盘策设置成 SYNC FLUSH 才会报这个错误) 。
                //FLUSH SLAVE TIMEOUT :表示在主备方式下，并且 Broker被设置成SYNC_MASTER方式，没有在设定时间内完成主从同步 。
                //SLAVE NOT AVAILABLE : 这个状态 产生的场景和 FLUSH SLAVE TIMEOUT 类似，
                //                  表示在主备方式下，并且Broker被设置成 SYNC MASTER，但是没有找到被配置成 Slave 的 Broker。
                //SEND OK :表示发送成功。
                System.out.println(sendResult.getSendStatus()); //发送结果状态
                //打印返回结果，可以看到消息发送的状态以及一些相关信息
                System.out.println(sendResult);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }

        //发送完消息之后，调用shutdown()方法关闭producer
        producer.shutdown();
    }
}