package org.mvnsearch.boot.oss.impl;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.io.IOUtils;
import org.mvnsearch.boot.oss.ByteArrayDataSource;
import org.mvnsearch.boot.oss.FileStorageService;
import org.springframework.beans.factory.InitializingBean;

import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * file storage service OSS implementation
 *
 * @author linux_china
 */
public class FileStorageServiceOssImpl implements FileStorageService, InitializingBean {
    String accessKey;
    String accessSecret;
    String bucketName;
    String endpoint;
    public static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
    public static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final AtomicLong fileUploadSuccess = new AtomicLong();
    public static final AtomicLong fileUploadFail = new AtomicLong();
    public static final AtomicLong fileGetCounts = new AtomicLong();
    public static final AtomicLong fileDeleteCounts = new AtomicLong();
    /**
     * oss client
     */
    private OSSClient ossClient;

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setEndpoint(String endpoint){
        this.endpoint = endpoint;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterPropertiesSet() throws Exception {
        this.ossClient = new OSSClient(endpoint,accessKey, accessSecret);
    }

    /**
     * save file
     *
     * @param ds data source
     * @return file name
     * @throws IOException IO Exception
     */
    @Override
    public String save(DataSource ds) throws IOException {
        String newName;
        newName = getUuidName(ds.getName());
        byte[] content = IOUtils.toByteArray(ds.getInputStream());
        upload(bucketName, newName, content);
        return newName;
    }

    /**
     * save file to the directory
     *
     * @param directory directory
     * @param ds        data source
     * @return file name with directory name
     * @throws IOException IO Exception
     */
    @Override
    public String saveToDirectory(String directory, DataSource ds) throws IOException {
        String newName;
        newName = directory + "/" + getUuidName(ds.getName());
        byte[] content = IOUtils.toByteArray(ds.getInputStream());
        upload(bucketName, newName, content);
        return newName;
    }

    /**
     * delete file
     *
     * @param fileName file name
     * @throws IOException IO Exception
     */
    @Override
    public void delete(String fileName) throws IOException {
        ossClient.deleteObject(bucketName, fileName);
        fileDeleteCounts.incrementAndGet();
    }

    /**
     * get file data source
     *
     * @param fileName file name
     * @return file data source
     * @throws IOException IO Exception
     */
    @Override
    public DataSource get(String fileName) throws IOException {
        OSSObject ossObject = ossClient.getObject(bucketName, fileName);
        if (ossObject != null) {
            ByteArrayDataSource ds = new ByteArrayDataSource(IOUtils.toByteArray(ossObject.getObjectContent()), getContentType(fileName));
            ds.setName(fileName);
            fileGetCounts.incrementAndGet();
            return ds;
        }
        return null;
    }


    @Override
    public void rename(String oldName, String newName) throws IOException {
        boolean keyExists = true;
        try {
            ossClient.getObjectMetadata(bucketName, oldName);
        } catch (Exception e) {
            keyExists = false;
        }
        if (keyExists) {
            ossClient.copyObject(bucketName, oldName, bucketName, newName);
        }
    }

    public static String getUuidName(String name) {
        String uuid = UUID.randomUUID().toString().replaceAll("\\-", "");
        String newName = uuid;
        if (name != null && name.contains(".")) {
            newName = uuid + name.substring(name.lastIndexOf(".")).toLowerCase();
        }
        return newName;
    }

    /**
     * upload to bucket
     *
     * @param bucketName bucket name
     * @param fileName   file name
     * @param content    content
     */
    private void upload(String bucketName, String fileName, byte[] content) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        try {
            metadata.setContentType(getContentType(fileName));
            metadata.setContentLength(content.length);
            metadata.setCacheControl("public, max-age=31536000");
            ossClient.putObject(bucketName, fileName, bis, metadata);
            fileUploadSuccess.incrementAndGet();
        } catch (Exception ignore) {
            ossClient.putObject(bucketName, fileName, bis, metadata);
            fileUploadFail.incrementAndGet();
        }
    }

    /**
     * get content type according to name or ext, default is "application/octet-stream"
     *
     * @param fileNameOrExt file name or ext name
     * @return content type
     */
    public String getContentType(String fileNameOrExt) {
        if (fileNameOrExt == null || fileNameOrExt.isEmpty()) {
            return DEFAULT_CONTENT_TYPE;
        }
        return fileTypeMap.getContentType(fileNameOrExt.toLowerCase());
    }


}
