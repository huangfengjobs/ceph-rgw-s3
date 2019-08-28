package com.cephfs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;


public class CephOperate {

    private static final String SUFFIX = "/";

    public static void main(String[] args) {
        /// accessKey : home/player/player/s3
        String accessKey = "FZ5SPA8M03WIJHWFDTVN";
        String secretKey = "3A9nPmehxU7Tgbax1cF10QnLJgsN29y1mFjD6ezy";
        //创建凭证
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);

        //创建S3 Client
        AmazonS3Client s3client = new AmazonS3Client(credentials, clientConfig);
        s3client.setEndpoint("http://192.168.4.95:7480");

        /*GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName).withExpiration(dateTime);
        if (versionId != null) {
            request.setVersionId(versionId);
        }
        System.out.println(s3client.generatePresignedUrl(request));*/

        //创建Bucket
        /*String bucketName = "java-example-bucket";
        s3client.createBucket(bucketName);*/

        //获取S3 Bucket的list
        List<Bucket> buckets = s3client.listBuckets();
        for (Bucket bucket : buckets) {
            System.out.println(bucket.getName() + "\t" +
                    StringUtils.fromDate(bucket.getCreationDate()));
        }

        //创建文件夹到Bucket
        //String folderName = "testfolder";
        //createFolder(bucketName, folderName, s3client);

        // 上传文件
        //String fileName = folderName + SUFFIX + "testvideo.mp4";
        //s3client.putObject(new PutObjectRequest(bucketName, fileName,
        //new File("C:\\Users\\user\\Desktop\\testvideo.mp4"))
        //.withCannedAcl(CannedAccessControlList.PublicRead));

        //删除文件夹
        //deleteFolder(bucketName, folderName, s3client);

        // 删除 bucket
        //s3client.deleteBucket(bucketName);
        System.out.println("执行完毕");
    }

    public Bucket createBucket(AmazonS3Client s3client, String bucketname) throws SocketTimeoutException {
        System.out.println("开始创建桶" + bucketname);
        Bucket bucket = null;
        bucket = s3client.createBucket(bucketname);
        System.out.println(s3client.toString());
        return bucket;
    }


}
