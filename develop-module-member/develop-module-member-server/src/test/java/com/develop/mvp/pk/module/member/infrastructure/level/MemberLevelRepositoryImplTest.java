package com.develop.mvp.pk.module.member.infrastructure.level;

import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberLevelDO;
import com.develop.mvp.pk.module.member.dal.mysql.level.MemberLevelMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class MemberLevelRepositoryImplTest extends BaseMockitoUnitTest {

    @Mock
    private MemberLevelMapper mapper;

    @InjectMocks
    private MemberLevelRepositoryImpl repository;

    @Test
    void findAll_returnsLevelsOrderedByLevel() {
        MemberLevelDO high = MemberLevelDO.builder().id(1L).name("高等级").level(3).build();
        MemberLevelDO low = MemberLevelDO.builder().id(2L).name("低等级").level(1).build();
        MemberLevelDO middle = MemberLevelDO.builder().id(3L).name("中等级").level(2).build();
        when(mapper.selectList()).thenReturn(List.of(high, low, middle));

        List<Integer> levels = repository.findAll().stream()
                .map(com.develop.mvp.pk.module.member.domain.level.MemberLevel::level)
                .toList();

        assertEquals(List.of(1, 2, 3), levels);
    }
}
