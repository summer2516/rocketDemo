package com.example.rocketfull;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

public class Producer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        //声明并初始化一个producer
        //需要一个producer group名字作为构造方法的参数，这里为producer1
        DefaultMQProducer producer = new DefaultMQProducer("producer1");

        //设置NameServer地址,此处应改为实际NameServer地址，多个地址之间用；分隔
        //NameServer的地址必须有，但是也可以通过环境变量的方式设置，不一定非得写死在代码里
//        producer.setNamesrvAddr("192.168.1.126:9876;192.168.140.129:9876");
        producer.setNamesrvAddr("192.168.31.120:9876");
        producer.setInstanceName("test11111");
//        producer.setsen
//        sh mqadmin updateTopic -n 192.168.1.126:9876 -b 192.168.1.126:10911 -t TopicTest
        //     producer.setVipChannelEnabled(false);//3.2。6的版本没有该设置，在更新或者最新的版本中务必将其设置为false，否则会有问题
        //调用start()方法启动一个producer实例
        producer.start();
//sh mqadmin updateTopic -n 192.168.1.126:9876 -b 192.168.1.150:10911 -t orderTopic
        //发送10条消息到Topic为TopicTest，tag为TagA，消息内容为“Hello RocketMQ”拼接上i的值
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("TopicTest",// topic
                        "TagA",// tag
                        ("Hello RocketMQ " + i).getBytes("utf-8")// body
                );


//                ( 1s/5s/1Os/30s/Im/2m/3m/4m/5m/6m/ 7m/8m/9m/1Om/20m/30m/1h/2h)
                msg.setDelayTimeLevel(1);

                //调用producer的send()方法发送消息
                //这里调用的是同步的方式，所以会有返回结果
                SendResult sendResult = producer.send(msg);
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