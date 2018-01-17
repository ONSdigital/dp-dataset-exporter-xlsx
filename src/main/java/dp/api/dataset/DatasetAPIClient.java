package dp.api.dataset;

import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.exceptions.FilterAPIException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Defines an API client for the dp-dataset-api.
 */
public interface DatasetAPIClient {

    /**
     * Get the Dataset {@link Metadata}.
     *
     * @param versionPath the version uri to get the metadata for.
     * @return the Dataset {@link Metadata}.
     * @throws MalformedURLException MalformedURLException problem getting the metadata.
     */
    Metadata getMetadata(final String versionPath) throws MalformedURLException, FilterAPIException;

    /**
     * Update the dataset version
     *
     * @param datasetVersionURL the url of the dataset version to update.
     * @param downloads         the {@link dp.api.dataset.models.DatasetDownloads} to update.
     * @throws MalformedURLException problem updating the dataset version.
     */
    void putVersionDownloads(final String datasetVersionURL, DownloadsList downloads) throws MalformedURLException, FilterAPIException;
}