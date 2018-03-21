package dp.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Region;

import dp.s3crypto.S3Crypto;
import dp.s3crypto.S3CryptoClient;

@Configuration
public class S3Configuration {

    @Value("${S3_REGION:eu-west-1}")
    private String region;

    @Bean
    @Qualifier("s3-client")
    public AmazonS3 amazonS3Client() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName("eu-west-1"))
                .build();
        return s3Client;
    }
    
    @Bean
    @Qualifier("crypto-client")
    public S3Crypto s3CryptoClient() {
    		return new S3CryptoClient((AmazonS3Client) amazonS3Client());
    }

}
