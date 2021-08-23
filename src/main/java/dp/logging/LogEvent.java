package dp.logging;

import com.github.onsdigital.logging.v2.DPLogger;
import com.github.onsdigital.logging.v2.event.BaseEvent;
import com.github.onsdigital.logging.v2.event.Severity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import static com.github.onsdigital.logging.v2.DPLogger.logConfig;


public class LogEvent extends BaseEvent<LogEvent> {

    public static LogEvent warn() {
        return new LogEvent(logConfig().getNamespace(), Severity.WARN);
    }

    public static LogEvent info() {
        return new LogEvent(logConfig().getNamespace(), Severity.INFO);
    }

    public static LogEvent error() {
        return new LogEvent(logConfig().getNamespace(), Severity.ERROR);
    }

    private LogEvent(String namespace, Severity severity) {
        super(namespace, severity, DPLogger.logConfig().getLogStore());
    }

    public LogEvent instanceID(String instanceID) {
        if (StringUtils.isNotEmpty(instanceID)) {
            data("instance_id", instanceID);
        }
        return this;
    }

    public LogEvent filterID(String filterID) {
        if (StringUtils.isNotEmpty(filterID)) {
            data("filter_id", filterID);
        }
        return this;
    }

    public LogEvent path(String path) {
        if(StringUtils.isNotEmpty(path)){
            data("path", path);
        }
        return this;
    }

    public LogEvent fileName(String fileName) {
        if (StringUtils.isNotEmpty(fileName)) {
            data("file_name", fileName);
        }
        return this;
    }

    public LogEvent bucket(String bucket) {
        if (StringUtils.isNotEmpty(bucket)){
            data("bucket", bucket);
        }
        return this;
    }

    public LogEvent datasetID(String datasetID) {
        if (StringUtils.isNotEmpty(datasetID)) {
            data("dataset_id", datasetID);
        }
        return this;
    }

    public LogEvent edition(String edition) {
        if (StringUtils.isNotEmpty(edition)) {
            data("edition", edition);
        }
        return this;
    }

    public LogEvent version(String version) {
        if (StringUtils.isNotEmpty(version)) {
            data("version", version);
        }
        return this;
    }

    public LogEvent versionURL(String versionURL){
        if (StringUtils.isNotEmpty(versionURL)){
            data("version_url", versionURL);
        }
        return this;
    }

    public LogEvent rowCount(String rowCount) {
        if(StringUtils.isNotEmpty(rowCount)){
            data("row_count", rowCount);
        }
        return this;
    }

    public LogEvent zebedeeURL(String zebedeeURL) {
        if (StringUtils.isNotEmpty(zebedeeURL)){
            data("zebedee_url", zebedeeURL);
        }
        return this;
    }

    public LogEvent url(String url){
        if (StringUtils.isNotEmpty(url)){
            data("url", url);
        }
        return this;
    }

    public LogEvent json(String json){
        if (StringUtils.isNotEmpty(json)){
            data("json", json);
        }
        return this;
    }

    public LogEvent statusCode(HttpStatus statusCode){
        if (statusCode != null && StringUtils.isNotEmpty(statusCode.toString())) {
            data("status_code", statusCode.toString());
        }
        return this;
    }

    public LogEvent id(String id) {
        if (StringUtils.isNotEmpty(id)){
            data("id", id);
        }
        return this;
    }
}
