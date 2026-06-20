package com.tzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tzy.mapper.TagsMapper;
import com.tzy.pojo.Tags;
import com.tzy.service.TagsService;
import org.springframework.stereotype.Service;

@Service
public class TagsServiceImpl extends ServiceImpl<TagsMapper, Tags> implements TagsService {
}
