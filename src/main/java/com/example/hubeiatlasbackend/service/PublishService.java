package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.PublishMapper;
import lombok.extern.slf4j.Slf4j;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Service
@Slf4j
public class PublishService {
    @Resource
    private PublishMapper PublishMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${geoserver.rest_api_url}")
    private String geoserverUrl;

    @Value("${geoserver.gwc}")
    private String gwcUrl;

    @Value("${geoserver.username}")
    private String username;

    @Value("${geoserver.password}")
    private String password;

    @Value("${geoserver.map_data_dir}")
    private String dataDir;

    @Value("${geoserver.workspace}")
    private String workspace;

    public String publishGeoTiff(MultipartFile file, String type, String map_id) throws IOException, FactoryException {
        ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            throw new IllegalArgumentException("Unsupported image format.");
        }

        ImageReader reader = readers.next();
        log.info("Using ImageReader: " + reader.getClass().getName());
        reader.setInput(input);

        // 使用 TwelveMonkeys 读取图像（自动解析 ICC）
        BufferedImage cmykImage = reader.read(0);
        int width = cmykImage.getWidth();
        int height = cmykImage.getHeight();

        // 构建 RGB 目标图像
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 将 CMYK 图像转换到 RGB 空间（使用源图内嵌 ICC 或自动色彩空间）
        ColorConvertOp colorConvert = new ColorConvertOp(null);
        colorConvert.filter(cmykImage, rgbImage); // 源 ICC 可自动从 cmykImage 获取

        // 1.2 设置任意参考的经纬度范围（比如经度从 0 开始，跨度 1 度）
        double xmin = 0;
        double xmax = 1.0; // 经度跨度
        double aspectRatio = height / (double) width;
        double latSpan = (xmax - xmin) * aspectRatio;
        double ymin = 0;
        double ymax = ymin + latSpan;

        // 1.3 设置坐标参考系 EPSG:4326
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

        // 1.4 设置地理边界（envelope）
        GeneralEnvelope envelope = new GeneralEnvelope(new double[]{ymin, xmin}, new double[]{ymax, xmax});
        envelope.setCoordinateReferenceSystem(crs);

        // 1.5 创建 GridCoverage（带坐标图层）
        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D coverage = factory.create("GeoTIFF", rgbImage, envelope);

        // 获取原始文件名（不带扩展名）使用原始名称命名 tiff 文件
        String originalName = file.getOriginalFilename();
        String baseName = originalName != null ? originalName.replaceFirst("[.][^.]+$", "") : "default";
        String fileName = baseName + ".tif";

        Path filePath;
        if (type.equals("sheet")) {
            filePath = Paths.get(dataDir + "/sheet/", fileName);
        } else if (type.equals("piece")) {
            filePath = Paths.get(dataDir + "/piece/", fileName);
        } else {
            filePath = Paths.get(dataDir, fileName);
        }
        Files.createDirectories(filePath.getParent());

        File geoTiffFile = filePath.toFile();
        GeoTiffWriter writer = new GeoTiffWriter(geoTiffFile);
        writer.write(coverage, null);
        writer.dispose();

        String filePathString = geoTiffFile.getAbsolutePath().replace("\\", "/");

        // 2. 创建 coverageStore 并自动发布图层（一次性）
        //    使用 PUT + image/tiff 方式上传 GeoTIFF 到 GeoServer
        String storeName = map_id;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.parseMediaType("image/tiff"));

        byte[] tiffBytes = Files.readAllBytes(filePath);
        HttpEntity<byte[]> entity = new HttpEntity<>(tiffBytes, headers);

        String url = geoserverUrl + String.format("/workspaces/%s/coveragestores/%s/file.geotiff?configure=all", workspace, storeName);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        // 3. 提取 GeoTIFF 文件名（不带扩展名）作为关键字或描述
        String fileOriginalName = fileName.substring(0, fileName.lastIndexOf('.'));

        // 3.1 构造修改图层元数据的 JSON
        String metadataJson = String.format(
                "{\"coverage\": {" +
                        "\"description\": \"Uploaded from file: %s\", " +
                        "\"keywords\": {\"string\": [\"%s\", \"geotiff\"]}" +
                        "}}",
                fileOriginalName, fileOriginalName
        );

        // 3.2 发出 PUT 请求更新 coverage metadata
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBasicAuth(username, password);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> updateEntity = new HttpEntity<>(metadataJson, updateHeaders);
        String coverageUrl = String.format("%s/workspaces/%s/coveragestores/%s/coverages/%s",
                geoserverUrl, workspace, storeName, storeName);

        // 3.3 执行更新
        restTemplate.exchange(coverageUrl, HttpMethod.PUT, updateEntity, String.class);

        // 4. 返回 WMS URL 和路径
        return map_id+"构建wms成功！";
    }
}
