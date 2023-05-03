package main001.server.domain.user.controller;

import com.google.gson.Gson;
import main001.server.domain.user.dto.UserDto;
import main001.server.domain.user.entity.User;
import main001.server.domain.user.mapper.UserMapper;
import main001.server.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static main001.server.domain.user.enums.Grade.NOVICE;
import static main001.server.domain.user.enums.JobStatus.JOB_SEEKING;
import static main001.server.domain.user.enums.JobStatus.ON_THE_JOB;
import static main001.server.domain.user.enums.UserStatus.USER_ACTIVE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class UserControllerRestDocsTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson ;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper mapper;

    @Test
    @DisplayName("회원가입")
    public void joinTest() throws Exception {
        // given
        UserDto.Post post = new UserDto.Post("test1@gmail.com",
                "사용자1",
                "",
                "https://github.com/test1",
                "https://blog.com/test1",
                JOB_SEEKING,
                "자기소개");

        String content = gson.toJson(post);

        given(mapper.userPostToUser(Mockito.any(UserDto.Post.class))).willReturn(new User());

        User mockResultUser = new User();
        mockResultUser.setUserId(1L);
        given(userService.createUser(Mockito.any(User.class))).willReturn(mockResultUser);
        URI uri = UriComponentsBuilder.newInstance().path("/users/signup").build().toUri();
        // when
        ResultActions actions =
                mockMvc.perform(
                        post(uri)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content));

        // then
        actions.andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/users/"))))
                .andDo(document(
                        "post-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                List.of(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지"),
                                        fieldWithPath("gitLink").type(JsonFieldType.STRING).description("깃 링크"),
                                        fieldWithPath("blogLink").type(JsonFieldType.STRING).description("블로그 링크"),
                                        fieldWithPath("jobStatus").type(JsonFieldType.STRING).description("구직현황 : JOB_SEEKING(구직중), ON_THE_JOB(재직중), STUDENT(학생)"),
                                        fieldWithPath("about").type(JsonFieldType.STRING).description("자기소개")
                                )),
                        responseHeaders(headerWithName(HttpHeaders.LOCATION).description("Location header. 등록된 리소스의 URI")
                        )));
    }

    @Test
    @DisplayName("회원 조회")
    public void getUserTest() throws Exception {
        // given
        long userId = 1L;
        UserDto.Response response = new UserDto.Response(1L,"test1@gmail.com",
                "사용자1",
                "",
                "https://github.com/test1",
                "https://blog.com/test1",
                NOVICE,
                USER_ACTIVE,
                JOB_SEEKING,
                "자기소개",
                LocalDateTime.now(),
                LocalDateTime.now());
        given(userService.findUser(Mockito.anyLong())).willReturn(new User());
        given(mapper.userToUserResponse(Mockito.any(User.class))).willReturn(response);

        // then
        ResultActions actions = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/users/{user-id}", userId)
                                .accept(MediaType.APPLICATION_JSON));
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(response.getEmail()))
                .andExpect(jsonPath("$.data.name").value(response.getName()))
                .andExpect(jsonPath("$.data.about").value(response.getAbout()))
                .andDo(document("get-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("user-id").description("회원 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("결과 데이터"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data.profileImg").type(JsonFieldType.STRING).description("프로필 이미지"),
                                fieldWithPath("data.gitLink").type(JsonFieldType.STRING).description("깃 링크"),
                                fieldWithPath("data.blogLink").type(JsonFieldType.STRING).description("블로그 링크"),
                                fieldWithPath("data.grade").type(JsonFieldType.STRING).description("회원등급 : NOVICE(초보자), INTERMEDIATE(중급자), ADVANCED(고급자), EXPERT(전문가), MASTER(마스터)"),
                                fieldWithPath("data.userStatus").type(JsonFieldType.STRING).description("회원상태 : USER_ACTIVE(활동중), USER_SLEEP(휴면 상태), USER_QUIT(탈퇴 상태)"),
                                fieldWithPath("data.jobStatus").type(JsonFieldType.STRING).description("구직현황 :  JOB_SEEKING(구직중), ON_THE_JOB(재직중), STUDENT(학생)"),
                                fieldWithPath("data.about").type(JsonFieldType.STRING).description("자기소개"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성된 시간"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정된 시간")
                        ))
                ));

    }

    @Test
    @DisplayName("회원목록 조회")
    public void getUsersTest() throws Exception {
        // given
        String page = "1";
        String size = "10";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", page);
        queryParams.add("size", size);

        User user1 = new User(1L, "test1@gmail.com","사용자1","","https://github.com/test1","https://blog.com/test1", JOB_SEEKING,"자기소개");
        user1.setGrade(NOVICE);
        user1.setUserStatus(USER_ACTIVE);

        User user2 = new User(2L, "test2@gmail.com","사용자2","","https://github.com/test2","https://blog.com/test2", JOB_SEEKING,"자기소개");
        user2.setGrade(NOVICE);
        user2.setUserStatus(USER_ACTIVE);

        User user3 = new User(3L, "test3@gmail.com","사용자3","","https://github.com/test3","https://blog.com/test3", JOB_SEEKING,"자기소개");
        user3.setGrade(NOVICE);
        user3.setUserStatus(USER_ACTIVE);

        Page<User> users = new PageImpl<>(List.of(user1, user2, user3),
                PageRequest.of(0, 10, Sort.by("userId").descending()), 3);
        List<UserDto.Response> responses = List.of(new UserDto.Response(1L, "test1@gmail.com", "사용자1", "", "https://github.com/test1", "https://blog.com/test1", NOVICE, USER_ACTIVE, JOB_SEEKING, "자기소개", LocalDateTime.now(), LocalDateTime.now()),
                                                    new UserDto.Response(2L, "test2@gmail.com", "사용자2", "", "https://github.com/test2", "https://blog.com/test2", NOVICE, USER_ACTIVE, JOB_SEEKING, "자기소개", LocalDateTime.now(), LocalDateTime.now()),
                                                    new UserDto.Response(3L, "test3@gmail.com", "사용자3", "", "https://github.com/test3", "https://blog.com/test3", NOVICE, USER_ACTIVE, JOB_SEEKING, "자기소개", LocalDateTime.now(), LocalDateTime.now()));

        given(userService.findUsers(Mockito.anyInt(), Mockito.anyInt())).willReturn(users);
        given(mapper.usersToUserResponses(Mockito.anyList())).willReturn(responses);
        // when
        ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/users")
                .params(queryParams)
                .accept(MediaType.APPLICATION_JSON));

        // then
        MvcResult result = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pageInfo.totalElements").value(3))
                .andDo(document("get-users",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(List.of(
                                parameterWithName("page").description("조회할 페이지"),
                                parameterWithName("size").description("페이지당 컨텐츠 수")
                                )
                        ),
                        responseFields(List.of(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("결과 데이터"),
                                fieldWithPath("data[].userId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                fieldWithPath("data[].email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data[].profileImg").type(JsonFieldType.STRING).description("프로필 이미지"),
                                fieldWithPath("data[].gitLink").type(JsonFieldType.STRING).description("깃 링크"),
                                fieldWithPath("data[].blogLink").type(JsonFieldType.STRING).description("블로그 링크"),
                                fieldWithPath("data[].grade").type(JsonFieldType.STRING).description("회원등급 : NOVICE(초보자), INTERMEDIATE(중급자), ADVANCED(고급자), EXPERT(전문가), MASTER(마스터)"),
                                fieldWithPath("data[].userStatus").type(JsonFieldType.STRING).description("회원상태 : USER_ACTIVE(활동중), USER_SLEEP(휴면 상태), USER_QUIT(탈퇴 상태)"),
                                fieldWithPath("data[].jobStatus").type(JsonFieldType.STRING).description("구직현황 :  JOB_SEEKING(구직중), ON_THE_JOB(재직중), STUDENT(학생)"),
                                fieldWithPath("data[].about").type(JsonFieldType.STRING).description("자기소개"),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("생성된 시간"),
                                fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING).description("수정된 시간"),
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("해당 페이지"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 컨텐츠 수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 컨텐츠 수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수")
                        ))
                ))
                .andReturn();
    }

    @Test
    @DisplayName("회원정보 수정")
    public void patchUserTest() throws Exception {
        // given
        Long userId = 1L;
        UserDto.Patch patch = new UserDto.Patch(1L,
                "변경1", // 변경전 "사용자1"
                "",
                "https://github.com/patch1", // 변경전 "https://github.com/test1"
                "https://blog.com/patch1", //변경전 "https://blog.com/test1"
                USER_ACTIVE,
                ON_THE_JOB, // 변경전 JOB_SEEKING
                "수정 완료"); // 변경전 "자기소개"
        String patchContent = gson.toJson(patch);

        UserDto.Response responseDto = new UserDto.Response(1L, "test1@gmail.com", "변경1", "", "https://github.com/patch1", "https://blog.com/patch1", NOVICE, USER_ACTIVE, ON_THE_JOB, "수정 완료", LocalDateTime.now(), LocalDateTime.now());

        given(mapper.userPatchToUser(Mockito.any(UserDto.Patch.class))).willReturn(new User());
        given(userService.updateUser(Mockito.any(User.class))).willReturn(new User());
        given(mapper.userToUserResponse(Mockito.any(User.class))).willReturn(responseDto);

        // when
        ResultActions actions =
                mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/users/{user-id}", userId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                );

        // then
        ConstraintDescriptions patchUserConstraints = new ConstraintDescriptions(UserDto.Patch.class); // 유효성 검증 조건 정보 객체 생성
        List<String> nameDescriptions = patchUserConstraints.descriptionsForProperty("name"); // name 필드의 유효성 검증 정보 얻기
        List<String> aboutDescriptions = patchUserConstraints.descriptionsForProperty("about"); // about 필드의 유효성 검증 정보 얻기

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
                .andExpect(jsonPath("$.data.about").value(patch.getAbout()))
                .andDo(document("patch-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("user-id").description("회원 식별자")),
                        requestFields(List.of(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 식별자").ignored(),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일").optional(),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름").optional(),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지").optional(),
                                fieldWithPath("gitLink").type(JsonFieldType.STRING).description("깃 링크").optional(),
                                fieldWithPath("blogLink").type(JsonFieldType.STRING).description("블로그 링크").optional(),
                                fieldWithPath("userStatus").type(JsonFieldType.STRING).description("회원상태 : USER_ACTIVE(활동중), USER_SLEEP(휴면 상태), USER_QUIT(탈퇴 상태)").optional(),
                                fieldWithPath("jobStatus").type(JsonFieldType.STRING).description("구직현황 : JOB_SEEKING(구직중), ON_THE_JOB(재직중), STUDENT(학생)").optional(),
                                fieldWithPath("about").type(JsonFieldType.STRING).description("자기소개").optional()

                        )),
                        responseFields(List.of(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("결과 데이터"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data.profileImg").type(JsonFieldType.STRING).description("프로필 이미지"),
                                fieldWithPath("data.gitLink").type(JsonFieldType.STRING).description("깃 링크"),
                                fieldWithPath("data.blogLink").type(JsonFieldType.STRING).description("블로그 링크"),
                                fieldWithPath("data.grade").type(JsonFieldType.STRING).description("회원등급 : NOVICE(초보자), INTERMEDIATE(중급자), ADVANCED(고급자), EXPERT(전문가), MASTER(마스터)"),
                                fieldWithPath("data.userStatus").type(JsonFieldType.STRING).description("회원상태 : USER_ACTIVE(활동중), USER_SLEEP(휴면 상태), USER_QUIT(탈퇴 상태)"),
                                fieldWithPath("data.jobStatus").type(JsonFieldType.STRING).description("구직현황 :  JOB_SEEKING(구직중), ON_THE_JOB(재직중), STUDENT(학생)"),
                                fieldWithPath("data.about").type(JsonFieldType.STRING).description("자기소개"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성된 시간"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정된 시간")
                        ))
                ));
    }

    @Test
    @DisplayName("회원 탈퇴")
    public void deleteUserTest() throws Exception {
        // given
        long userId = 1L;
        doNothing().when(userService).deleteUser(Mockito.anyLong());

        // when
        ResultActions actions = mockMvc.perform(
                RestDocumentationRequestBuilders.delete("/users/{user-id}", userId)
        );

        // then
        actions.andExpect(status().isNoContent())
                .andDo(
                        document(
                                "delete-user",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        Arrays.asList(parameterWithName("user-id").description("회원 식별자 ID"))
                                )
                        )
                );
    }
}