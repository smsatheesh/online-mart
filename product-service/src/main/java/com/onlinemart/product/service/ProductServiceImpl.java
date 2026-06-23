package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.SaveProductRequestDto;
import com.onlinemart.product.dto.response.SaveProductResponseDto;
import com.onlinemart.product.entity.Product;
import com.onlinemart.product.mapper.ProductMapper;
import com.onlinemart.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
		this.productRepository = productRepository;
		this.productMapper = productMapper;
	}

	@Override
	public SaveProductResponseDto saveProduct(SaveProductRequestDto requestDto) {
		Product product = productMapper.toEntity(requestDto);
		Product saved = productRepository.save(product);
		return productMapper.toSaveResponseDto(saved);
	}

}