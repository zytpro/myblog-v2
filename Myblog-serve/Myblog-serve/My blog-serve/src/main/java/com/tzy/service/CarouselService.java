package com.tzy.service;

import com.tzy.pojo.Carousel;

import java.util.List;

public interface CarouselService {
    List<Carousel> getAllCarousels();

    Carousel getCarouselById(Integer id);

    Carousel addCarousel(Carousel carousel);

    Carousel updateCarousel(Carousel carousel);

    void deleteCarousel(Integer id);
}
