dp-dataset-exporter-xlsx
================

A service which converts a V4 into a formatted XLSX file

### Getting started

* Install maven 3.5+ `brew install maven`
* Install Java 1.8+
* Install kafka `brew install kafka` supports versions (0.9, 0.10, 0.11)
* Run the auth-stub-api or Zebedee for authentication
* Setup AWS credentials. The app uses the default provider chain. When running locally this typically means they are provided by the `~/.aws/credentials` file.  Alternatively you can inject the credentials via environment variables as described in the configuration section

To quickly run the service locally run `make debug`.

### Kafka scripts

Scripts for updating and debugging Kafka can be found [here](https://github.com/ONSdigital/dp-data-tools)(dp-data-tools)

### Configuration

| Environment variable        | Default                              | Description
| ----------------------------|--------------------------------------|---------------------------------
| PORT                        | 22800                                | Host and port to bind to
| KAFKA_ADDR                  | localhost:9092                       | Kafka address to use
| KAFKA_GROUP                 | dp-dataset-exporter-xlsx             | Kafka consumer group name
| KAFKA_TOPIC                 | common-output-created                | Kafka topic to listen to
| KAFKA_POLL_MAX_RECORDS      | 1                                    | Maximum number of Kafka messages that will be consumed each time (batch).
| KAFKA_POLL_TIMEOUT          | 120000                               | Timeout to process a batch of Kafka messages, after this time the consumer is considered failed and the group will rebalance in order to reassign the partitions to another member. This value must be greater than the maximum expected time to process a batch of messages.
| KAFKA_SESSION_TIMEOUT       | 10000                                | The timeout used to detect client failures when using Kafka's group management facility, this value may be smaller than the time to process a message.
| KAFKA_SEC_PROTO             | _unset_                              | if set to "TLS", kafka connections will use TLS
| KAFKA_SEC_CLIENT_KEY        | _unset_                              | if using TLS (see above), this is the path to the keystore (optional, used for client auth) - only used if `KAFKA_SEC_CLIENT_KEY_P12` is unset
| KAFKA_SEC_CLIENT_KEY_P12    | _unset_                              | if using TLS (see above), this is a base64-encoded PKCS12 keystore (optional, used for client auth)
| AWS_ACCESS_KEY_ID           | access-key-id                        | AWS access id for s3 (must be provided)
| AWS_SECRET_ACCESS_KEY       | access-key-secret                    | AWS secret key for s3 (must be provided)
| S3_REGION                   | eu-west-1                            | AWS region for S3
| S3_BUCKET_NAME              | csv-exported                         | AWS bucket to store the XLSX files
| S3_BUCKET_URL               | _unset_     (e.g. `https://cf.host`) | If set, the URL prefix for public, exported downloads
| S3_BUCKET_S3_URL            | _unset_  (e.g. `https://bkt.s3.aws`) | If set, S3-friendly URL prefix replacing S3_BUCKET_URL for obtaining CSV
| FILTER_API_URL              | http://localhost:22100               | Filter api URL
| FILTER_API_AUTH_TOKEN       | FD0108EA-825D-411C-9B1D-41EF7727F465 | Secret token to use the Filter api
| DATASET_API_URL             | http://localhost:22000               | Dataset api URL
| DATASET_API_AUTH_TOKEN      | FD0108EA-825D-411C-9B1D-41EF7727F465 | Secret token to use the Dataset api
| DOWNLOAD_SERVICE_URL        | http://localhost:23600               | URL for the download service
| SERVICE_AUTH_TOKEN          | 7049050e-5d55-440d-b461-319f8cdf6670 | Service token to authenticate against Zebedee
| ZEBEDEE_URL                 |                                      | A url to zebedee, if provided the service auth token will be checked on startup
| AWS_ACCESS_KEY_ID           | -                                    | The AWS access key credential
| AWS_SECRET_ACCESS_KEY       | -                                    | The AWS secret key credential
| FULL_DATASET_FILE_PREFIX    | full-datasets                        | The prefix added to full dataset download files
| FILTERED_DATASET_FILE_PREFIX| filtered-dataset                     | The prefix added to filtered dataset download files

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright © 2016-2018, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.
