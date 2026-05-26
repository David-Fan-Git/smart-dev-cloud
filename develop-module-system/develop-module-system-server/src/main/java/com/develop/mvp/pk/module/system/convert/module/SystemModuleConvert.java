package com.develop.mvp.pk.module.system.convert.module;

import com.develop.mvp.pk.module.system.application.module.command.RegisterModuleCommand;
import com.develop.mvp.pk.module.system.application.module.dto.SystemModuleDTO;
import com.develop.mvp.pk.module.system.controller.admin.module.vo.SystemModuleRegisterReqVO;
import com.develop.mvp.pk.module.system.controller.admin.module.vo.SystemModuleRespVO;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SystemModuleConvert {

    SystemModuleConvert INSTANCE = Mappers.getMapper(SystemModuleConvert.class);

    default RegisterModuleCommand toCommand(SystemModuleRegisterReqVO reqVO) {
        List<ModuleDependency> dependencies = reqVO.getDependencies() == null ? List.of()
                : reqVO.getDependencies().stream().map(ModuleDependency::required).toList();
        return new RegisterModuleCommand(reqVO.getCode(), reqVO.getName(), reqVO.getVersion(), reqVO.getEnabled(),
                dependencies, reqVO.getOrder() == null ? 0 : reqVO.getOrder(), reqVO.getDescription());
    }

    default SystemModuleRespVO toRespVO(SystemModuleDTO dto) {
        return new SystemModuleRespVO()
                .setCode(dto.code())
                .setName(dto.name())
                .setVersion(dto.version())
                .setEnabled(dto.enabled())
                .setDependencies(dto.dependencies())
                .setOrder(dto.order())
                .setDescription(dto.description())
                .setState(dto.state())
                .setFailureReason(dto.failureReason());
    }
}
