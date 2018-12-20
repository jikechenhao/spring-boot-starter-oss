package org.mvnsearch.boot.oss;

import org.mvnsearch.boot.oss.impl.FileStorageServiceOssImpl;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * aliyun OSS metrics
 *
 * @author linux_china
 */
@Component
public class AliyunOssMetrics implements PublicMetrics {

    @Override
    public Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new LinkedList<Metric<?>>();
        metrics.add(new Metric<Number>("oss.upload.success", FileStorageServiceOssImpl.fileUploadSuccess.get()));
        metrics.add(new Metric<Number>("oss.upload.fail", FileStorageServiceOssImpl.fileUploadFail.get()));
        metrics.add(new Metric<Number>("oss.get.count", FileStorageServiceOssImpl.fileGetCounts.get()));
        metrics.add(new Metric<Number>("oss.delete.count", FileStorageServiceOssImpl.fileDeleteCounts.get()));
        return metrics;
    }


}
