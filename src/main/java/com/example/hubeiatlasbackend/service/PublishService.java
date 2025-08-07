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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${geoserver.rest_api_url}")
    private String geoserverUrl;

    @Value("${geoserver.raw}")
    private String raw;

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
        GeoTiffWriteParams geoTiffWriteParams = new GeoTiffWriteParams();
        geoTiffWriteParams.setCompressionMode(GeoToolsWriteParams.MODE_EXPLICIT);
        geoTiffWriteParams.setCompressionType("Deflate");  // 设置压缩方式为 Deflate
        geoTiffWriteParams.setCompressionQuality(1.0f);    // 无损压缩

        GeoTiffFormat format = new GeoTiffFormat();
        ParameterValueGroup params = format.getWriteParameters();
        params.parameter(GeoTiffFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(geoTiffWriteParams);

        GeoTiffWriter writer = new GeoTiffWriter(geoTiffFile);
        writer.write(coverage, params.values().toArray(new GeneralParameterValue[0]));
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

    public String insertBaseMapInfo(MultipartFile file, String type) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("文件不是有效的图片格式");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 获取中文图名
        String originalFilename = file.getOriginalFilename();
        String mapName = extractChineseName(originalFilename);

        return PublishMapper.insertBaseMapInfo(mapName,type,width,height);
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

    public boolean deleteMapFile(UUID id) {
        String url = geoserverUrl + String.format(
                "/workspaces/%s/coveragestores/%s?recurse=true",
                workspace, id
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteMapInfo(UUID id) {
        PublishMapper.deleteMap(id);
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

    public ResponseEntity<byte[]> getFullImage(UUID id, int width, int height) throws IOException {
        String url = raw
                +"/wms?service=WMS&version=1.1.0&request=GetMap&layers="+ workspace +":" + id.toString()
                +"&bbox=0.0,0.0,1.0,"+ (height/(double)width)
                +"&width="+ width +"&height="+ height +"&srs=EPSG%3A4326&styles=&format=image%2Fjpeg";
        URL imageUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);

        InputStream inputStream = conn.getInputStream();
        byte[] imageBytes = inputStream.readAllBytes();
        inputStream.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // 或 MediaType.IMAGE_PNG
        headers.setContentDisposition(ContentDisposition.attachment().filename("map.jpg").build());

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    private static void processDirectory(File inputDir, File outputDir) throws Exception {
        for (File file : Objects.requireNonNull(inputDir.listFiles())) {
            if (file.isDirectory()) {
                // 递归处理子文件夹
                File newDir = new File(outputDir, file.getName());
                if (!newDir.exists()) {
                    newDir.mkdirs();
                }
                processDirectory(file, newDir);
            } else if (isJpg(file)) {
                // 处理jpg图片
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    System.out.println("无法解码图片: " + file);
                    continue;
                }
                File output = new File(outputDir, file.getName());
                ImageIO.write(image, "jpg", output);
                System.out.println("保存图片到: " + output);
            }
        }
    }

    private static boolean isJpg(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") ||
                name.endsWith(".jpeg");

    }

    public void processImage() throws Exception {
        File inputDir = new File("C:\\Users\\17643\\Desktop\\缩小压缩-富饶资源");

        File outputDir = new File("C:\\Users\\17643\\Desktop\\缩小压缩-富饶资源2");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        processDirectory(inputDir, outputDir);
    }
}
