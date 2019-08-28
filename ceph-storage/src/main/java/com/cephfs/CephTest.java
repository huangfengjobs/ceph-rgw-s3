package com.cephfs;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.util.List;

public class CephTest {

    public static void main(String[] args) {

        CephS3Utils ceph = CephS3Utils.getInstance();

        //创建bucket
        //ceph.s3BucketCreate("java/example/bucket2");

        //删除Bucket
        //boolean delete = ceph.s3BucketDelete("java/example/bucket2");
        //System.out.println(delete);

        //获取bucketList
        //ceph.s3BucketList();

        /*String str = "dgp-escheduler-data/resource/root";
        System.out.println(str.split("/")[0]);*/

        //文件上传
        //boolean upload = ceph.uploadToS3("java-example-bucket2/data/data1/data2", "hello.txt", new File("/Users/jingshuo/Huangfeng/tmp/hello.txt"));
        //System.out.println(upload);

        System.out.println("*******************");
        /*List<S3ObjectSummary> s3ObjectSummaries = ceph.s3BucketObjectList("java-example-bucket2");
        for (S3ObjectSummary objectSummary : s3ObjectSummaries) {
            System.out.println(objectSummary.getSize());
        }
        System.out.println("对象数量："+s3ObjectSummaries.size());*/

        //文件下载
        //boolean flag = ceph.downloadFromS3("java-example-bucket", "hello11", "/Users/jingshuo/Huangfeng/tmp/1.txt");
        //System.out.println(flag);

        /*List<String> strings = ceph.catFile("java-example-bucket", "build.sh", 0, 5);
        for (String string : strings) {
            System.out.println(string);
        }*/

        boolean existsObject = ceph.isExistsObject("java-example-bucket", "build1.sh");
        System.out.println(existsObject);

    }
}
