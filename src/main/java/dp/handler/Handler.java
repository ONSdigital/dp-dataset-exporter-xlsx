package dp.handler;

import static dp.api.dataset.MessageType.FILTER;
import static dp.api.dataset.MessageType.GetMessageType;
import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultOperations;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;

import dp.api.dataset.DatasetAPIClient;
import dp.api.dataset.MessageType;
import dp.api.dataset.WorkbookDetails;
import dp.api.dataset.models.Download;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.Version;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.avro.ExportedFile;
import dp.exceptions.FilterAPIException;
import dp.s3crypto.S3Crypto;
import dp.xlsx.Converter;

/**
 * A class used to consume ExportedFile message from kafka
 */
@Component
public class Handler {

	private final static Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	private static final String VERSION_DOWNLOADS_URL = "/datasets/{0}/editions/{1}/versions/{2}";
	private static final String INSTANCE_URL = "/instances/{0}";
	private static final String PUBLISHED_STATE = "published";

	@Value("${S3_BUCKET_NAME:csv-exported}")
	private String bucket;

	@Value("${VAULT_PATH:secret/shared/psk}")
	private String vaultPath;

	@Value("${DOWNLOAD_SERVICE_URL:http://localhost:23600}")
	private String downloadServiceUrl;

	@Autowired
	@Qualifier("s3-client")
	private AmazonS3 s3Client;
	
	@Autowired
	@Qualifier("crypto-client")
	private S3Crypto s3Crypto;

	@Autowired
	private VaultOperations vaultOperations;

	@Autowired
	private Converter converter;

	@Autowired
	private FilterAPIClient filterAPIClient;

	@Autowired
	private DatasetAPIClient datasetAPIClient;

	@KafkaListener(topics = "${KAFKA_TOPIC:common-output-created}")
	public void listen(final ExportedFile message) {
		MessageType messageType = GetMessageType(message);
		final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());

		try {
			String state = getState(message);
			S3Object object = getObject(uri.getBucket(), uri.getKey(), PUBLISHED_STATE.equals(state));
			if (FILTER.equals(messageType)) {
				handleFilterMessage(message, object, PUBLISHED_STATE.equals(state));
			} else {
				handleFullDownloadMessage(message, object, PUBLISHED_STATE.equals(state));
			}
		} catch (final IOException | FilterAPIException e) {
			if (FILTER.equals(messageType)) {
				LOGGER.error("error when exporting filter {}. exception : {}", message.getFilterId(), e);
			} else {
				LOGGER.error("error when exporting pre publish message {}. exception : {}",
						message.getFilename().toString(), e);
			}
			return;
		} catch (Exception e) {
			LOGGER.error("unexpected error throw while attempting to process message, {}", e);
		}
		if (FILTER.equals(messageType)) {
			LOGGER.info("exported completed for filterID: {}", message.getFilterId());
		} else {
			LOGGER.info("exported completed for instance: datasetID: {}, edition: {}, version: {}",
					message.getDatasetId(), message.getEdition(), message.getVersion());
		}
	}

	private String getState(ExportedFile message) throws MalformedURLException, FilterAPIException {
		String path = "";
		if (message.getInstanceId().length() == 0) {
			path = format(VERSION_DOWNLOADS_URL, message.getDatasetId(), message.getEdition(), message.getVersion());
		} else {
			path = format(INSTANCE_URL, message.getInstanceId());
		}

		Version version = datasetAPIClient.getVersion(path);

		return version.getState();
	}

	private void handleFilterMessage(ExportedFile message, S3Object object, boolean isPublished) throws IOException {
		LOGGER.info("handling filter message: ", message.getFilterId().toString());

		Filter filter = filterAPIClient.getFilter(message.getFilterId().toString());
		Metadata datasetMetadata = null;
		String metadataURL = null;
		WorkbookDetails details = null;

		try {
			URL url = new URL(filter.getLinks().getVersion().getHref());
			metadataURL = url.getPath();
		} catch (MalformedURLException e) {
			throw new IOException(format("error while attempting to create metadata URL filterID {0}, value: {1}",
					message.getFilterId().toString(), filter.getLinks().getVersion().getHref()), e);
		}

		try {
			datasetMetadata = datasetAPIClient.getMetadata(metadataURL);
		} catch (FilterAPIException e) {
			throw new IOException(format("dataset api get metadata returned error. filterID {0}, uri: {1}",
					message.getFilterId().toString(), metadataURL.toString()), e);
		}

		try {
			details = createWorkbook(object, datasetMetadata, message.getFilterId().toString(), isPublished);
		} catch (IOException e) {
			throw new IOException(format("error while attempting to create XLSX workbook filterID: {0}, filename: {1}",
					message.getFilterId().toString(), message.getFilename().toString()), e);
		}

		try {
			filterAPIClient.addXLSXFile(message.getFilterId().toString(), details.getDowloadURI(),
					details.getContentLength());
		} catch (JsonProcessingException e) {
			throw new IOException(format("filter api client addXLSXFile returned error, filterID: {0}",
					message.getFilterId().toString()), e);
		}
	}

	private void handleFullDownloadMessage(ExportedFile message, S3Object object, boolean isPublished)
			throws IOException {
		LOGGER.info("handling pre-publish message: ", message.getFilename().toString());

		String versionURL = format(VERSION_DOWNLOADS_URL, message.getDatasetId(), message.getEdition(),
				message.getVersion());

		Metadata metadata = null;
		try {
			metadata = datasetAPIClient.getMetadata(versionURL);
		} catch (MalformedURLException | FilterAPIException e) {
			LOGGER.error("dataset api client error while attempting to get metadata, URL: {}", versionURL);
			throw e;
		}

		WorkbookDetails details = null;
		try {
			details = createWorkbook(object, metadata, message.getFilename().toString(), isPublished);

			try {
				String downloadUrl = downloadServiceUrl + format(VERSION_DOWNLOADS_URL, message.getDatasetId(),
						message.getEdition(), message.getVersion()) + ".xlsx";

				DownloadsList downloadsList = null;

				if (isPublished) {
					Download download = new Download(downloadUrl, String.valueOf(details.getContentLength()));
					download.setPublicState(details.getDowloadURI());

					downloadsList = new DownloadsList(download, null);
				} else {
					Download download = new Download(downloadUrl, String.valueOf(details.getContentLength()));
					download.setPrivateState(details.getDowloadURI());

					downloadsList = new DownloadsList(download, null);
				}

				datasetAPIClient.putVersionDownloads(versionURL, downloadsList);
			} catch (MalformedURLException | FilterAPIException e)

			{
				LOGGER.error("dataset api PUT version returned error, filename: {}, URL: {}",
						message.getFilename().toString(), versionURL);
				throw e;
			}

		} catch (IOException e) {
			LOGGER.error("error while attempting to create XLSX workbook, filename: {}",
					message.getFilename().toString());
			throw e;
		}
		LOGGER.info("completed processing kafka message", message.getFilterId());
	}

	private WorkbookDetails createWorkbook(S3Object object, Metadata datasetMetadata, String filename,
			boolean isPublished) throws IOException {
		try (final Workbook workbook = converter.toXLSX(object.getObjectContent(), datasetMetadata);
				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			workbook.write(outputStream);

			byte[] xlsxBytes = outputStream.toByteArray();

			final long contentLength = xlsxBytes.length;
			final ObjectMetadata metadata = new ObjectMetadata();

			metadata.setContentLength(contentLength);
			final String key = filename + ".xlsx";

			try (final ByteArrayInputStream stream = new ByteArrayInputStream(xlsxBytes)) {
				final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, stream, metadata);
				if (isPublished) {
					s3Client.putObject(putObjectRequest);
				} else {
					byte[] psk = createPSK();

					Map<String, String> map = new HashMap<String, String>();
					map.put(key, Hex.encodeHexString(psk));

					vaultOperations.write(vaultPath, map);

					s3Crypto.putObjectWithPSK(putObjectRequest, psk);
				}
				return new WorkbookDetails(s3Client.getUrl(bucket, key).toString(), contentLength);
			} catch (SdkClientException e) {
				LOGGER.error("error while attempting PUT XLSX workbook to S3 bucket, filename: {}, bucket: {}", key,
						bucket);
				throw new FilterAPIException("error while attempting PUT XLSX workbook to S3 bucket", e);
			}
		} catch (IOException e) {
			LOGGER.error("error while attempting create XLSX workbook, filename: {}, bucket: {}", filename);
			throw e;
		}
	}
	
	private S3Object getObject(String bucket, String key, boolean isPublished) throws AmazonServiceException, SdkClientException, DecoderException {
		if (isPublished) {
			return s3Client.getObject(bucket, key);
		}
		
		Map<String, Object> map =  vaultOperations.read(vaultPath).getData();
		
		String psk = (String) map.get(key);
		
		byte[] pskBytes =  Hex.decodeHex(psk.toCharArray());		
		return s3Crypto.getObjectWithPSK(bucket, key, pskBytes);
	}

	private byte[] createPSK() {
		byte[] b = new byte[16];
		new Random().nextBytes(b);
		return b;
	}
}
