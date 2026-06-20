package com.tzy.mapper;

import com.tzy.pojo.Carousel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CarouselMapper {

    @Select("SELECT * FROM carousel_items")
    List<Carousel> selectAll();

    @Select("SELECT * FROM carousel_items WHERE id = #{id}")
    Carousel selectById(Integer id);

    @Insert("INSERT INTO carousel_items (title, image_url, article_id, status, create_time) " +
        "VALUES (#{title}, #{imageURL}, #{articleId},#{status}, #{createTime})")
    void insert(Carousel carousel);

    @Update("UPDATE carousel_items SET title = #{title}, image_url = #{imageURL}, article_id = #{articleId}, " +
        "status = #{status}, create_time = #{createTime} WHERE id = #{id}")
    void update(Carousel carousel);

    @Delete("DELETE FROM carousel_items WHERE id = #{id}")
    void delete(Integer id);
}
