package cn.itcast.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

public class FastDFSTest {

    @Test
    public void test() throws Exception {
        //追踪服务器配置文件路径
        String conf_filename = ClassLoader.getSystemResource("fastdfs/tracker.conf").getPath();

        //设置全局配置信息
        ClientGlobal.init(conf_filename);

        //创建TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //创建trackerServer;可以由trackerClient获取
        TrackerServer trackerServer = trackerClient.getConnection();

        //创建storageServer，可以为空
        StorageServer storageServer = null;

        //创建存储服务器客户端StorageClient
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);

        //上传文件
        /**
         * 参数1：文件路径
         * 参数2：文件的扩展名，后缀
         * 参数3：文件信息
         */
        String[] upload_file = storageClient.upload_file("D:\\itcast\\pics\\575968fcN2faf4aa4.jpg", "jpg", null);
        /**
         * 返回数组：
         * group1 组名
         * M00/00/00/wKgMqF0prEWARdE2AABw0se6LsY409.jpg 文件路径
         */
        if (upload_file != null && upload_file.length > 0) {
            for (String str : upload_file) {
                System.out.println(str);
            }

            String groupName = upload_file[0];
            String filename = upload_file[1];

            //获取存储服务器信息
            ServerInfo[] serverInfos = trackerClient.getFetchStorages(trackerServer, groupName, filename);
            for (ServerInfo serverInfo : serverInfos) {
                System.out.println("ip = " + serverInfo.getIpAddr() + "; port=" + serverInfo.getPort());
            }

            String url = "http://" + serverInfos[0].getIpAddr() + "/" + groupName + "/" + filename;
            System.out.println("url = " + url);
        }
     }
}
