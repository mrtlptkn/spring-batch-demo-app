package com.mrtlptkn.springbatchdemoapp.sample03;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

// Uyarı step seviyesinde bir karar vermiyoruz. Job seviyesinde akışın hangi steplere aktarılacağının kararını veriyoruz.


// Not: Split Flow akışlarında Decider kullanımı thread arasında çalışırken @JobScope ile aşretlenmemeli. Bu durumda Farklı bir Thread Job Scope kaybediyor.
// Senkron çalışırken prolem yok.

@Component
//@JobScope // Senkron Thread üzerinde Job seviyesinde çalışır.
public class CriticalStockProductsDecider implements JobExecutionDecider {

    // Burada karar verirken job seviyesinde bir karar vermek lazım. Bu sebeple bu karar servisinin birkaç farklı adım job çalışırken tetikleneceğini düşünerek çok falza veri okuma ve APİ call gibi isteklerde bunu dikkatli kullanalım.

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        // bellir bir kritik stok eşik değerine göre akışın nasıl solandırılacağına karar vermek istiyorum.
        if(jobExecution.getStepExecutions().stream().anyMatch(se -> se.getStepName().equals("productStep") && se.getReadCount() > 0 && se.getFilterCount() > 20 && se.getStatus().toString().equals("COMPLETED"))) { // Product işleme adımında eğer elimde okunabilen bir kayıt varsa
            return new FlowExecutionStatus("HIGH_CRITICAL_STOCK");
        } else {
            if(stepExecution.getStatus().toString().equals("COMPLETED")) {
                return new FlowExecutionStatus("LOW_CRITICAL_STOCK");
            } else {
                return new FlowExecutionStatus("FAILED");
            }
        }

    }
}
