package com.example.rocketdemo;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

public class TransactionProducer {

//   1 ) 发送方向 RocketMQ 发送“待确认”消息 。
//   2 ) RocketMQ将收到的“待确认”消息持久化成功后，向发送方回复消息已经发送成功，此时第一阶段消息发送完成 。
//   3 ) 发送方开始执行本地事件逻辑 。
//   4 ) 发送方根据本地事件执行结果向RocketMQ发送二次确认( Commit或 是 Rollback) 消息,
//            RocketMQ收到Commit状态则将第一阶段消息标记为可投递，订阅方将能够收到该消息;
//            收到Rollback状态则删除第一阶段的消息，订阅方接收不到该消息。
//   5 ) 如果出现异常情况，步骤 4 )提交的二次确认最终未到达 RocketMQ,
//            服务器在经过固定时间段后将对“待确认”消息、发起回查请求。
//   6 ) 发送方收到消息回查请求后通过检查对应消息的本地事件执行结果返回Commit或Roolback状态 。
//   7 ) RocketMQ 收到回 查请求后，按照步骤 4 ) 的逻辑处理 。

    public static void main(String[] args) throws MQClientException, InterruptedException {
        // 事务消息生产者
        TransactionMQProducer producer = new TransactionMQProducer("TranProducerGroup");
        producer.setNamesrvAddr("192.168.1.130:9876;192.168.1.133:9876");

        //服务器回调producer,检查本地事务分支成功还是失败
        producer.setTransactionCheckListener(new TransactionCheckListener() {
            @Override
            public LocalTransactionState checkLocalTransactionState(MessageExt messageExt) {
                System.out.println("state --" + new String(messageExt.getBody()));
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });

        TransactionExecuterImpl transactionExecuter = new TransactionExecuterImpl();
        producer.start();
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("TopicTestTran",// topic
                        "TagA",// tag
                        ("Hello RocketMQ " + i).getBytes("utf-8")// body
                );
                // 发送message 并得到发送返回结果
                SendResult sendResult = producer.sendMessageInTransaction(msg, transactionExecuter, "tq");

                System.out.println(sendResult.getSendStatus());
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
        producer.shutdown();
    }
}
