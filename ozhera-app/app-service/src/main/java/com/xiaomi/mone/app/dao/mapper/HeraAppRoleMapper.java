/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.app.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaomi.mone.app.model.HeraAppRole;
import com.xiaomi.mone.app.model.HeraAppRoleExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface HeraAppRoleMapper extends BaseMapper<HeraAppRole> {
    long countByExample(HeraAppRoleExample example);

    int deleteByExample(HeraAppRoleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HeraAppRole record);

    int insertSelective(HeraAppRole record);

    List<HeraAppRole> selectByExample(HeraAppRoleExample example);

    HeraAppRole selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HeraAppRole record, @Param("example") HeraAppRoleExample example);

    int updateByExample(@Param("record") HeraAppRole record, @Param("example") HeraAppRoleExample example);

    int updateByPrimaryKeySelective(HeraAppRole record);

    int updateByPrimaryKey(HeraAppRole record);

    int batchInsert(@Param("list") List<HeraAppRole> list);

    int batchInsertSelective(@Param("list") List<HeraAppRole> list, @Param("selective") HeraAppRole.Column... selective);
}