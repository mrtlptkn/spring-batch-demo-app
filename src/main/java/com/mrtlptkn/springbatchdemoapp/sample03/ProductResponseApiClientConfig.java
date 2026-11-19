package com.mrtlptkn.springbatchdemoapp.sample03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
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

    public ProductResponseApiClientConfig(ProductItemWriter itemWriter, ProductItemProcessor itemProcessor) {
        this.itemWriter = itemWriter;
        this.itemProcessor = itemProcessor;
    }

    @Bean
    @StepScope
    public ODataPagedItemReader itemReader(RestClient.Builder restClientBuilder) {
        return new ODataPagedItemReader(restClientBuilder.build(),"https://services.odata.org/northwind/northwind.svc/Products",10);
    }

    @Bean
    @StepScope
    public ItemWriter<ProductResponse> productItemWriter() {
        return items -> {

            for (ProductResponse item : items) {
                log.info("Product: {}", item.productName());
            }

            log.info("Yazılacak ürün sayısı: {}", items.size());
        };
    }

    @Bean
    public Step productStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, ODataPagedItemReader itemReader){

        // Eğer bu ayarı kurumsal projelerde yapmazsak kendisi arka arkaya retry yapar. ve sistem kaynaklarını bunun gibi süre verilmeyen retry ifadeler ciddi anlamda tüketir.

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(300); // ilk başlanıç 2.deneme 300ms sonra
        backOffPolicy.setMultiplier(2.0); // her bir denemede 300 -> 600 -> 1200 -> 2400ms -> 3600ms
        backOffPolicy.setMaxInterval(30000); // 3sn denemenin bitmesi için süre tanıdım sana

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
                .noSkip(HttpClientErrorException.class) // 4xx Client hataları varsa ne atlatma nede retry et -> Bu durumda zaten Job failed olucak boşuna kodun geri kalan kısmını çalıştırmayalım.
                .build();

        // Not: Retry işlemleri Skip öncesi çalışan işlemlerdir.
    }

    @Bean
    public Job productJob(JobRepository jobRepository, Step productStep){
        return new JobBuilder("productJob",jobRepository)
                .start(productStep)
                .build();
    }

}
