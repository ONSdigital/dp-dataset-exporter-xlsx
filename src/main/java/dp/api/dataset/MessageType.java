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
     * pre published jobs that are generated internally and are not driven by filter jobs.
     */
    PRE_PUBLISH;

    public static MessageType GetMessageType(ExportedFile message) {
        if (StringUtils.isEmpty(message.getFilterId().toString())) {
            return PRE_PUBLISH;
        }
        return FILTER;
    }
}
