package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;


public class Main {
	private final static String bucketName = "wuxuyu";
	private final static String filePath   = 
          "E:\\study\\big data\\filesync\\";
	private final static String accessKey = "FB3D24B0CA8E499819B0";
private final static String secretKey = 
"W0U2NzZBOUE5MEY4MzdFNEFEQkVEMzVGNjhGNjIw";
	private final static String serviceEndpoint = 
		"http://scut.depts.bingosoft.net:29997";
	private final static String signingRegion = "";
	private static long partSize = 5 << 20;
	private final static long threshold =20 << 20;
	
	private final static BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
	private final static ClientConfiguration ccfg = new ClientConfiguration().
			withUseExpectContinue(true);

	private final static EndpointConfiguration endpoint = new EndpointConfiguration(serviceEndpoint, signingRegion);

	private final static AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withClientConfiguration(ccfg)
            .withEndpointConfiguration(endpoint)
            .withPathStyleAccessEnabled(true)
            .build();

	//上传文件
	public static void upload(String keyName){
		String FilePath = Paths.get(filePath, keyName).toString();
		System.out.format("Uploading %s to S3 bucket %s...\n", FilePath, bucketName);
        final File file = new File(FilePath);

        for (int i = 0; i < 2; i++) {
            try {
                s3.putObject(bucketName, keyName, file);
                break;
            } catch (AmazonServiceException e) {
                if (e.getErrorCode().equalsIgnoreCase("NoSuchBucket")) {
                    s3.createBucket(bucketName);
                    continue;
                }

                System.err.println(e.toString());
                System.exit(1);
            } catch (AmazonClientException e) {
                try {
                    // detect bucket whether exists
                    s3.getBucketAcl(bucketName);
                } catch (AmazonServiceException ase) {
                    if (ase.getErrorCode().equalsIgnoreCase("NoSuchBucket")) {
                        s3.createBucket(bucketName);
                        continue;
                    }
                } catch (Exception ignore) {
                }

                System.err.println(e.toString());
                System.exit(1);
            }
        }

        System.out.println("Upload Done!");
    }

	//下载文件
	public static void download(String keyName) {
		String FilePath = Paths.get(filePath, keyName).toString();
        System.out.format("Downloading %s to S3 bucket %s...\n", keyName, bucketName);
        
        S3ObjectInputStream s3is = null;
		FileOutputStream fos = null;
		try {
			S3Object o = s3.getObject(bucketName, keyName);
			s3is = o.getObjectContent();
			fos = new FileOutputStream(new File(FilePath));
			byte[] read_buf = new byte[64 * 1024];
			int read_len = 0;
			while ((read_len = s3is.read(read_buf)) > 0) {
				fos.write(read_buf, 0, read_len);
			}
		} catch (AmazonServiceException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} finally {
			if (s3is != null) try { s3is.close(); } catch (IOException e) { }
			if (fos != null) try { fos.close(); } catch (IOException e) { }
		}
		
		System.out.println("Download Done!");
	}
	
	//分块上传
	public static void multiupload(String keyName) {		
		// Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
		ArrayList<PartETag> partETags = new ArrayList<PartETag>();
		final String FilePath = Paths.get(filePath, keyName).toString();
		File file = new File(FilePath);
		long contentLength = file.length();
		String uploadId = null;
		
		try {
			// Step 1: Initialize.
			InitiateMultipartUploadRequest initRequest = 
					new InitiateMultipartUploadRequest(bucketName, keyName);
			uploadId = s3.initiateMultipartUpload(initRequest).getUploadId();
			System.out.format("Created upload ID was %s\n", uploadId);

			// Step 2: Upload parts.
			long filePosition = 0;
			for (int i = 1; filePosition < contentLength; i++) {
				// Last part can be less than 5 MB. Adjust part size.
				partSize = Math.min(partSize, contentLength - filePosition);

				// Create request to upload a part.
				UploadPartRequest uploadRequest = new UploadPartRequest()
						.withBucketName(bucketName)
						.withKey(keyName)
						.withUploadId(uploadId)
						.withPartNumber(i)
						.withFileOffset(filePosition)
						.withFile(file)
						.withPartSize(partSize);

				// Upload part and add response to our list.
				System.out.format("Uploading part %d\n", i);
				partETags.add(s3.uploadPart(uploadRequest).getPartETag());

				filePosition += partSize;
			}

			// Step 3: Complete.
			System.out.println("Completing upload");
			CompleteMultipartUploadRequest compRequest = 
					new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);

			s3.completeMultipartUpload(compRequest);
		} catch (Exception e) {
			System.err.println(e.toString());
			if (uploadId != null && !uploadId.isEmpty()) {
				// Cancel when error occurred
				System.out.println("Aborting upload");
				s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, keyName, uploadId));
			}
			System.exit(1);
		}
		System.out.println("Multiupload Done!");
	}

	//分块下载
	public static void multidownload(String keyName) {
		final String FilePath = Paths.get(filePath, keyName).toString();
		
		File file = new File(FilePath);
		
		S3Object o = null;
		S3ObjectInputStream s3is = null;
		FileOutputStream fos = null;
		
		try {
			// Step 1: Initialize.
			ObjectMetadata oMetaData = s3.getObjectMetadata(bucketName, keyName);
			final long contentLength = oMetaData.getContentLength();
			final GetObjectRequest downloadRequest = 
new GetObjectRequest(bucketName, keyName);

			fos = new FileOutputStream(file);

			// Step 2: Download parts.
			long filePosition = 0;
			for (int i = 1; filePosition < contentLength; i++) {
				// Last part can be less than 5 MB. Adjust part size.
				partSize = Math.min(partSize, contentLength - filePosition);

				// Create request to download a part.
				downloadRequest.setRange(filePosition, filePosition + partSize);
				o = s3.getObject(downloadRequest);

				// download part and save to local file.
				System.out.format("Downloading part %d\n", i);

				filePosition += partSize+1;
				s3is = o.getObjectContent();
				byte[] read_buf = new byte[64 * 1024];
				int read_len = 0;
				while ((read_len = s3is.read(read_buf)) > 0) {
					fos.write(read_buf, 0, read_len);
				}
			}

			// Step 3: Complete.
			System.out.println("Completing download");

			System.out.format("save %s to %s\n", keyName, filePath);
		} catch (Exception e) {
			System.err.println(e.toString());
			
			System.exit(1);
		} finally {
			if (s3is != null) try { s3is.close(); } catch (IOException e) { }
			if (fos != null) try { fos.close(); } catch (IOException e) { }
		}
		System.out.println("Multidownload Done!");
	}

	//删除文件
	public static void delete(String keyName) {
		try {
			s3.deleteObject(bucketName, keyName);
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}
		System.out.println("Delete Done!");
	}
	
	public static void main(String[] args) throws Exception{
		System.out.format("Objects in S3 bucket %s:\n", bucketName);
		ListObjectsV2Result result = s3.listObjectsV2(bucketName);
		List<S3ObjectSummary> objects = result.getObjectSummaries();
		for (S3ObjectSummary os : objects) {
			String keyName=os.getKey();
		    System.out.println("正在下载"+keyName);
		    String FilePath=Paths.get(filePath, keyName).toString();;
	        File file = new File(FilePath);
		    if(file.exists()) {}//默认如果有冲突则覆盖
		    if(file.length()>=threshold) {
		    	multidownload(keyName);
		    }
		    else download(keyName);
		}
		//监听
        //构造监听服务
        WatchService watcher = FileSystems.getDefault().newWatchService();
        //监听注册，监听实体的创建、修改、删除事件
        Paths.get(filePath).register(watcher,
                new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE});
        while (true) {
            WatchKey key = watcher.take();
            //监听key为null,则跳过
            if (key == null) {
                continue;
            }
            //获取监听事件
            for (WatchEvent<?> event : key.pollEvents()) {
                //获取监听事件类型
                WatchEvent.Kind<?> kind = event.kind();
                //异常事件跳过
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                //获取监听Path
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path path = ev.context();
                //构造完整路径
                String FilePath=Paths.get(filePath, path.toString()).toString();;
    	        File file = new File(FilePath);
                //文件删除
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.printf("file delete:%s \n", path);
                    delete(path.toString());
                }
               //文件创建
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    System.out.printf("file create:%s \n", path);
                    if(file.length()>=threshold) {
        		    	multiupload(path.toString());
        		    }
        		    else upload(path.toString());
                }
               //文件修改
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    System.out.printf("file modify:%s \n", path);
                    if(file.length()>=threshold) {
        		    	multiupload(path.toString());
        		    }
        		    else upload(path.toString());
                }
                //处理监听key后(即处理监听事件后)，监听key需要复位，便于下次监听
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
	}
}
