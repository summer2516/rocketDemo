package com.example.rocketdemo;

import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.message.Message;
/**
 * 执行本地事务，由客户端回调
 */

public class TransactionExecuterImpl implements LocalTransactionExecuter {

    @Override
    public LocalTransactionState executeLocalTransactionBranch(Message msg, Object arg) {
        System.out.println("msg=" + new String(msg.getBody()));
        System.out.println("arg = "+arg);
        String tag = msg.getTags();
        if (tag.equals("Transaction1")){
            //这里有一个分阶段提交的概念
            System.out.println("这里是处理业务逻辑，失败情况下进行ROLLBACK");
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
