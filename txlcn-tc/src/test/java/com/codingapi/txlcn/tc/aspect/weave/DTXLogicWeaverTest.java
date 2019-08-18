package com.codingapi.txlcn.tc.aspect.weave;

import com.codingapi.txlcn.tc.aspect.DTXInfo;
import com.codingapi.txlcn.tc.core.DTXLocalContext;
import com.codingapi.txlcn.tc.core.DTXServiceExecutor;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.context.TxContext;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author lorne
 * @date 2019-08-17
 * @description
 */

@SpringBootTest(classes = {DTXLogicWeaver.class})
@RunWith(SpringRunner.class)
@Slf4j
public class DTXLogicWeaverTest {

    @MockBean
    private DTXServiceExecutor dtxServiceExecutor;

    @MockBean
    private TCGlobalContext tcGlobalContext;

    @MockBean
    private DTXInfo dtxInfo;

    @Autowired
    private DTXLogicWeaver dtxLogicWeaver;



    @Test
    public void runTransaction_start() {
        //Mock TxContext Start
        TxContext txContext = new TxContext();
        // 事务发起方判断
        txContext.setDtxStart(!TracingContext.tracing().hasGroup());
        if (txContext.isDtxStart()) {
            TracingContext.tracing().beginTransactionGroup();
        }
        txContext.setGroupId(TracingContext.tracing().groupId());
        Mockito.when(tcGlobalContext.startTx()).thenReturn(txContext);


        try {
            int res = (int)dtxLogicWeaver.runTransaction(dtxInfo,()->{
                return 10;
            });
            Assert.assertTrue("return values was error.",res==10);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    @Test
    public void runTransaction_on() {
        TxContext txContext = new TxContext();
        txContext.setDtxStart(!TracingContext.tracing().hasGroup());
        if (txContext.isDtxStart()) {
            TracingContext.tracing().beginTransactionGroup();
        }
        txContext.setGroupId(TracingContext.tracing().groupId());
        Mockito.when(tcGlobalContext.txContext()).thenReturn(txContext);
        Mockito.when(tcGlobalContext.hasTxContext()).thenReturn(true);

        try {
            int res = (int)dtxLogicWeaver.runTransaction(dtxInfo,()->{
                return 20;
            });
            Assert.assertTrue("return values was error.",res==20);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    @Test
    public void runTransaction_return() {
        DTXLocalContext.getOrNew();

        try {
            int res = (int)dtxLogicWeaver.runTransaction(dtxInfo,()->{
                return 30;
            });
            Assert.assertTrue("return values was error.",res==30);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }



    @Test
    public void runTransaction_local() {
        //Mock TxContext Start
        TxContext txContext = new TxContext();
        // 事务发起方判断
        txContext.setDtxStart(!TracingContext.tracing().hasGroup());
        if (txContext.isDtxStart()) {
            TracingContext.tracing().beginTransactionGroup();
        }
        txContext.setGroupId(TracingContext.tracing().groupId());
        Mockito.when(tcGlobalContext.startTx()).thenReturn(txContext);


        try {
            Mockito.when(dtxServiceExecutor.transactionRunning(Mockito.any())).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    int res = (int)dtxLogicWeaver.runTransaction(dtxInfo,()->{
                        return 41;
                    });
                    Assert.assertTrue("return values was error.",res==41);
                    return res;
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        try {
            int res = (int)dtxLogicWeaver.runTransaction(dtxInfo,()->{
                return 40;
            });

            Assert.assertTrue("return values was error.",res==41);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
