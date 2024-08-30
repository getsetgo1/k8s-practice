package org.beyond.ordersystem.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beyond.ordersystem.common.service.StockInventoryService;
import org.beyond.ordersystem.product.domain.Product;
import org.beyond.ordersystem.product.dto.CreateProductRequest;
import org.beyond.ordersystem.product.dto.ProductResponse;
import org.beyond.ordersystem.product.dto.ProductSearchDto;
import org.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    //== static이 붙으면 Value 주입이 되지 않는다! ==//
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // synchronized를 설정한다 하더라도, 재고 감소가 DB에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점이다.
    //
    public Long createProduct(CreateProductRequest createProductRequest) {
        MultipartFile image = createProductRequest.getProductImage();
        try {

            Product product = CreateProductRequest.toEntity(createProductRequest);
            Product savedProduct = productRepository.save(product);


            byte[] bytes = image.getBytes();
            Path path = Paths.get("/tmp/", product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            product.updateImagePath(path.toString()); // dirty checking

            //== 동시성 ==//
            if(createProductRequest.getName().contains("sale")) {
                stockInventoryService.increaseStock(savedProduct.getId(), createProductRequest.getStockQuantity());
            }

            return savedProduct.getId();
        } catch(IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public Long createAwsProduct(CreateProductRequest createProductRequest) {
        MultipartFile image = createProductRequest.getProductImage();

        Product product = null;

        try {

            product = CreateProductRequest.toEntity(createProductRequest);
            Product savedProduct = productRepository.save(product);

            byte[] bytes = image.getBytes(); // 이미지 자체는 바이트 형태로 받아옴. 근데 바이트 형태로는 아마 AWS에 올라가지 않을 것이다.
            String fileName = product.getId() + "_" + image.getOriginalFilename();
            // 로컬 PC에 임시저장. 파일로 바꾸고 올리기 위해서!!
            // 프론트엔드에서 직접 올릴 때에는 이렇게 변환해줄 필요가 없다. 파일로 가지고 있으니까
            Path path = Paths.get("/tmp/", product.getId() + "_" + image.getOriginalFilename());
            // Path path = Paths.get("/Users/sejeong/Documents/temp", fileName);
            // local pc에 임시 저장
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            // aws에 pc에 저장된 파일을 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            // 이 s3Path는 https:// 를 달고 나오는 파일이다.
            String s3Path = s3Client.utilities().getUrl(a -> a.bucket(bucketName).key(fileName)).toExternalForm();
            product.updateImagePath(s3Path);
            return savedProduct.getId();
        } catch(IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public Page<ProductResponse> productList(ProductSearchDto searchDto, Pageable pageable) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                log.info("line 113: {}", searchDto);
                // root: 엔티티의 속성을 접근하기 위한 객체이다.
                // CriteriaBuilder: 쿼리를 생성하기 위한 객체이다.
                // select * from product where name like '%searchDto.getSearchName()%';

                if(searchDto != null && searchDto.getSearchName() != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+ searchDto.getSearchName() + "%"));
                }

                if(searchDto != null && searchDto.getCategory() != null) {
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+ searchDto.getCategory() + "%"));
                }
                // 화면 설계가 그렇게 안돼있기 때문에 & 조건이 걸릴 일은 없음

                // 만약 아무것도 없이 들어오면 그냥 findAll 되는 것임
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i< predicateArr.length; i++) {
                    predicateArr[i] = predicates.get(i);
                }

                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList
                .map(ProductResponse::fromEntity);
    }
}
