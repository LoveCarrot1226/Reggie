package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Category;
import org.springframework.stereotype.Service;


public interface CategoryService extends IService<Category> {
    public void removeById(Long id);
}
