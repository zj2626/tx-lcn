package com.codingapi.txlcn.tc.core;

import com.codingapi.txlcn.common.exception.TransactionException;
import com.codingapi.txlcn.logger.TxLogger;
import com.codingapi.txlcn.logger.db.LogDbProperties;
import com.codingapi.txlcn.tc.core.context.TCGlobalContext;
import com.codingapi.txlcn.tc.core.context.TxContext;
import com.codingapi.txlcn.tc.core.propagation.DTXPropagationResolver;
import com.codingapi.txlcn.tc.support.TxLcnBeanHelper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

/**
 * @author lorne
 * @date 2019-08-18
 * @description
 */
@SpringBootTest(classes = {DTXServiceExecutor.class,TxLogger.class,LogDbProperties.class})
@RunWith(SpringRunner.class)
@Slf4j
public class DTXServiceExecutorTest {

    @MockBean
    private DTXPropagationResolver dtxPropagationResolver;

    @MockBean
    private TCGlobalContext tcGlobalContext;

    @MockBean
    private TxLcnBeanHelper txLcnBeanHelper;

    @MockBean
    private DTXLocalControl dtxLocalControl;

    @MockBean
    private TxLogger txLogger;


    @Autowired
    private DTXServiceExecutor dtxServiceExecutor;





    class TestMethodClass{

        public int val(){
            return 1;
        }

    }

    @Test
    public void transactionRunning() {
        final TestMethodClass testMethodClass = new TestMethodClass();
        TxTransactionInfo info = new TxTransactionInfo();
        info.setBusinessCallback(()->{
            return testMethodClass.val();
        });

        Class testClass = TestMethodClass.class;
        Method valMethod = null;
        try {
            valMethod = testClass.getMethod("val");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        info.setPointMethod(valMethod);

        info.setUnitId("unitId");
        info.setGroupId("groupId");

        info.setTransactionType("lcn");

        try {
            Mockito.when(dtxPropagationResolver.resolvePropagationState(Mockito.any())).thenReturn(DTXPropagationState.CREATE);
        } catch (TransactionException e) {
            e.printStackTrace();
        }

        Mockito.when(txLcnBeanHelper.loadDTXLocalControl(Mockito.eq("lcn"),Mockito.eq(DTXPropagationState.CREATE))).thenReturn(dtxLocalControl);

        TxContext txContext = new TxContext();
        txContext.setTransactionTypes(Sets.newHashSet("lcn"));
        Mockito.when(tcGlobalContext.txContext(Mockito.eq("groupId"))).thenReturn(txContext);


        try {
            dtxServiceExecutor.transactionRunning(info);
        } catch (Throwable throwable) {
            log.error("transactionRunning:",throwable);
        }
    }

}