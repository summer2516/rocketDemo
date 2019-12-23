#!/bin/bash
# 1.启动命令 nameserver
nohup ./mqnamesrv  &

# 2.启动命令 broker  -n nameserver地址 多个：192.168.1.120:9876;192.168.1.121:9876
#autoCreateTopicEnable=true 自动创建Topic  -c config_file 指定配置文件
nohup ./mqbroker -n localhost:9876 autoC:qreateTopicEnable=true  &

# 3.停止 broker  nameserver
sh ./mqshutdown broker
sh ./mqshutdown namesrv

# 4.创建/修改Topic  -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥）   -t topic名称  必填
#          -r 可读队列数  -w 可写队列数    -p 指定新 Topic 的权限限制，(2[4[6), [2:W 4:R; 6:RW]
sh mqadmin updateTopic -n 192.168.1.130:9876 -c DefaultCluster -t TopicTestTran
# 5.删除Topic  -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥）   -t topic名称  必填
sh mqadmin deleteTopic -n 192.168.1.120:9876 -b 192.168.1.120:10911 -t TopicTestOne

# 6.创建/修改订阅组 指的是同一类消费者，当同一类消费者使用Clustering模式消费时，会提高效率  订阅组创建时是创建consumer是创建
#    -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥） -g 订阅组名称   必填
#    非必填   -d -i  -m -q -r -s -w -h  太多了就不说了。。。
sh mqadmin updatesubGroup -n 192.168.1.120:9876 -b 192.168.1.120:10911

# 7.删除订阅组
#  -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥） -g 订阅组名称   必填
sh mqadmin deleteSubGroup -n 192.168.1.120:9876 -b 192.168.1.120:10911

# 8.更新Broker配置信息
#  -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥）   -k Key值 必填
sh mqadmin updateBrokerConfig -n 192.168.1.120:9876 -b 192.168.1.120:10911

# 9.查询Topic的路由信息  -n  -t 必填
sh mqadmin TopicRoute -n 192.168.1.120:9876  -t TopicTestOne

# 10.查看某一个Topic的消费信息
sh mqadmin TopicStats -n 192.168.1.120:9876  -t TopicTestOne

# 11.topic列表信息
sh mqadmin TopicList -n 192.168.1.120:9876

# 12.更新Topic的读写权限
#  -n 指定nameserver地址   -b 指定broker地址  （-c borker集群名称）  （-b -c 互斥） -t topic -p  必填
sh mqadmin updateTopicPerm -n 192.168.1.120:9876 -b 192.168.1.120:10911 -t TopicTestOne

# 13.根据时间查询message  -n
# -b  开始时间：格式时间戳或yyyy-MM-dd#HH:mm:ss:SSS  -d 结束时间  -t Topic  -s Tag  TagA||TagB
sh mqadmin printMsg -n 192.168.1.120:9876 -b 2020-02-02#23:23:01:123 -d 2020-02-02#23:32:01:123  -t TopicTestOne -s TagA

# 14.根据MessageID查询  -n -i 消息ID
sh mqadmin queryMsgByld -n 192.168.1.120:9876 -i MessageID

# 15.查看集群信息 -n  -m 打印更多信息
sh mqadmin clusterList -n 192.168.1.120:9876 -m true

# 16.启动rocker-console        192.168.1.123:9876;192.168.1.136:9876
#java -jar rocketmq-console-ng-1.0.0.jar --server.port=12581 --rocketmq.config.namesrvAddr=192.168.1.120:9876
java -jar rocketmq-console-ng-1.0.0.jar --server.port=12581

# 汇总消息的
cat test|awk -F ' ' '{print $10}'|sort |uniq -c
