package com.compass.yuhengapi.model.converter;

import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.vo.ApiClientVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")  // 添加这个
public interface ApiClientConverter {

    ApiClientVo toPageVo(ApiClient bo);

//    Page<UserPageVO> toPageVo(Page<UserBO> bo);
//
//    List<UserPageVO> toPageVo(List<UserBO> bo);
//
//    UserForm toForm(User entity);
//
//    @InheritInverseConfiguration(name = "toForm")
//    User toEntity(UserForm entity);
//
//    @Mappings({
//        @Mapping(target = "userId", source = "id")
//    })
//    CurrentUserDTO toCurrentUserDto(User entity);
}
