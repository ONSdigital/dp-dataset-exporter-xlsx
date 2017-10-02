package dp.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DatasetAPI {

    @Value("${DATASET_API:http://localhost:22000}")
    private String kafkaAddress;

    //@Autowired
    //private RestTemplate restTemplate;

    public Dataset getDataset(final String id) {
        return new Dataset();
    }
}
