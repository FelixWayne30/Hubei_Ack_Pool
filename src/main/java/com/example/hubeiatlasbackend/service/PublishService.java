package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.PublishMapper;
import lombok.extern.slf4j.Slf4j;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class PublishService {
    @Resource
    private PublishMapper PublishMapper;

    @Value("${pictures.location}")
    private String pictures_location;

    @Value("${subimages.location}")
    private String subimages_location;

    public void insertBaseMapInfo(MultipartFile file, String type,  Map<String, String> params) throws Exception {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("文件不是有效的图片格式");
        }
        // 获取中文图名
        String originalFilename = file.getOriginalFilename();
        String mapName = extractChineseName(originalFilename);
        // 根据 type 来决定插入逻辑
        if ("sheet".equals(type)) {
            String originTopic = params.get("originTopic");
            String subTopic = params.get("subTopic");
            PublishMapper.insertBaseMapInfoSheet(mapName, originTopic, subTopic);
        } else if ("piece".equals(type)) {
            String subitem = params.get("subitem");
            String parentMapName = params.get("parentMapName");
            PublishMapper.insertBaseMapInfoPiece(mapName, subitem, parentMapName);
        }

        // 确保目录存在
        File originDir = new File(pictures_location);
        if (!originDir.exists()) originDir.mkdirs();

        File subDir = new File(subimages_location);
        if (!subDir.exists()) subDir.mkdirs();

        // 保存原图
        File originFile = new File(originDir, mapName + ".jpg");
        file.transferTo(originFile);

        // 生成压缩图并保存
        File compressedFile = new File(subDir, mapName + ".jpg");
        saveCompressedImage(image, compressedFile);
    }

    private void saveCompressedImage(BufferedImage image, File outputFile) throws IOException {
        // 简单压缩：按固定比例缩放，比如 0.5
        int newW = image.getWidth() / 4;
        int newH = image.getHeight() / 4;
        Image scaled = image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        BufferedImage buffered = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        ImageIO.write(buffered, "jpg", outputFile);
    }

    private String extractChineseName(String filename) {
        // 去掉后缀
        String nameWithoutExtension = filename.replaceAll("\\.jpg$|\\.jpeg$|\\.png$", "");

        // 提取中文
        Matcher matcher = Pattern.compile("[\u4e00-\u9fa5]+").matcher(nameWithoutExtension);
        StringBuilder chineseName = new StringBuilder();

        while (matcher.find()) {
            chineseName.append(matcher.group());
        }

        return chineseName.toString();
    }

    public void deleteMapFile(UUID id,String name) {
        try {
            PublishMapper.deleteMap(id);
        } finally {
            // 2. 拼接文件名
            String fileName = name + ".jpg";

            // 3. 删除原图
            File originFile = new File(pictures_location, fileName);
            deleteFile(originFile);

            // 4. 删除压缩图
            File subFile = new File(subimages_location, fileName);
            deleteFile(subFile);
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("已删除文件: " + file.getAbsolutePath());
            } else {
                System.err.println("删除失败: " + file.getAbsolutePath());
            }
        } else {
            System.out.println("文件不存在: " + file.getAbsolutePath());
        }
    }

    public void editMapFile(MultipartFile file, UUID id) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("文件不是有效的图片格式");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        PublishMapper.editMapFileInfo(id,width,height);
    }

    public void editMapInfo(UUID id, String name, String description) {
        PublishMapper.editMapInfo(id,name,description);
    }
}
