package jetcache.samples.service;

import jetcache.samples.dto.response.SkuResponse;

import java.util.List;

public interface ProductImageService {

    List<String> listByProductCode(String productCode);


}
