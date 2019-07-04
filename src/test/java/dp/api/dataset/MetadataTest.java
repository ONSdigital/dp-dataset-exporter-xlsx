package dp.api.dataset;

import dp.api.Link;
import dp.api.dataset.models.Alert;
import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.ContactDetails;
import dp.api.dataset.models.Download;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.GeneralDetails;
import dp.api.dataset.models.LatestChange;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.MetadataLinks;
import dp.api.dataset.models.Publisher;
import dp.api.dataset.models.TemporalFrequency;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MetadataTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

    public static Metadata createTestMetadata() {

        final Metadata metadata = new Metadata();

        metadata.setAlerts(Arrays.asList(
                new Alert("alert 1 date", "alert 1 desc", "alert 1 type"),
                new Alert("alert 2 date", "alert 2 desc", "alert 2 type")
        ));

        metadata.setContacts(Arrays.asList(
                new ContactDetails("Contact 1 email", "Contact 1 name", "Contact 1 phone"),
                new ContactDetails("Contact 2 email", "Contact 2 name", "Contact 2 phone")
        ));

        metadata.setDescription("This is the description of the dataset. This is the description of the dataset. This is the description of the dataset. This is the description of the dataset. This is the description of the dataset. This is the description of the dataset.");

        metadata.setDimensions(Arrays.asList(
                new CodeList("dimension1Id", "dimension1 name", "dimension1 description", "http://localhost:22400/code-lists/64d384f1-ea3b-445c-8fb8-aa453f96e58a"),
                new CodeList("dimension2Id", "dimension2 name", "dimension2 description", "http://localhost:22400/code-lists/64d384f1-ea3b-445c-8fb8-aa453f96e58a"),
                new CodeList("dimension3Id", "dimension3 name", "dimension3 description", "http://localhost:22400/code-lists/64d384f1-ea3b-445c-8fb8-aa453f96e58a")
        ));

        metadata.setDistribution(Arrays.asList(
                "Distribution1",
                "Distribution2",
                "Distribution3"
        ));

        metadata.setDownloads(new DownloadsList(
                new Download("http://localhost:22400/CSVURL", "123"),
                new Download("http://localhost:22400/XSLXURL", "123")
        ));

        metadata.setKeywords(Arrays.asList(
                "keyword1",
                "keyword2",
                "keyword3"
        ));

        metadata.setLatestChanges(Arrays.asList(
                new LatestChange("Latest change 1 name", "Latest change 1 description", "Latest change 1 type"),
                new LatestChange("Latest change 2 name", "Latest change 2 description", "Latest change 2 type")
        ));

        metadata.setLicense("License");

        metadata.setLinks(new MetadataLinks(
                new Link("accessRights", "http://localhost:22400/accessRights"),
                new Link("self", "http://localhost:22400/self"),
                new Link("spatial", "http://localhost:22400/spatial"),
                new Link("version", "http://localhost:22400/version"),
                new Link("version", "http://localhost:20000/version")
        ));

        metadata.setMethodologies(Arrays.asList(
                new GeneralDetails("http://localhost:22400/Methodology1", "Methodology1 title", "Methodology1 description"),
                new GeneralDetails("http://localhost:22400/Methodology2", "Methodology2 title", "Methodology2 description"),
                new GeneralDetails("http://localhost:22400/Methodology3", "Methodology3 title", "Methodology3 description")
        ));

        metadata.setNationalStatistic(true);
        metadata.setNextRelease("next release value");


        metadata.setPublications(Arrays.asList(
                new GeneralDetails("http://localhost:22400/Publication1", "Publication1 title", "Publication1 description"),
                new GeneralDetails("http://localhost:22400/Publication2", "Publication2 title", "Publication2 description"),
                new GeneralDetails("http://localhost:22400/Publication3", "Publication3 title", "Publication3 description")
        ));

        metadata.setPublisher(new Publisher("Publisher name", "Publisher type", "http://localhost:22400/publisherHREF"));

        metadata.setQmi(new GeneralDetails("http://localhost:22400/QMI-HREF", "QMI title", "QMI desc"));


        metadata.setRelatedDatasets(Arrays.asList(
                new GeneralDetails("http://localhost:22400/RelatedDataset1", "RelatedDataset1 title", "RelatedDataset1 description"),
                new GeneralDetails("http://localhost:22400/RelatedDataset2", "RelatedDataset2 title", "RelatedDataset2 description"),
                new GeneralDetails("http://localhost:22400/RelatedDataset3", "RelatedDataset3 title", "RelatedDataset3 description")
        ));


        metadata.setReleaseDate("2019-12-31T00:00:00.000Z");
        metadata.setReleaseFrequency("release frequency value");

        metadata.setTemporal(Arrays.asList(
                new TemporalFrequency("end date", "frequency", "start date")
        ));


        metadata.setTheme("The dataset theme");
        metadata.setUri("http://localhost:22400/the-dataset-uri");
        metadata.setUnitOfMeasure("The unit of measure");
        metadata.setTitle("The dataset title");

        return metadata;
    }

}
