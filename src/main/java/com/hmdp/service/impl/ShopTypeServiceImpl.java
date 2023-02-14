package com.hmdp.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        String key = CACHE_SHOPTYPE_KEY;
        List<String> typeStrList = stringRedisTemplate.opsForList().range(key, 0, -1);

        if (typeStrList != null && !typeStrList.isEmpty()) {
            List<ShopType> typeList = typeStrList.stream().map(typeStr -> JSONUtil.toBean(typeStr, ShopType.class)).collect(Collectors.toList());
            return Result.ok(typeList);
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();

        if (typeList == null || typeList.isEmpty()) {
            return Result.fail("店铺类型不存在");
        }

        typeStrList = typeList.stream().map(type -> JSONUtil.toJsonStr(type)).collect(Collectors.toList());

        stringRedisTemplate.opsForList().rightPushAll(key, typeStrList);

        return Result.ok(typeList);
    }
}
