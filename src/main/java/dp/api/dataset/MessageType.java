package dp.api.dataset;

import dp.avro.ExportedFile;
import org.springframework.util.StringUtils;

/**
 * Created by dave on 12/12/2017.
 */
public enum MessageType {
    FILTER,

    PRE_PUBLISH;

    public static MessageType GetMessageType(ExportedFile message) {
        if (StringUtils.isEmpty(message.getFilterId().toString())) {
            return PRE_PUBLISH;
        }
        return FILTER;
    }
}
