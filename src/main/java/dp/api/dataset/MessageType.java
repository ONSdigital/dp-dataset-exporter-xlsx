package dp.api.dataset;

import dp.avro.ExportedFile;
import org.springframework.util.StringUtils;

/**
 * Type to represent the different types of messages to process.
 */
public enum MessageType {

    /**
     * user create jobs driven by filters.
     */
    FILTER,

    /**
     * full download jobs that are generated internally and are not driven by filter jobs.
     */
    FULL_DOWNLOAD;

    public static MessageType GetMessageType(ExportedFile message) {
        if (StringUtils.isEmpty(message.getFilterId().toString())) {
            return FULL_DOWNLOAD;
        }
        return FILTER;
    }
}
