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

    public String publishGeoTiff(MultipartFile file, String map_id) throws IOException, FactoryException {
        // 1. 解码 CMYK 图像
        ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            throw new IllegalArgumentException("Unsupported image format.");
        }

        ImageReader reader = readers.next();
        reader.setInput(input);
        BufferedImage cmykImage = reader.read(0);

        int width = cmykImage.getWidth();
        int height = cmykImage.getHeight();

        // 2. 转换为 RGB 图像
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ColorConvertOp colorConvert = new ColorConvertOp(null);
        colorConvert.filter(cmykImage, rgbImage);

        // 3. 构造空间参考（地理包络矩形）
        double xmin = 0;
        double xmax = 1.0;
        double aspectRatio = height / (double) width;
        double latSpan = (xmax - xmin) * aspectRatio;
        double ymin = 0;
        double ymax = ymin + latSpan;

        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        GeneralEnvelope envelope = new GeneralEnvelope(new double[]{ymin, xmin}, new double[]{ymax, xmax});
        envelope.setCoordinateReferenceSystem(crs);

        // 4. 创建带空间参考的 GridCoverage2D
        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D coverage = factory.create("GeoTIFF", rgbImage, envelope);

        // 5. 使用临时文件代替永久文件路径
        Path tempFilePath = Files.createTempFile(map_id, ".tif");
        File geoTiffFile = tempFilePath.toFile();
        geoTiffFile.deleteOnExit(); // 程序退出时自动清理

        // 6. 写入 GeoTIFF（可扩展加入 Overview）
        GeoTiffWriter writer = new GeoTiffWriter(geoTiffFile);
        writer.write(coverage, null);
        writer.dispose();

        // 7. 上传至 GeoServer（PUT）
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.parseMediaType("image/tiff"));
        byte[] tiffBytes = Files.readAllBytes(tempFilePath);
        HttpEntity<byte[]> entity = new HttpEntity<>(tiffBytes, headers);

        String storeName = map_id;
        String url = geoserverUrl + String.format(
                "/workspaces/%s/coveragestores/%s/file.geotiff?configure=all",
                workspace, storeName
        );

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        // 8. 更新元数据描述
        String baseName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceFirst("[.][^.]+$", "")
                : "image";

        String metadataJson = String.format(
                "{\"coverage\": {\"description\": \"Uploaded from file: %s\", " +
                        "\"keywords\": {\"string\": [\"%s\"]}}}",
                baseName, baseName
        );

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBasicAuth(username, password);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> updateEntity = new HttpEntity<>(metadataJson, updateHeaders);

        String coverageUrl = String.format(
                "%s/workspaces/%s/coveragestores/%s/coverages/%s",
                geoserverUrl, workspace, storeName, storeName
        );

        restTemplate.exchange(coverageUrl, HttpMethod.PUT, updateEntity, String.class);

        // 9. 返回成功信息（可附加 WMS 预览地址）
        return map_id + " 构建 WMS 成功！";
    }
}
