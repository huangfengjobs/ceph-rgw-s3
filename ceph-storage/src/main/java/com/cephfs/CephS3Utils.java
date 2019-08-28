package com.cephfs;


import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;
import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import jdk.management.resource.ResourceType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CephS3Utils {
    private String accessKey;
    private String secretKey;
    private String serviceEndpoint;
    private String signingRegion;

    private AWSCredentials credentials;
    private AmazonS3 amazonS3;

    private volatile static CephS3Utils instance;

    private CephS3Utils() {
        this.accessKey = "FZ5SPA8M03WIJHWFDTVN";
        this.secretKey = "3A9nPmehxU7Tgbax1cF10QnLJgsN29y1mFjD6ezy";
        this.serviceEndpoint = "http://192.168.4.95:7480";
        this.signingRegion = "";

        rgwConnect();
    }

    private CephS3Utils(String accessKey, String secretKey, String serviceEndpoint, String signingRegion) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;

        rgwConnect();
    }

    //新建AmazonS3对象，连接rgw对象网关
    public boolean rgwConnect() {
        boolean connectResult = false;
        try {
            credentials = new BasicAWSCredentials(accessKey, secretKey);
            amazonS3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                    .build();
            /*amazonS3 = new AmazonS3Client(credentials);
            amazonS3.setEndpoint(serviceEndpoint);*/
            connectResult = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectResult;
    }

    public boolean isConnected() {
        return amazonS3 != null && rgwConnect();
    }

    public static CephS3Utils getInstance() {
        if (instance == null) {
            synchronized (CephS3Utils.class) {
                if (instance == null) {
                    instance = new CephS3Utils();
                }
            }
        }
        if (!instance.isConnected()) {
            instance = null;
        }
        return instance;
    }

    //查看所有Bucket
    public List<Bucket> s3BucketList() {
        List<Bucket> buckets = new ArrayList<Bucket>();
        try {
            buckets = amazonS3.listBuckets();
            for (Bucket bucket : buckets) {
                System.out.println(bucket.getName() + "\t" +
                        StringUtils.fromDate(bucket.getCreationDate()));
            }
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return buckets;
    }

    //创建bucket
    public Bucket s3BucketCreate(String bucketName) {
        Bucket bucket = new Bucket();
        try {
            bucket = amazonS3.createBucket(bucketName);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return bucket;
    }

    //删除bucket
    public boolean s3BucketDelete(String bucketName) {
        ObjectListing objectListing = amazonS3.listObjects(bucketName);
        try {
            //清空bucket中的对象
            Iterator<S3ObjectSummary> iterator = objectListing.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, iterator.next().getKey()));
            }
            //删除bucket
            amazonS3.deleteBucket(new DeleteBucketRequest(bucketName));
            return true;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    //查看具体文件内容
    public List<String> catFile(String bucketName, String key, int skipLineNums, int limit) {
        List<String> content = new ArrayList<>();
        try {
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, key));
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            Stream<String> stream = reader.lines().skip(skipLineNums).limit(limit);
            content = stream.collect(Collectors.toList());
            inputStream.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    //查询指定bucket中的对象
    public List<S3ObjectSummary> s3BucketObjectList(String bucketName) {
        List<S3ObjectSummary> content = new ArrayList<>();
        try {
            content = amazonS3.listObjects(bucketName).getObjectSummaries();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return content;
    }

    //上传对象
    public boolean uploadToS3(String bucketName, String key, File file) {
        PutObjectResult putObjectResult = null;
        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
            putObjectResult = amazonS3.putObject(request);
            return true;
        } catch (SdkClientException e) {
            e.printStackTrace();
        } finally {
            System.out.println("putObjectResult:" + putObjectResult);
        }
        return false;
    }

    //下载对象
    public boolean downloadFromS3(String bucketName, String key, String targetFilePath) {
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            //直接使用getObject()进行下载操作
            File downloadFile = new File(targetFilePath);
            File dir = new File(downloadFile.getParent());
            if (!dir.exists() && !dir.mkdirs()) {
                System.out.println("本地路径不存在");
            }
            amazonS3.getObject(request, new File(targetFilePath));
            return true;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    //删除对象
    public boolean deleteFromS3(String bucketName, String objectName) {
        try {
            ObjectListing objects = amazonS3.listObjects(bucketName);
            for (S3ObjectSummary summary : objects.getObjectSummaries()) {
                if (summary.getKey().equals(objectName)) {
                    amazonS3.deleteObject(bucketName, objectName);
                    return true;
                }
            }
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    //指定的Bucket中是否存在对象
    public boolean isExistsObject(String bucketName,String key) {
        ObjectListing objectListing = new ObjectListing();
        try {
            objectListing = amazonS3.listObjects(bucketName);
            if (objectListing != null && !objectListing.getObjectSummaries().isEmpty()) {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    if (key.equals(objectSummary.getKey())) {
                        return true;
                    }
                }
            }
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    //获取租户的远程路径
    /*public static String getTenantRemotePath(String tenantCode, ResourceType resourceType) {
        String middlePathName = ResourceType.UDF.equals(resourceType) ? "udfs" : "resources";
        String basePathName = getString(DATA_REMOTE_STORE_BASE_PATH);
        if (basePathName.endsWith(File.separator)) {
            return String.format("%s%s/%s", basePathName, middlePathName, tenantCode);
        } else {
            return String.format("%s/%s/%s", basePathName, middlePathName, tenantCode);
        }
    }*/


}
