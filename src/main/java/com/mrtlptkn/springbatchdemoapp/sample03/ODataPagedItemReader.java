package com.mrtlptkn.springbatchdemoapp.sample03;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
@StepScope
public class ODataPagedItemReader extends AbstractPagingItemReader<ProductResponse> {

    private final RestClient restClient;
    private final String baseUrl;


    public ODataPagedItemReader(RestClient restClient, String baseUrl, int pageSize) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        setPageSize(pageSize); // Set the page size for pagination
        setName("productApiPagedItemReader");
    }

    @Override
    protected void doReadPage() {

        if(results == null)
            results = new CopyOnWriteArrayList<>();
        else
            results.clear();

        // getPage() -> hangi sayfada kaldığımıza dair bilgi
        // getPageSize() -> sayfa başına kaç kayıt çekileceğine dair bilgi

        int skip = getPage() * getPageSize();
        int top = getPageSize(); // bu kadar kayıt çekilecek.

        ODataResponse response = restClient.get().uri(baseUrl + "?$format=json&$skip={skip}&$top={top}",skip,top).retrieve().body(ODataResponse.class);


        if(response != null && response.value() != null){
            results.addAll(response.value());
        }

    }
}
