package dp.xlsx;

import dp.api.dataset.DatasetAPIClientImpl;
import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.Metadata;
import dp.api.filter.FilterAPIClient;
import dp.configuration.TestConfig;
import dp.handler.Handler;
import dp.s3crypto.S3Crypto;

import org.apache.poi.ss.usermodel.Workbook;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.s3.AmazonS3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
public class ConverterTest {

	@MockBean
	@Qualifier("crypto-client")
	private S3Crypto s3Crypto;

	@MockBean
	@Qualifier("s3-client")
	private AmazonS3 s3Client;

	@Autowired
	private Converter converter;

	@Autowired
	@InjectMocks
	private Handler handler;

	@MockBean
	private FilterAPIClient filterAPI;

	@MockBean
	private DatasetAPIClientImpl datasetAPI;

	@Test
	public void csvToXlsx() throws IOException {
		try (final InputStream csv = ConverterTest.class.getResourceAsStream("v4_0.csv")) {

			/*
			We need to include at least one blank/standard codelist to avoid throwing null pointer errors.
			This is a testing scenario only, CMD datasets by definition ALWAYS have codelists.
			*/

			List<CodeList> emptyCodeListArray = new ArrayList<>();
			emptyCodeListArray.add(new CodeList("1234", "a code list name", " adescription", "a codelist href"));

			Metadata datasetMetadata = new Metadata();
			datasetMetadata.setTitle("test title");
			datasetMetadata.setDimensions(emptyCodeListArray);

			Workbook workbook = converter.toXLSX(csv, datasetMetadata);
			Assertions.assertThat(workbook.getNumberOfSheets()).isEqualTo(2);

			}
		}
	}
