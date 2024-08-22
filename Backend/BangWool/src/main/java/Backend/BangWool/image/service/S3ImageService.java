package Backend.BangWool.image.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.ServerException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    public URI upload(MultipartFile image, String filename, int targetSize, boolean okToOverride) {
        // 입력 받은 이미지가 비어 있는지 검사
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new BadRequestException("Image is empty");
        }

        this.validateImageFileExtension(image.getOriginalFilename());
        try {
            return URI.create(this.uploadImageToS3(image, filename, targetSize, okToOverride));
        } catch (IOException e) {
            throw new ServerException("Failed to upload image");
        }
    }

    public void delete(URI uri) {
        String key = getKeyFromUrl(uri);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new ServerException("Failed to delete image");
        }
    }


    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new BadRequestException("Invalid image file format");
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtension = Arrays.asList("jpg", "jpeg", "png");

        if (!allowedExtension.contains(extension)) {
            throw new BadRequestException("Invalid image file format");
        }
    }

    private String uploadImageToS3(MultipartFile image, String filename, int targetSize, boolean okToOverride) throws IOException {
        if (filename != null) {
            // 확장자가 있다면, 확장자 제거
            filename = (filename.lastIndexOf('.') != -1) ? filename.substring(0, filename.lastIndexOf('.')) : filename;
        } else {
            filename = "";
        }

        String s3Filename;
        if (okToOverride && !filename.isEmpty()) {
            s3Filename = filename + ".jpg";
        } else if (filename.isEmpty()){
            s3Filename = UUID.randomUUID() + ".jpg";
        } else {
            s3Filename = filename + "_" + UUID.randomUUID() + ".jpg";
        }

        // 이미지 resizing
        byte[] bytes = resizeImage(image, targetSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        // metadata 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(bytes.length);

        // 이미지 올리기
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, s3Filename, byteArrayInputStream, metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new ServerException("Failed to upload image");
        } finally {
            byteArrayInputStream.close();
        }

        return amazonS3.getUrl(bucketName, s3Filename).toString();
    }

    private byte[] resizeImage(MultipartFile image, int targetSize) throws IOException {
        // MultipartFile -> BufferedImage
        BufferedImage inputImage = ImageIO.read(image.getInputStream());

        // 원본 width, height
        int originalWidth = inputImage.getWidth();
        int originalHeight = inputImage.getHeight();

        // resizing 안하는 경우
        if (targetSize == 0 || originalWidth <= targetSize || originalHeight <= targetSize) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(inputImage, "jpg", baos);

            return baos.toByteArray();
        }

        // resizing할 이미지 크기 계산
        int width, height;
        if (originalWidth <= originalHeight) { // 가로가 더 짧은 경우
            width = targetSize;
            height = (int) (targetSize * ((double) originalHeight / originalWidth));
        } else { // 세로가 더 짧은 경우
            width = (int) (targetSize * ((double) originalWidth / originalHeight));
            height = targetSize;
        }

        // resizing
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, width, height, null);
        g.dispose();

        // BufferedImage -> byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);

        return baos.toByteArray();
    }

    public String getKeyFromUrl(URI uri) {
        String decodingKey = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
        return decodingKey.substring(1);
    }

}
