package com.tzy.service.impl;

import com.tzy.mapper.CarouselMapper;
import com.tzy.pojo.Carousel;
import com.tzy.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarouselServiceImpl implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public List<Carousel> getAllCarousels() {
        return carouselMapper.selectAll();
    }

    @Override
    public Carousel getCarouselById(Integer id) {
        return carouselMapper.selectById(id);
    }

    @Override
    public Carousel addCarousel(Carousel carousel) {
        carouselMapper.insert(carousel);
        return carousel;
    }

    @Override
    public Carousel updateCarousel(Carousel carousel) {
        carouselMapper.update(carousel);
        return carousel;
    }

    @Override
    public void deleteCarousel(Integer id) {
        carouselMapper.delete(id);
    }
}
