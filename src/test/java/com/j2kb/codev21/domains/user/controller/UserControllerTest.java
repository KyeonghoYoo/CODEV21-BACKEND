package com.j2kb.codev21.domains.user.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j2kb.codev21.domains.board.controller.BoardController;
import com.j2kb.codev21.domains.board.dto.BoardDto;
import com.j2kb.codev21.domains.board.service.BoardService;
import com.j2kb.codev21.domains.user.domain.Field;
import com.j2kb.codev21.domains.user.domain.Status;
import com.j2kb.codev21.domains.user.domain.User;
import com.j2kb.codev21.domains.user.dto.UserDto;
import com.j2kb.codev21.domains.user.dto.UserDto.SelectUserRes;
import com.j2kb.codev21.domains.user.dto.UserDto.UpdateUserReq;
import com.j2kb.codev21.domains.user.dto.mapper.UserMapper;
import com.j2kb.codev21.domains.user.repository.UserRepository;
import com.j2kb.codev21.domains.user.service.UserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@SpringBootTest
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class, SpringExtension.class})
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(documentationConfiguration(restDocumentation)
                .uris()
                .withScheme("http")
                .withHost("localhost.com")
                .withPort(8080))
            .build();
    }

    @DisplayName("?????? ?????? ??????")
    @Test
    void find_all_userList() throws Exception {

        //given
        List<SelectUserRes> list = new ArrayList<>();
        list.add(
            UserDto.SelectUserRes.builder()
                .id(1L)
                .email("jikimee64@gmail.com")
                .name("?????????")
                .joinGisu("2???")
                .status(Status.ACTIVE.getStatus())
                .field(Field.NONE.getField())
                .githubId("jikimee64@gmail.com")
                .createdAt(
                    LocalDateTime.parse(LocalDateTime.now().format(formatter),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .build()
        );

        when(userService.getUserList())
            .thenReturn(list);

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/admin/users")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bear {token???}"))
            .andExpect(status().isOk())
            .andDo(document("UserController/selectAllUser",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data[0].id").description("The user's id(????????????)"),
                    fieldWithPath("data[0].email").description("The user's email(?????????)"),
                    fieldWithPath("data[0].name").description("The user's name"),
                    fieldWithPath("data[0].joinGisu").description("The user's joinGisu(????????????)"),
                    fieldWithPath("data[0].status")
                        .description("The user's status(?????????(ACTIVE),????????????)"),
                    fieldWithPath("data[0].field")
                        .description("The user's field(backend,frontend...)"),
                    fieldWithPath("data[0].githubId").description("The user's githubId"),
                    fieldWithPath("data[0].createdAt").description("The user's ????????????")
                )
            ));
    }

    @DisplayName("????????????")
    @Test
    void join() throws Exception {

        String content = objectMapper.writeValueAsString(
            getUserDto()
        );

//        User user = User.builder()
//            .email("jikimee64@gmail.com")
//            .password("1q2w3e4r1!")
//            .name("?????????")
//            .joinGisu("2???")
//            .githubId("jikimee64@gmail.com")
//            .build();

        when(userService.joinUser(userMapper.joinDtoToEntity(getUserDto())))
            .thenReturn(
                UserDto.UserIdRes.builder()
                .id(1L)
                .build()
            );

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/api/v1/users")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("UserController/joinUser",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("email").description("?????????"),
                    fieldWithPath("password").description("????????????"),
                    fieldWithPath("name").description("??????"),
                    fieldWithPath("joinGisu").description("????????????"),
                    fieldWithPath("githubId").description("J2KB ?????? ???????????? ?????? ??? ???????????? ?????????ID")
                ),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data.id").description("The user's id(????????????)")
                )
            ));
    }

    @DisplayName("?????? ?????? ??????")
    @Test
    void selectUser() throws Exception {

        when(userService.getUser(anyLong()))
            .thenReturn(UserDto.SelectUserRes.builder()
                .id(1L)
                .email("jikimee64@gmail.com")
                .name("?????????")
                .joinGisu("2???")
                .status(Status.ACTIVE.getStatus())
                .field(Field.NONE.getField())
                .githubId("jikimee64@gmail.com")
                .createdAt(
                    LocalDateTime.parse(LocalDateTime.now().format(formatter),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .build());

        this.mockMvc
            .perform(RestDocumentationRequestBuilders.get("/api/v1/users/{userId}", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bear {token???}"))
            .andExpect(status().isOk())
            .andDo(document("UserController/selectUser",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data.id").description("The user's id(????????????)"),
                    fieldWithPath("data.email").description("The user's email(?????????)"),
                    fieldWithPath("data.name").description("The user's name"),
                    fieldWithPath("data.joinGisu").description("The user's joinGisu(????????????)"),
                    fieldWithPath("data.status").description("The user's status(?????????(ACTIVE),????????????)"),
                    fieldWithPath("data.field")
                        .description("The user's field(backend,frontend...)"),
                    fieldWithPath("data.githubId").description("The user's githubId"),
                    fieldWithPath("data.createdAt").description("The user's ????????????")
                ),
                pathParameters(parameterWithName("userId").description("????????? ????????????"))));
    }

    @DisplayName("?????? ??????(?????? ??????)")
    @Test
    void updateUser() throws Exception {

        UpdateUserReq dto = UpdateUserReq.builder()
            .password("1q2w3e4r2@")
            .build();

        String content = objectMapper.writeValueAsString(
            dto
        );

        //any() => ?????????????????? - UserMapper.INSTANCE.updateUserDtoToEntity(dto)
        when(userService.updateUser(anyLong(), any()))
            .thenReturn(UserDto.SelectUserRes.builder()
                .id(1L)
                .email("jikimee64@gmail.com")
                .name("?????????")
                .joinGisu("2???")
                .status(Status.ACTIVE.getStatus())
                .field(Field.NONE.getField())
                .githubId("jikimee64@gmail.com")
                .createdAt(
                    LocalDateTime.parse(LocalDateTime.now().format(formatter),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .build());

        this.mockMvc
            .perform(RestDocumentationRequestBuilders.patch("/api/v1/users/{userId}", 1L)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bear {token???}"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("UserController/updateUser",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
                requestFields(
                    fieldWithPath("password").description("????????? ????????????")
                ),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data.id").description("The user's id(????????????)"),
                    fieldWithPath("data.email").description("The user's email(?????????)"),
                    fieldWithPath("data.name").description("The user's name"),
                    fieldWithPath("data.joinGisu").description("The user's joinGisu(????????????)"),
                    fieldWithPath("data.status").description("The user's status(?????????(ACTIVE),????????????)"),
                    fieldWithPath("data.field")
                        .description("The user's field(backend,frontend...)"),
                    fieldWithPath("data.githubId").description("The user's githubId"),
                    fieldWithPath("data.createdAt").description("The user's ????????????")
                ),
                pathParameters(parameterWithName("userId").description("????????? ????????????"))));
    }


    @DisplayName("?????? ??????(????????? ??????)")
    @Test
    void updateUserByAdmin() throws Exception {

        UserDto.UpdateUserByAdminReq dto = UserDto.UpdateUserByAdminReq.builder()
            .status("INACTIVE")
            .field("BACK_END")
            .joinGisu("2???")
            .build();

        String content = objectMapper.writeValueAsString(
            dto
        );

        //any() => ?????????????????? - UserMapper.INSTANCE.updateUserByAdminDtoToEntity(dto)
        when(userService.updateUserByAdmin(anyLong(), any()))
            .thenReturn(UserDto.SelectUserRes.builder()
                .id(1L)
                .email("jikimee64@gmail.com")
                .name("?????????")
                .joinGisu("2???")
                .status(Status.ACTIVE.getStatus())
                .field(Field.NONE.getField())
                .githubId("jikimee64@gmail.com")
                .createdAt(
                    LocalDateTime.parse(LocalDateTime.now().format(formatter),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .build());

        this.mockMvc
            .perform(RestDocumentationRequestBuilders.patch("/api/v1/admin/users/{userId}", 1L)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bear {token???}"))
            .andExpect(status().isOk())
            .andDo(document("UserController/updateUserByAdmin",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
                requestFields(
                    fieldWithPath("status").description("???????????? ??????(??? : ACTIVE, INACTIVE, ARMY)"),
                    fieldWithPath("field").description("?????? ??????(??? : APP, FRONT_END, BACK_END, DATA_SCIENCE, NONE)"),
                    fieldWithPath("joinGisu").description("????????????")
                ),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data.id").description("The user's id(????????????)"),
                    fieldWithPath("data.email").description("The user's email(?????????)"),
                    fieldWithPath("data.name").description("The user's name"),
                    fieldWithPath("data.joinGisu").description("The user's joinGisu(????????????)"),
                    fieldWithPath("data.status").description("The user's status(?????????(ACTIVE),????????????)"),
                    fieldWithPath("data.field")
                        .description("The user's field(backend,frontend...)"),
                    fieldWithPath("data.githubId").description("The user's githubId"),
                    fieldWithPath("data.createdAt").description("The user's ????????????")
                ),
                pathParameters(parameterWithName("userId").description("????????? ????????????"))))
                .andDo(print());;
    }

    @DisplayName("?????? ??????")
    @Test
    void deleteUser() throws Exception {
        //given
        when(userService.deleteUser(anyLong()))
            .thenReturn(
                UserDto.DeleteUserCheckRes.builder()
                    .checkFlag(true)
                    .build()
            );

        //when
        //then
        this.mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/users/{userId}", 1L)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bear {token???}"))
            .andExpect(status().isOk())
            .andDo(document("UserController/deleteUser",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
                responseFields(
                    fieldWithPath("code").description("code(200,400...)"),
                    fieldWithPath("message").description("message(success...)"),
                    fieldWithPath("data.checkFlag").description("????????????(true, false)")
                ),
                pathParameters(parameterWithName("userId").description("????????? ?????? ??????"))));
    }

    UserDto.JoinReq getUserDto() {
        return UserDto.JoinReq.builder()
            .email("jikimee64@gmail.com")
            .password("1q2w3e4r1!")
            .name("?????????")
            .joinGisu("2???")
            .githubId("jikimee64@gmail.com")
            .build();
    }

}