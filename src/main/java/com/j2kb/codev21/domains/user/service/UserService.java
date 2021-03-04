package com.j2kb.codev21.domains.user.service;

import com.j2kb.codev21.domains.user.domain.Authority;
import com.j2kb.codev21.domains.user.domain.Field;
import com.j2kb.codev21.domains.user.domain.Status;
import com.j2kb.codev21.domains.user.domain.User;
import com.j2kb.codev21.domains.user.domain.UserAuthority;
import com.j2kb.codev21.domains.user.dto.UserDto;
import com.j2kb.codev21.domains.user.dto.UserDto.DeleteUserCheckRes;
import com.j2kb.codev21.domains.user.dto.UserDto.SelectUserRes;
import com.j2kb.codev21.domains.user.dto.UserDto.UpdateUserByAdminReq;
import com.j2kb.codev21.domains.user.dto.mapper.UserMapper;
import com.j2kb.codev21.domains.user.exception.MemberNotFoundException;
import com.j2kb.codev21.domains.user.repository.AuthorityRepository;
import com.j2kb.codev21.domains.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.NamedStoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //회원 전체 조회
    public List<SelectUserRes> getUserList() {
        List<User> all = userRepository.findAll();

        if (all.size() == 0) {
            throw new MemberNotFoundException();
        }

        List<SelectUserRes> collect = all.stream().map(v ->
            UserMapper.INSTANCE.userToDto(v)
        ).collect(Collectors.toList());

        collect.forEach(v -> {
            v.setCreatedAt(
                LocalDateTime.parse(v.getCreatedAt().format(formatter),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        });
        return collect;
    }

    //회원가입
    @Transactional
    public UserDto.UserIdRes joinUser(UserDto.JoinReq dto) {
        User user = UserMapper.INSTANCE.userDtoToEntity(dto);
        user.changePassword((passwordEncoder.encode(dto.getPassword())));
        //ROLE_USER GET
        Optional<Authority> authorityRoleUser = authorityRepository.findById(1L);
        UserAuthority userAuthorityEntity = UserAuthority.builder()
            .user(user)
            .authority(authorityRoleUser.get())
            .build();
        user.addAuthority(userAuthorityEntity);
        User save = userRepository.save(user);
        return UserDto.UserIdRes.builder()
            .id(save.getId())
            .build();
    }

    //회원 단건 조회
    public UserDto.SelectUserRes getUser(Long userId) {
        User user = findUserEntityAndCheckIsEmpty(userId);
        SelectUserRes selectUserRes = UserMapper.INSTANCE.userToDto(user);
        return changeTimeFormat(selectUserRes);

    }

    //회원수정(유저권한)
    @Transactional
    public UserDto.SelectUserRes updateUser(Long userId, UserDto.UpdateUserReq dto) {
        User byId = userRepository.findById(userId).orElseThrow(() -> new MemberNotFoundException());
        byId.changePassword((passwordEncoder.encode(dto.getPassword())));
        SelectUserRes selectUserRes = UserMapper.INSTANCE.userToDto(byId);
        return changeTimeFormat(selectUserRes);
    }

    //회원수정(관리자 권한)
    @Transactional
    public SelectUserRes updateUserByAdmin(Long userId, UpdateUserByAdminReq dto) {
        User user = findUserEntityAndCheckIsEmpty(userId);
        user.changeUserInfoByAdmin(dto.getJoinGisu(), Status.valueOf(dto.getStatus()), Field.valueOf(dto.getField()));
        log.info("user status : " + user.getStatus());
        log.info("user field: " + user.getField());
        SelectUserRes selectUserRes = UserMapper.INSTANCE.userToDto(user);
        log.info("status : " + selectUserRes.getStatus());
        log.info("field : " + selectUserRes.getField());
        return changeTimeFormat(selectUserRes);
    }

    //회원삭제
    @Transactional
    public UserDto.DeleteUserCheckRes deleteUser(Long userId) {
        User user = findUserEntityAndCheckIsEmpty(userId);
        userRepository.deleteById(user.getId());
        return UserDto.DeleteUserCheckRes.builder().checkFlag(true).build();
    }

    private User findUserEntityAndCheckIsEmpty(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new MemberNotFoundException());
    }

    private SelectUserRes changeTimeFormat(SelectUserRes selectUserRes){
        SelectUserRes localRes = selectUserRes;
        localRes.setCreatedAt(LocalDateTime.parse(selectUserRes.getCreatedAt().format(formatter),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return localRes;
    }

}
