package com.mrtlptkn.springbatchdemoapp.sample03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductResponseApiClientConfig {

    private final  static Logger log = LoggerFactory.getLogger(ProductResponseApiClientConfig.class);
    private final ProductItemWriter itemWriter;
    private final ProductItemProcessor itemProcessor;
    private final CriticalStockProductsDecider decider;

    public ProductResponseApiClientConfig(ProductItemWriter itemWriter, ProductItemProcessor itemProcessor, CriticalStockProductsDecider decider) {
        this.itemWriter = itemWriter;
        this.itemProcessor = itemProcessor;
        this.decider = decider;
    }

    @Bean
    @StepScope
    public ODataPagedItemReader itemReader(RestClient.Builder restClientBuilder) {
        return new ODataPagedItemReader(restClientBuilder.build(),"https://services.odata.org/northwind/northwind.svc/Products",10);
    }

//    @Bean
//    @StepScope
//    public ItemWriter<ProductResponse> productItemWriter() {
//        return items -> {
//
//            for (ProductResponse item : items) {
//                log.info("Product: {}", item.productName());
//            }
//
//            log.info("Yazılacak ürün sayısı: {}", items.size());
//        };
//    }

    @Bean
    public Step productStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, ODataPagedItemReader itemReader){

        // Eğer bu ayarı kurumsal projelerde yapmazsak kendisi arka arkaya retry yapar. ve sistem kaynaklarını bunun gibi süre verilmeyen retry ifadeler ciddi anlamda tüketir.

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(300); // ilk başlanıç 2.deneme 300ms sonra
        backOffPolicy.setMultiplier(2.0); // her bir denemede 300 -> 600 -> 1200 -> 2400ms -> 3600ms
        backOffPolicy.setMaxInterval(3000); // 3sn denemenin bitmesi için süre tanıdım sana

        return new StepBuilder("productStep",jobRepository)
                .<ProductResponse,ProductResponse>chunk(2,platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .faultTolerant()
                // Process ve Write adımda -> Veri kaynağına anlık kesinti olabilir.
                // Process adımında veri işlenirken hatalı işlem yapılabilir.
                // retry bu durumlarda çalışır. read kısmında çalışmaz. eğer api call işlemlerinde read ederken bağlantı kopukluğu durumlarını abstractPageReader üzerinden custom yönetmemiz gerekir.
                .retry(TransientDataAccessException.class) // anlık veri kaynağına giderken kesinti olursa veriyi çektik işleyemedik diye dayanıklılık sağlayan bir yöntem
                .retryLimit(3) // 3 kez dene
                .backOffPolicy(backOffPolicy)
                .noSkip(HttpClientErrorException.class) // 4xx Client hataları varsa job direk kesilir.
                .build();

        // Not: Retry işlemleri Skip öncesi çalışan işlemlerdir.
    }

    @Bean
    public Tasklet alertTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Critical Stock Products Alert Tasklet çalıştı.");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step criticalStockProductsStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,Tasklet alertTasklet){
        return new StepBuilder("criticalStockProductsStep",jobRepository)
                .tasklet(alertTasklet,platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet reportTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Report Tasklet çalıştı.");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step productReportStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,Tasklet reportTasklet){
        return new StepBuilder("productReportStep",jobRepository)
                .tasklet(reportTasklet,platformTransactionManager)
                .build();
    }

    // Decision Flow -> Karara bağlı akışlar.

    @Bean
    public Job productJob(JobRepository jobRepository, Step productStep, Step criticalStockProductsStep, Step productReportStep){
        return new JobBuilder("productJob",jobRepository)
                .start(productStep)
                .next(decider) // adımı next ile karar vericiye bırak
                .on("HIGH_CRITICAL_STOCK") // HIGH_CRITICAL_STOCK alındıysa criticalStockProductsStep ilgili step
                .to(criticalStockProductsStep)
                .from(decider).on("LOW_CRITICAL_STOCK")// LOW_CRITICAL_STOCK Repor Step
                .to(productReportStep)
                .from(decider).on("*").fail() // bunun dışındaki herhangi bir senaryoda akışı fail et.
                .end()
                .build();
    }

}
