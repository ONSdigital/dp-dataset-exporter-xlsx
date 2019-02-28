package dp.xlsx;
import dp.api.dataset.models.Metadata;

/**
 * A simple wrapper around the the Group class (a Group represents relevant cells extracted from a single row of a v4 file)
 */
public class GroupProcessor {

    public Group processObsRow(String[] data, int offset) {
        return new Group(data, offset, null);
    }

    public Group processHeaderRow(String[] data, int offset, Metadata datasetMetadata) {
        return new Group(data, offset, datasetMetadata);}

}
