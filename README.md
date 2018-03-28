dp-dataset-exporter-xlsx
================

A service which converts a V4 into a formatted XLSX file

### Getting started

* Install maven 3.5+ `brew install maven`
* Install Java 1.8+
* Install kafka `brew install kafka` supports versions (0.9, 0.10, 0.11)

To quickly run the service locally use the `run.sh` script. Make sure AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY are
setup as environment variables.

### Configuration

| Environment variable  | Default                              | Description
| ----------------------| -------------------------------------|---------------------------------
| PORT                  | 22800                                | Host and port to bind to
| KAFKA_ADDR            | localhost:9092                       | Kafka address to use
| KAFKA_GROUP           | dp-dataset-exporter-xlsx             | Kafka consumer group name
| KAFKA_TOPIC           | common-output-created                | Kafka topic to listen to
| AWS_ACCESS_KEY_ID     | access-key-id                        | AWS access id for s3 (must be provided)
| AWS_SECRET_ACCESS_KEY | access-key-secret                    | AWS secret key for s3 (must be provided)
| S3_REGION             | eu-west-1                            | AWS region for S3
| S3_BUCKET_NAME        | csv-exported                         | AWS bucket to store the XLSX files
| FILTER_API_URL        | http://localhost:22100               | Filter api URL
| FILTER_API_AUTH_TOKEN | FD0108EA-825D-411C-9B1D-41EF7727F465 | Secret token to use the Filter api
| DATASET_API_URL       | http://localhost:22000               | Dataset api URL
| SERVICE_AUTH_TOKEN    | FD0108EA-825D-411C-9B1D-41EF7727F465 | Secret token to use the Dataset api
| ZEBEDEE_URL           |                                      | A url to zebedee, if provided the service auth token will be checked on startup

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright © 2016-2018, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.
