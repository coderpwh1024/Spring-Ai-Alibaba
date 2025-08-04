package com.coderpwh.model;

import java.util.List;


/***
 * 商品信息
 * @param slogan(描述)
 * @param material(材质)
 * @param colors(颜色)
 * @param season(季节)
 */
public record Product(String slogan, String material, List<String> colors, String season) {

}
