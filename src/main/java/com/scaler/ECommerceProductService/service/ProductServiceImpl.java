package com.scaler.ECommerceProductService.service;

import com.scaler.ECommerceProductService.Repository.CategoryRepository;
import com.scaler.ECommerceProductService.Repository.ProductRepository;
import com.scaler.ECommerceProductService.dto.ProductRequestDTO;
import com.scaler.ECommerceProductService.exception.ProductAlreadyExistsException;
import com.scaler.ECommerceProductService.exception.ProductNotFoundException;
import com.scaler.ECommerceProductService.exception.ProductServiceException;
import com.scaler.ECommerceProductService.model.Category;
import com.scaler.ECommerceProductService.model.Price;
import com.scaler.ECommerceProductService.model.Product;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Service("ProductServiceImpl")
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        }
        catch (DataAccessException e){
            throw new ProductServiceException("Error retrieving products", e);
        }
    }

    public Product getProductById(String id) throws ProductNotFoundException {
        Optional<Product> productOptional;

        try{
            productOptional = productRepository.findById(id);
        }catch (DataAccessException e){
            throw new ProductServiceException("Error retrieving the product", e);
        }
        if(productOptional.isEmpty()){
            throw new ProductNotFoundException("Product with id: " + id + " could not be found!");
        }
        return productOptional.get();
    }

    @Override
    public Product addProduct(ProductRequestDTO requestDTO) throws ProductAlreadyExistsException {
        Product product = new Product();
        Optional<Product> productOptional = productRepository.findByTitleIgnoreCase(requestDTO.getTitle());
        if(productOptional.isPresent()){
            throw new ProductAlreadyExistsException("The product you are trying to save already exists!");
        }

        String categoryName = requestDTO.getCategory();
        Category requestedCategory;

        Optional<Category> categoryOptional = categoryRepository.findByCategoryNameIgnoreCase(categoryName);
        if(categoryOptional.isEmpty()){
            requestedCategory = new Category();
            requestedCategory.setCategoryName(requestDTO.getCategory());
        }
        else{
            requestedCategory = categoryOptional.get();
        }

        Price price = new Price();
        price.setPrice(requestDTO.getPrice());
        price.setCurrency(Currency.getInstance(requestDTO.getCurrencyCode().toUpperCase()));
        price.setDiscount(requestDTO.getDiscountPercentage());

        product.setCategory(requestedCategory);
        product.setPrice(price);
        product.setTitle(requestDTO.getTitle());
        product.setDescription(requestDTO.getDescription());
        product.setImage(requestDTO.getImage());

        Product savedProduct;

        try{
            savedProduct = productRepository.save(product);
        } catch (DataAccessException e){
            throw new ProductServiceException("Error saving the product!", e);
        }

        return savedProduct;
    }

    @Override
    public Product deleteProduct(String id) throws ProductNotFoundException {
        Optional<Product> productOptional;
        //Searching for the product
        try{
            productOptional = productRepository.findById(id);
        }catch (DataAccessException e){
            throw new ProductServiceException("Error Retrieving the product", e);
        }

        if(productOptional.isEmpty()){
            throw new ProductNotFoundException("Product with id: " + id + " could not be found!");
        }

        try{
            productRepository.deleteById(id);
        }catch (DataAccessException e){
            throw new ProductServiceException("Error retrieving the product", e);
        }

        return productOptional.get();
    }

    @Override
    public Product updateProduct(String id, ProductRequestDTO requestDTO) throws ProductNotFoundException {
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isEmpty()){
            throw new ProductNotFoundException("Product with id: " + id + " could not be found!");
        }

        Category requestedCategory;

        Optional<Category> categoryOptional = categoryRepository.findByCategoryNameIgnoreCase(requestDTO.getCategory());
        if(categoryOptional.isEmpty()){
            requestedCategory = new Category();
            requestedCategory.setCategoryName(requestDTO.getCategory());
        }
        else{
            requestedCategory = categoryOptional.get();
        }

        Product product = productOptional.get();

        Price updatedPrice;
        Price currentPrice = product.getPrice();

        if(!currentPrice.getCurrency().getCurrencyCode().equalsIgnoreCase(requestDTO.getCurrencyCode())
                || currentPrice.getPrice() != requestDTO.getPrice()
                || currentPrice.getDiscount() != requestDTO.getDiscountPercentage()
        ){
            updatedPrice = new Price();
            updatedPrice.setPrice(requestDTO.getPrice());
            updatedPrice.setCurrency(Currency.getInstance(requestDTO.getCurrencyCode().toUpperCase()));
            updatedPrice.setDiscount(requestDTO.getDiscountPercentage());
        }
        else{
            updatedPrice = product.getPrice();
        }

        product.setTitle(requestDTO.getTitle());
        product.setImage(requestDTO.getImage());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(updatedPrice);
        product.setCategory(requestedCategory);

        Product updatedProduct;

        try{
            updatedProduct = productRepository.save(product);
        } catch (DataAccessException e){
            throw new ProductServiceException("Error saving the updated product!", e);
        }

        return updatedProduct;
    }

    @Override
    public Product modifyProduct(String id, ProductRequestDTO requestDTO) throws ProductNotFoundException {
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isEmpty()){
            throw new ProductNotFoundException("Product with id: " + id + " could not be found!");
        }

        Product currentProduct = productOptional.get();

        Category updatedCategory;
        Optional<Category> categoryOptional = categoryRepository.findByCategoryNameIgnoreCase(requestDTO.getCategory());

        if(requestDTO.getCategory() == null
                || requestDTO.getCategory().isBlank()
                || categoryOptional.isEmpty()
                || categoryOptional.get().equals(currentProduct.getCategory())
        ){
            updatedCategory = currentProduct.getCategory();
        }
        else{
            updatedCategory = new Category();
            updatedCategory.setCategoryName(requestDTO.getCategory());
        }

        Price updatedPrice;
        Price currentPrice = currentProduct.getPrice();

        if(requestDTO.getPrice() > 0 &&
                (!currentPrice.getCurrency().getCurrencyCode().equalsIgnoreCase(requestDTO.getCurrencyCode())
                || currentPrice.getPrice() != requestDTO.getPrice()
                || currentPrice.getDiscount() != requestDTO.getDiscountPercentage())
        ){
            updatedPrice = new Price();
            updatedPrice.setCurrency(Currency.getInstance(requestDTO.getCurrencyCode()));
            updatedPrice.setDiscount(requestDTO.getDiscountPercentage());
            updatedPrice.setPrice(requestDTO.getPrice());
        }
        else{
            updatedPrice = currentPrice;
        }

        currentProduct.setCategory(updatedCategory);
        currentProduct.setPrice(updatedPrice);

        if(requestDTO.getTitle() != null && !requestDTO.getTitle().isBlank() && requestDTO.getTitle().trim().length() >= 3) {
            currentProduct.setTitle(requestDTO.getTitle());
        }

        if(requestDTO.getDescription() != null && !requestDTO.getDescription().isBlank() && requestDTO.getDescription().trim().length() >= 5){
            currentProduct.setDescription(requestDTO.getDescription());
        }

        if(!requestDTO.getImage().equalsIgnoreCase("placeholder.jpg") && !requestDTO.getImage().isBlank() && !requestDTO.getImage().equalsIgnoreCase(currentProduct.getImage())){
            currentProduct.setImage(requestDTO.getImage());
        }

        Product modifiedProduct;
        try {
            modifiedProduct = productRepository.save(currentProduct);
        } catch (DataAccessException e) {
            throw new ProductServiceException("Error while modifying the product!", e);
        }

        return modifiedProduct;
    }
}
